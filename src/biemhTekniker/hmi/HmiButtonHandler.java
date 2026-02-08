package biemhTekniker.hmi;

import biemhTekniker.logger.Logger;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.uiModel.userKeys.*;

/**
 * Handles HMI programmable button events on the KUKA SmartPad.
 * Implements the four side buttons with the following functionality:
 * - Button 1: Toggle gripper open/close
 * - Button 2: Reserved (future use, e.g. tool change)
 * - Button 3: Print current robot position to console
 * - Button 4: Reserved for future use
 */
public class HmiButtonHandler implements IUserKeyListener
{
    private static final Logger log = Logger.getLogger(HmiButtonHandler.class);

    private final LBR robot;
    private final Tool gripper;
    private final MediaFlangeIOGroup gripperIO;

    // Track gripper state for button 1
    private boolean gripperOpen = true;

    // User key references
    private IUserKey button1;
    private IUserKey button2;
    private IUserKey button3;
    private IUserKey button4;

    /**
     * Creates a new HMI button handler.
     *
     * @param robot     The LBR robot instance
     * @param gripper   The gripper tool
     * @param gripperIO The MediaFlange IO group for controlling the gripper
     */
    public HmiButtonHandler(LBR robot, Tool gripper, MediaFlangeIOGroup gripperIO)
    {
        this.robot = robot;
        this.gripper = gripper;
        this.gripperIO = gripperIO;
    }

    /**
     * Registers the user keys with the HMI user key bar.
     * This must be called during application initialization.
     *
     * @param keyBar The user key bar from getApplicationUI().createUserKeyBar()
     */
    public void registerUserKeys(IUserKeyBar keyBar)
    {
        try
        {
            button1 = keyBar.addUserKey(0, this, true);
            button1.setText(UserKeyAlignment.TopMiddle, "Gripper1: switch");
            button1.setEnabled(true);

            button2 = keyBar.addUserKey(1, this, true);
            button2.setText(UserKeyAlignment.TopMiddle, "Gripper2: switch");
            button2.setEnabled(true);

            button3 = keyBar.addUserKey(2, this, true);
            button3.setText(UserKeyAlignment.TopMiddle, "Gripper3: switch");
            button3.setEnabled(true);

            button4 = keyBar.addUserKey(3, this, true);
            button4.setText(UserKeyAlignment.TopMiddle, "Null");
            button4.setEnabled(true);

            keyBar.publish();
            log.info("HMI programmable buttons initialized successfully");
        } catch (Exception e)
        {
            log.error("Failed to register user keys: " + e.getMessage(), e);
        }
    }

    @Override
    public void onKeyEvent(IUserKey key, UserKeyEvent event)
    {
        try
        {
            if (event == UserKeyEvent.KeyDown)
            {
                handleKeyDown(key);
            }
            // If you need KeyUp events (e.g. for hold-to-unlock like iiwaTOFAS Button 2),
            // add: else if (event == UserKeyEvent.KeyUp) { handleKeyUp(key); }
        } catch (Exception e)
        {
            log.error("Error handling key event: " + e.getMessage(), e);
        }
    }

    private void handleKeyDown(IUserKey key)
    {
        if (key == button1)
        {
            handleGripper1Toggle();
        } else if (key == button2)
        {
            handleGripper2Toggle();
        } else if (key == button3)
        {
            handleGripper3Toggle();
        } else if (key == button4)
        {
            handleButton4Press();
        }
    }

    private void handleGripper1Toggle()
    {
        try
        {
            if (gripperOpen)
            {
                // Close gripper - activate digital output
                gripperIO.setGripper1_Switch(true);
                gripperOpen = false;
                button1.setText(UserKeyAlignment.TopMiddle, "Gripper 1: Closed");
                log.debug("HMI Button 1: Gripper closed");
            } else
            {
                // Open gripper - deactivate digital output
                gripperIO.setGripper1_Switch(false);
                gripperOpen = true;
                button1.setText(UserKeyAlignment.TopMiddle, "Gripper 1: Open");
                log.debug("HMI Button 1: Gripper opened");
            }
        } catch (Exception e)
        {
            log.error("Error toggling gripper: " + e.getMessage(), e);
            button1.setText(UserKeyAlignment.TopMiddle, "Gripper 1 ERROR");
        }
    }

    private void handleGripper2Toggle()
    {
        try
        {
            if (gripperOpen)
            {
                // Close gripper - activate digital output
                gripperIO.setGripper2_Switch(true);
                gripperOpen = false;
                button1.setText(UserKeyAlignment.TopMiddle, "Gripper 2: Closed");
                log.debug("HMI Button 2: Gripper closed");
            } else
            {
                // Open gripper - deactivate digital output
                gripperIO.setGripper2_Switch(false);
                gripperOpen = true;
                button1.setText(UserKeyAlignment.TopMiddle, "Gripper 2: Open");
                log.debug("HMI Button 2: Gripper opened");
            }
        } catch (Exception e)
        {
            log.error("Error toggling gripper: " + e.getMessage(), e);
            button1.setText(UserKeyAlignment.TopMiddle, "Gripper 2 ERROR");
        }
    }

    /**
     * Button 3: Print current robot position to console (joint + Cartesian).
     * Useful for teaching points and debugging.
     */
    private void handleGripper3Toggle()
    {
        try
        {
            if (gripperOpen)
            {
                // Close gripper - activate digital output
                gripperIO.setGripper2_Switch(true);
                gripperOpen = false;
                button1.setText(UserKeyAlignment.TopMiddle, "Gripper 3: Closed");
                log.debug("HMI Button 2: Gripper closed");
            } else
            {
                // Open gripper - deactivate digital output
                gripperIO.setGripper2_Switch(false);
                gripperOpen = true;
                button1.setText(UserKeyAlignment.TopMiddle, "Gripper 3: Open");
                log.debug("HMI Button 2: Gripper opened");
            }
        } catch (Exception e)
        {
            log.error("Error toggling gripper: " + e.getMessage(), e);
            button1.setText(UserKeyAlignment.TopMiddle, "Gripper 3 ERROR");
        }

    }

    /**
     * Button 4: Reserved for future use.
     */
    private void handleButton4Press()
    {
        log.info("HMI Button 4 pressed (reserved)");
    }
}