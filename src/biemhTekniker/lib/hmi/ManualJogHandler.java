package biemhTekniker.lib.hmi;

import biemhTekniker.lib.config.ConfigManager;
import biemhTekniker.lib.logger.Logger;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.geometricModel.World;
import com.kuka.roboticsAPI.motionModel.IMotionContainer;
import com.kuka.roboticsAPI.uiModel.userKeys.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.linRel;

/**
 * Handles manual jogging of the robot via SmartPad HMI buttons.
 * Uses press-and-hold behavior for continuous motion in WORLD coordinates.
 * Supports 12 directions: X+/-, Y+/-, Z+/-, A+/-, B+/-, C+/-
 */
public class ManualJogHandler
{
    private static final Logger log = Logger.getLogger(ManualJogHandler.class);

    // Direction flags for 12 jog axes (thread-safe communication between UI and cyclic task)
    private final AtomicBoolean jogXPlus = new AtomicBoolean(false);
    private final AtomicBoolean jogXMinus = new AtomicBoolean(false);
    private final AtomicBoolean jogYPlus = new AtomicBoolean(false);
    private final AtomicBoolean jogYMinus = new AtomicBoolean(false);
    private final AtomicBoolean jogZPlus = new AtomicBoolean(false);
    private final AtomicBoolean jogZMinus = new AtomicBoolean(false);
    private final AtomicBoolean jogAPlus = new AtomicBoolean(false);
    private final AtomicBoolean jogAMinus = new AtomicBoolean(false);
    private final AtomicBoolean jogBPlus = new AtomicBoolean(false);
    private final AtomicBoolean jogBMinus = new AtomicBoolean(false);
    private final AtomicBoolean jogCPlus = new AtomicBoolean(false);
    private final AtomicBoolean jogCMinus = new AtomicBoolean(false);

    // Motion container for cancellation on key release (thread-safe)
    private final AtomicReference<IMotionContainer> activeMotion = new AtomicReference<IMotionContainer>(null);

    // Robot and tool references
    private final LBR robot;
    private final Tool tool;

    // World frame reference (cached for consistent coordinate system)
    private final ObjectFrame worldFrame;

    // Configuration parameters
    private final double linearStepMm;
    private final double rotationalStepDeg;
    private final double jogVelocityRel;

    /**
     * Creates a new manual jog handler.
     *
     * @param robot The LBR robot instance
     * @param tool  The tool attached to the robot flange
     */
    public ManualJogHandler(LBR robot, Tool tool)
    {
        this.robot = robot;
        this.tool = tool;
        this.worldFrame = World.Current.getRootFrame();

        // Load configuration parameters
        ConfigManager config = ConfigManager.getInstance();
        this.linearStepMm = config.getDouble("jog.linear.step.mm", 2.0);
        this.rotationalStepDeg = config.getDouble("jog.rotational.step.deg", 1.0);
        this.jogVelocityRel = config.getDouble("jog.velocity.rel", 0.1);
        

        log.info("ManualJogHandler initialized: linearStep=" + linearStepMm + "mm, rotationalStep=" + rotationalStepDeg + "deg, velocityRel=" + jogVelocityRel);
    }

    /**
     * Registers a key bar with 3 buttons for a specific axis group.
     *
     * @param keyBar     The user key bar to register buttons on
     * @param axisIndex0 Index for button 0 (0=X+, 1=X-, 2=Y+, 3=Y-, 4=Z+, 5=Z-, 6=A+, 7=A-, 8=B+, 9=B-, 10=C+, 11=C-)
     * @param axisIndex1 Index for button 1
     * @param axisIndex2 Index for button 2
     * @param label0     Label for button 0
     * @param label1     Label for button 1
     * @param label2     Label for button 2
     */
    public void registerKeyBar(IUserKeyBar keyBar, final int axisIndex0, final int axisIndex1, final int axisIndex2,
                                String label0, String label1, String label2)
    {
        try
        {
            // Button 0
            IUserKey key0 = keyBar.addUserKey(0, new IUserKeyListener()
            {
                @Override
                public void onKeyEvent(IUserKey key, UserKeyEvent event)
                {
                    handleKeyEvent(axisIndex0, event);
                }
            }, true);
            key0.setText(UserKeyAlignment.TopMiddle, label0);
            key0.setEnabled(true);

            // Button 1
            IUserKey key1 = keyBar.addUserKey(1, new IUserKeyListener()
            {
                @Override
                public void onKeyEvent(IUserKey key, UserKeyEvent event)
                {
                    handleKeyEvent(axisIndex1, event);
                }
            }, true);
            key1.setText(UserKeyAlignment.TopMiddle, label1);
            key1.setEnabled(true);

            // Button 2
            IUserKey key2 = keyBar.addUserKey(2, new IUserKeyListener()
            {
                @Override
                public void onKeyEvent(IUserKey key, UserKeyEvent event)
                {
                    handleKeyEvent(axisIndex2, event);
                }
            }, true);
            key2.setText(UserKeyAlignment.TopMiddle, label2);
            key2.setEnabled(true);

            keyBar.publish();
            log.info("Jog key bar registered: " + label0 + ", " + label1 + ", " + label2);

        } catch (Exception e)
        {
            log.error("Failed to register jog key bar: " + e.getMessage(), e);
        }
    }

    /**
     * Handles key events for a specific axis direction.
     *
     * @param axisIndex Direction index (0-11)
     * @param event     Key event (KeyDown or KeyUp)
     */
    private void handleKeyEvent(int axisIndex, UserKeyEvent event)
    {
        try
        {
            if (event == UserKeyEvent.KeyDown)
            {
                // Set the flag for this direction
                setAxisFlag(axisIndex, true);
                log.debug("Jog started: axis " + axisIndex);
            } else if (event == UserKeyEvent.KeyUp)
            {
                // Clear the flag and cancel any active motion
                setAxisFlag(axisIndex, false);
                cancelActiveMotion();
                log.debug("Jog stopped: axis " + axisIndex);
            }
        } catch (Exception e)
        {
            log.error("Error handling jog key event for axis " + axisIndex + ": " + e.getMessage(), e);
        }
    }

    /**
     * Sets the flag for a specific axis direction.
     *
     * @param axisIndex Direction index (0-11)
     * @param active    True to activate, false to deactivate
     */
    private void setAxisFlag(int axisIndex, boolean active)
    {
        switch (axisIndex)
        {
            case 0:
                jogXPlus.set(active);
                break;
            case 1:
                jogXMinus.set(active);
                break;
            case 2:
                jogYPlus.set(active);
                break;
            case 3:
                jogYMinus.set(active);
                break;
            case 4:
                jogZPlus.set(active);
                break;
            case 5:
                jogZMinus.set(active);
                break;
            case 6:
                jogAPlus.set(active);
                break;
            case 7:
                jogAMinus.set(active);
                break;
            case 8:
                jogBPlus.set(active);
                break;
            case 9:
                jogBMinus.set(active);
                break;
            case 10:
                jogCPlus.set(active);
                break;
            case 11:
                jogCMinus.set(active);
                break;
        }
    }

    /**
     * Cancels any active motion.
     */
    private void cancelActiveMotion()
    {
        IMotionContainer motion = activeMotion.getAndSet(null);
        if (motion != null)
        {
            try
            {
                motion.cancel();
                log.debug("Active motion cancelled");
            } catch (Exception e)
            {
                log.error("Error cancelling active motion: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Processes cyclic jog commands. Called from the background task's runCyclic().
     * Finds the first active flag (priority order: X > Y > Z > A > B > C) and executes
     * a small linRel increment in that direction.
     * Skips execution if a previous motion is still in progress.
     */
    public void processCyclic()
    {
        try
        {
            // Skip if a motion is already in progress
            IMotionContainer currentMotion = activeMotion.get();
            if (currentMotion != null && !currentMotion.isFinished())
            {
                return;
            }

            // Priority order: X > Y > Z > A > B > C
            double dx = 0.0, dy = 0.0, dz = 0.0;
            double da = 0.0, db = 0.0, dc = 0.0;

            // Only one axis at a time - check in priority order
            if (jogXPlus.get())
            {
                dx = linearStepMm;
            } else if (jogXMinus.get())
            {
                dx = -linearStepMm;
            } else if (jogYPlus.get())
            {
                dy = linearStepMm;
            } else if (jogYMinus.get())
            {
                dy = -linearStepMm;
            } else if (jogZPlus.get())
            {
                dz = linearStepMm;
            } else if (jogZMinus.get())
            {
                dz = -linearStepMm;
            } else if (jogAPlus.get())
            {
                da = Math.toRadians(rotationalStepDeg);
            } else if (jogAMinus.get())
            {
                da = -Math.toRadians(rotationalStepDeg);
            } else if (jogBPlus.get())
            {
                db = Math.toRadians(rotationalStepDeg);
            } else if (jogBMinus.get())
            {
                db = -Math.toRadians(rotationalStepDeg);
            } else if (jogCPlus.get())
            {
                dc = Math.toRadians(rotationalStepDeg);
            } else if (jogCMinus.get())
            {
                dc = -Math.toRadians(rotationalStepDeg);
            } else
            {
                // No axis active, nothing to do
                return;
            }

            // Execute the linRel motion in WORLD coordinates
            IMotionContainer motion = tool.move(
                    linRel(dx, dy, dz, da, db, dc, worldFrame)
                            .setJointVelocityRel(jogVelocityRel)
            );
            activeMotion.set(motion);

        } catch (Exception e)
        {
            log.error("Error during jog motion: " + e.getMessage(), e);
            clearAllFlags();
        }
    }

    /**
     * Clears all jog flags. Used for error recovery and shutdown.
     */
    public void clearAllFlags()
    {
        jogXPlus.set(false);
        jogXMinus.set(false);
        jogYPlus.set(false);
        jogYMinus.set(false);
        jogZPlus.set(false);
        jogZMinus.set(false);
        jogAPlus.set(false);
        jogAMinus.set(false);
        jogBPlus.set(false);
        jogBMinus.set(false);
        jogCPlus.set(false);
        jogCMinus.set(false);
        cancelActiveMotion();
        log.info("All jog flags cleared");
    }

    /**
     * Checks if any axis is currently active.
     *
     * @return True if any jog direction is active
     */
    public boolean isAnyAxisActive()
    {
        return jogXPlus.get() || jogXMinus.get() ||
                jogYPlus.get() || jogYMinus.get() ||
                jogZPlus.get() || jogZMinus.get() ||
                jogAPlus.get() || jogAMinus.get() ||
                jogBPlus.get() || jogBMinus.get() ||
                jogCPlus.get() || jogCMinus.get();
    }
}
