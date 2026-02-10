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
            if (gripperIO.getGripper1_Switch())
            {
                button1.setText(UserKeyAlignment.TopMiddle, "Gripper1: closed");
            } else
            {
                button1.setText(UserKeyAlignment.TopMiddle, "Gripper1: open");
            }
            button1.setEnabled(true);

            button2 = keyBar.addUserKey(1, this, true);
            if (gripperIO.getGripper2_Switch())
            {
                button2.setText(UserKeyAlignment.TopMiddle, "Gripper2: switch");
            } else
            {
                button2.setText(UserKeyAlignment.TopMiddle, "Gripper2: closed");
            }
            button2.setEnabled(true);

            button3 = keyBar.addUserKey(2, this, true);
            if (gripperIO.getGripper2_Switch())
            {
                button3.setText(UserKeyAlignment.TopMiddle, "3: switch");
            } else
            {
                button3.setText(UserKeyAlignment.TopMiddle, "Gripper3: closed");
            }
            button3.setEnabled(true);

            button4 = keyBar.addUserKey(3, this, true);
            if (gripperIO.getGripper2_Switch())
            {
                button4.setText(UserKeyAlignment.TopMiddle, "SecSwitch: ON");
            } else
            {
                button4.setText(UserKeyAlignment.TopMiddle, "SecSwitch: OFF");
            }
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
            if (!gripperIO.getGripper1_Switch())
            {
                // Close gripper - activate digital output
                gripperIO.setGripper1_Switch(true);
                button1.setText(UserKeyAlignment.TopMiddle, "Gripper 1: Closed");
                log.debug("HMI Button 1: Gripper closed");
            } else
            {
                // Open gripper - deactivate digital output
                gripperIO.setGripper1_Switch(false);
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
            if (!gripperIO.getGripper2_Switch())
            {
                // Close gripper - activate digital output
                gripperIO.setGripper2_Switch(true);
                button2.setText(UserKeyAlignment.TopMiddle, "Gripper 2: Closed");
                log.debug("HMI Button 2: Gripper closed");
            } else
            {
                // Open gripper - deactivate digital output
                gripperIO.setGripper2_Switch(false);
                button2.setText(UserKeyAlignment.TopMiddle, "Gripper 2: Open");
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
            if (!gripperIO.getGripper3_Switch())
            {
                // Close gripper - activate digital output
                gripperIO.setGripper3_Switch(true);
                button3.setText(UserKeyAlignment.TopMiddle, "Gripper 3: Closed");
                log.debug("HMI Button 3: Gripper closed");
            } else
            {
                // Open gripper - deactivate digital output
                gripperIO.setGripper3_Switch(false);
                button3.setText(UserKeyAlignment.TopMiddle, "Gripper 3: Open");
                log.debug("HMI Button 3: Gripper opened");
            }
        } catch (Exception e)
        {
            log.error("Error toggling gripper: " + e.getMessage(), e);
            button1.setText(UserKeyAlignment.TopMiddle, "Gripper 2 ERROR");
        }
    }

    /**
     * Button 4: Reserved for future use.
     */
    private void handleButton4Press()
    {
        log.debug("HMI Button 4 pressed (Security Switch)");
        try
        {
            if (!gripperIO.getSecuritySwitch())
            {
                gripperIO.setSecuritySwitch(true);
                button4.setText(UserKeyAlignment.TopMiddle, "SecuritySwitch: ON");
                log.debug("HMI Button 2: Gripper closed");
            } else
            {
                gripperIO.setSecuritySwitch(false);
                button4.setText(UserKeyAlignment.TopMiddle, "SecuritySwitch: OFF");
                log.debug("HMI Button 2: Gripper opened");
            }
        } catch (Exception e)
        {
            log.error("Error toggling security: " + e.getMessage(), e);
            button4.setText(UserKeyAlignment.TopMiddle, "Security switch ERROR");
        }
    }
}