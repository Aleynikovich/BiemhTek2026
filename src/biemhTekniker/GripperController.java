package biemhTekniker;

import javax.inject.Inject;

import com.kuka.generated.ioAccess.Gripper1IOGroup;
import com.kuka.generated.ioAccess.Gripper2IOGroup;
import com.kuka.roboticsAPI.uiModel.IApplicationData;
import com.kuka.roboticsAPI.uiModel.userKeys.IUserKey;
import com.kuka.roboticsAPI.uiModel.userKeys.IUserKeyBar;
import com.kuka.roboticsAPI.uiModel.userKeys.IUserKeyListener;
import com.kuka.roboticsAPI.uiModel.userKeys.UserKeyAlignment;
import com.kuka.roboticsAPI.uiModel.userKeys.UserKeyEvent;

/**
 * Controller for Gripper 1 and Gripper 2 with HMI button support.
 */
public class GripperController {
    
    @Inject
    private Gripper1IOGroup gripper1IO;
    
    @Inject
    private Gripper2IOGroup gripper2IO;
    
    @Inject
    private IApplicationData appData;
    
    private IUserKeyBar keyBar;
    private IUserKey btnGripper1Open;
    private IUserKey btnGripper1Close;
    private IUserKey btnGripper2Open;
    private IUserKey btnGripper2Close;
    
    public void initializeHMI() {
        keyBar = appData.processDataManager().getUserKeyBar(UserKeyAlignment.TopMiddle);
        
        btnGripper1Open = keyBar.addUserKey(0, btnGripper1OpenListener, true);
        btnGripper1Open.setText(UserKeyAlignment.TopMiddle, "G1 Open");
        
        btnGripper1Close = keyBar.addUserKey(1, btnGripper1CloseListener, true);
        btnGripper1Close.setText(UserKeyAlignment.TopMiddle, "G1 Close");
        
        btnGripper2Open = keyBar.addUserKey(2, btnGripper2OpenListener, true);
        btnGripper2Open.setText(UserKeyAlignment.TopMiddle, "G2 Open");
        
        btnGripper2Close = keyBar.addUserKey(3, btnGripper2CloseListener, true);
        btnGripper2Close.setText(UserKeyAlignment.TopMiddle, "G2 Close");
        
        keyBar.publish();
    }
    
    private IUserKeyListener btnGripper1OpenListener = new IUserKeyListener() {
        public void onKeyEvent(IUserKey key, UserKeyEvent event) {
            if (event == UserKeyEvent.KeyDown) {
                openGripper1();
            }
        }
    };
    
    private IUserKeyListener btnGripper1CloseListener = new IUserKeyListener() {
        public void onKeyEvent(IUserKey key, UserKeyEvent event) {
            if (event == UserKeyEvent.KeyDown) {
                closeGripper1();
            }
        }
    };
    
    private IUserKeyListener btnGripper2OpenListener = new IUserKeyListener() {
        public void onKeyEvent(IUserKey key, UserKeyEvent event) {
            if (event == UserKeyEvent.KeyDown) {
                openGripper2();
            }
        }
    };
    
    private IUserKeyListener btnGripper2CloseListener = new IUserKeyListener() {
        public void onKeyEvent(IUserKey key, UserKeyEvent event) {
            if (event == UserKeyEvent.KeyDown) {
                closeGripper2();
            }
        }
    };
    
    public void openGripper1() {
        gripper1IO.setClose(false);
        gripper1IO.setOpen(true);
    }
    
    public void closeGripper1() {
        gripper1IO.setOpen(false);
        gripper1IO.setClose(true);
    }
    
    public void openGripper2() {
        gripper2IO.setClose(false);
        gripper2IO.setOpen(true);
    }
    
    public void closeGripper2() {
        gripper2IO.setOpen(false);
        gripper2IO.setClose(true);
    }
    
    public boolean waitForGripper1Open(long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (!gripper1IO.getIsOpen()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return false;
            }
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                return false;
            }
        }
        return true;
    }
    
    public boolean waitForGripper1Close(long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (!gripper1IO.getIsClosed()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return false;
            }
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                return false;
            }
        }
        return true;
    }
    
    public boolean waitForGripper2Open(long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (!gripper2IO.getIsOpen()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return false;
            }
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                return false;
            }
        }
        return true;
    }
    
    public boolean waitForGripper2Close(long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (!gripper2IO.getIsClosed()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return false;
            }
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isGripper1Open() {
        return gripper1IO.getIsOpen();
    }
    
    public boolean isGripper1Closed() {
        return gripper1IO.getIsClosed();
    }
    
    public boolean isGripper2Open() {
        return gripper2IO.getIsOpen();
    }
    
    public boolean isGripper2Closed() {
        return gripper2IO.getIsClosed();
    }
}
