package biemhTekniker.IO;

import com.kuka.roboticsAPI.uiModel.userKeys.*;

/**
 * Handles HMI programmable button events on the KUKA SmartPad.
 * Implements button functionality for manual gripper control.
 * Separates HMI logic from gripper business logic for better testability.
 */
public class HmiButtonHandler implements IUserKeyListener
{

    private final GripperController gripperController;

    private IUserKey btnGripper1Open;
    private IUserKey btnGripper1Close;
    private IUserKey btnGripper2Open;
    private IUserKey btnGripper2Close;

    /**
     * Creates a new HMI button handler.
     *
     * @param gripperController The gripper controller for executing gripper commands
     */
    public HmiButtonHandler(GripperController gripperController)
    {
        this.gripperController = gripperController;
    }

    /**
     * Registers the user keys with the HMI user key bar.
     * This must be called during application initialization.
     *
     * @param keyBar The user key bar from getApplicationUI().createUserKeyBar()
     */
    public void registerUserKeys(IUserKeyBar keyBar)
    {
        btnGripper1Open = keyBar.addUserKey(0, this, true);
        btnGripper1Open.setText(UserKeyAlignment.TopMiddle, "G1 Open");

        btnGripper1Close = keyBar.addUserKey(1, this, true);
        btnGripper1Close.setText(UserKeyAlignment.TopMiddle, "G1 Close");

        btnGripper2Open = keyBar.addUserKey(2, this, true);
        btnGripper2Open.setText(UserKeyAlignment.TopMiddle, "G2 Open");

        btnGripper2Close = keyBar.addUserKey(3, this, true);
        btnGripper2Close.setText(UserKeyAlignment.TopMiddle, "G2 Close");

        keyBar.publish();
    }

    @Override
    public void onKeyEvent(IUserKey key, UserKeyEvent event)
    {
        if (event == UserKeyEvent.KeyDown)
        {
            handleKeyDown(key);
        }
    }

    private void handleKeyDown(IUserKey key)
    {
        if (key == btnGripper1Open)
        {
            gripperController.openGripper1();
        } else if (key == btnGripper1Close)
        {
            gripperController.closeGripper1();
        } else if (key == btnGripper2Open)
        {
            gripperController.openGripper2();
        } else if (key == btnGripper2Close)
        {
            gripperController.closeGripper2();
        }
    }
}
