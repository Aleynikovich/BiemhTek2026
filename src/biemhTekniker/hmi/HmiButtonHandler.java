package biemhTekniker.hmi;

import biemhTekniker.logger.Logger;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.uiModel.userKeys.*;

/**
 * Handles HMI programmable button events on the KUKA SmartPad.
 * Uses a configurable button action system for flexibility.
 * Supports multiple user key bars for different operation modes.
 */
public class HmiButtonHandler implements IUserKeyListener
{
    private static final Logger log = Logger.getLogger(HmiButtonHandler.class);

    private final LBR robot;
    private final Tool gripper;
    private final MediaFlangeIOGroup gripperIO;

    // User key references
    private IUserKey[] buttons = new IUserKey[4];

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
            // Button 0: Gripper 1
            buttons[0] = keyBar.addUserKey(0, this, true);
            updateGripperButtonText(buttons[0], 1);
            buttons[0].setEnabled(true);

            // Button 1: Gripper 2
            buttons[1] = keyBar.addUserKey(1, this, true);
            updateGripperButtonText(buttons[1], 2);
            buttons[1].setEnabled(true);

            // Button 2: Gripper 3
            buttons[2] = keyBar.addUserKey(2, this, true);
            updateGripperButtonText(buttons[2], 3);
            buttons[2].setEnabled(true);

            // Button 3: Security Switch
            buttons[3] = keyBar.addUserKey(3, this, true);
            updateSecurityButtonText(buttons[3]);
            buttons[3].setEnabled(true);

            keyBar.publish();
            log.info("HMI programmable buttons initialized successfully");
        } catch (Exception e)
        {
            log.error("Failed to register user keys: " + e.getMessage(), e);
        }
    }

    /**
     * Updates the button text for a gripper button based on current state.
     *
     * @param button Button to update
     * @param gripperNum Gripper number (1, 2, or 3)
     */
    private void updateGripperButtonText(IUserKey button, int gripperNum)
    {
        boolean isActive = false;
        String label = "Gripper " + gripperNum + ": ";

        switch (gripperNum)
        {
            case 1:
                isActive = gripperIO.getGripper1_Switch();
                break;
            case 2:
                isActive = gripperIO.getGripper2_Switch();
                break;
            case 3:
                isActive = gripperIO.getGripper3_Switch();
                break;
        }

        label += isActive ? "Closed" : "Open";
        button.setText(UserKeyAlignment.TopMiddle, label);
    }

    /**
     * Updates the button text for the security switch button.
     *
     * @param button Button to update
     */
    private void updateSecurityButtonText(IUserKey button)
    {
        boolean isActive = gripperIO.getSecuritySwitch();
        String label = "SecSwitch: " + (isActive ? "ON" : "OFF");
        button.setText(UserKeyAlignment.TopMiddle, label);
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
        // Find which button was pressed
        for (int i = 0; i < buttons.length; i++)
        {
            if (key == buttons[i])
            {
                handleButtonPress(i);
                return;
            }
        }
    }

    /**
     * Handles button press for a specific button index.
     *
     * @param buttonIndex Button index (0-3)
     */
    private void handleButtonPress(int buttonIndex)
    {
        switch (buttonIndex)
        {
            case 0:
                toggleGripper(1, buttons[0]);
                break;
            case 1:
                toggleGripper(2, buttons[1]);
                break;
            case 2:
                toggleGripper(3, buttons[2]);
                break;
            case 3:
                toggleSecuritySwitch(buttons[3]);
                break;
        }
    }

    /**
     * Toggles a gripper and updates button text.
     *
     * @param gripperNum Gripper number (1, 2, or 3)
     * @param button Button to update
     */
    private void toggleGripper(int gripperNum, IUserKey button)
    {
        try
        {
            boolean currentState = false;
            boolean newState;

            // Get current state
            switch (gripperNum)
            {
                case 1:
                    currentState = gripperIO.getGripper1_Switch();
                    break;
                case 2:
                    currentState = gripperIO.getGripper2_Switch();
                    break;
                case 3:
                    currentState = gripperIO.getGripper3_Switch();
                    break;
            }

            newState = !currentState;

            // Set new state
            switch (gripperNum)
            {
                case 1:
                    gripperIO.setGripper1_Switch(newState);
                    break;
                case 2:
                    gripperIO.setGripper2_Switch(newState);
                    break;
                case 3:
                    gripperIO.setGripper3_Switch(newState);
                    break;
            }

            updateGripperButtonText(button, gripperNum);
            log.debug("HMI: Gripper " + gripperNum + " " + (newState ? "closed" : "opened"));

        } catch (Exception e)
        {
            log.error("Error toggling gripper " + gripperNum + ": " + e.getMessage(), e);
            button.setText(UserKeyAlignment.TopMiddle, "Gripper " + gripperNum + " ERROR");
        }
    }

    /**
     * Toggles the security switch and updates button text.
     *
     * @param button Button to update
     */
    private void toggleSecuritySwitch(IUserKey button)
    {
        try
        {
            boolean currentState = gripperIO.getSecuritySwitch();
            boolean newState = !currentState;

            gripperIO.setSecuritySwitch(newState);
            updateSecurityButtonText(button);
            log.debug("HMI: Security switch " + (newState ? "ON" : "OFF"));

        } catch (Exception e)
        {
            log.error("Error toggling security switch: " + e.getMessage(), e);
            button.setText(UserKeyAlignment.TopMiddle, "Security switch ERROR");
        }
    }
}