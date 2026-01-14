package hardware;

import com.kuka.generated.ioAccess.Gripper1IOGroup;
import com.kuka.generated.ioAccess.Gripper2IOGroup;

import common.ILogger;

/**
 * Controller for Gripper 1 and Gripper 2.
 * Provides methods to control grippers via Profinet I/O.
 * HMI buttons can be added by application using getApplicationData().
 * 
 * Java 1.7 compatible - no lambdas, no diamond operators
 */
public class GripperController {
    
    private Gripper1IOGroup gripper1IO;
    private Gripper2IOGroup gripper2IO;
    private ILogger logger;
    
    /**
     * Create gripper controller
     * @param gripper1IO Gripper 1 I/O group
     * @param gripper2IO Gripper 2 I/O group
     * @param logger Task logger
     */
    public GripperController(Gripper1IOGroup gripper1IO, Gripper2IOGroup gripper2IO, ILogger logger) {
        this.gripper1IO = gripper1IO;
        this.gripper2IO = gripper2IO;
        this.logger = logger;
    }
    
    /**
     * Open Gripper 1
     */
    public void openGripper1() {
        gripper1IO.setClose(Boolean.valueOf(false));
        gripper1IO.setOpen(Boolean.valueOf(true));
        logger.info("Gripper 1: OPEN");
    }
    
    /**
     * Close Gripper 1
     */
    public void closeGripper1() {
        gripper1IO.setOpen(Boolean.valueOf(false));
        gripper1IO.setClose(Boolean.valueOf(true));
        logger.info("Gripper 1: CLOSE");
    }
    
    /**
     * Open Gripper 2
     */
    public void openGripper2() {
        gripper2IO.setClose(Boolean.valueOf(false));
        gripper2IO.setOpen(Boolean.valueOf(true));
        logger.info("Gripper 2: OPEN");
    }
    
    /**
     * Close Gripper 2
     */
    public void closeGripper2() {
        gripper2IO.setOpen(Boolean.valueOf(false));
        gripper2IO.setClose(Boolean.valueOf(true));
        logger.info("Gripper 2: CLOSE");
    }
    
    /**
     * Wait for Gripper 1 to reach open position
     * @param timeoutMs Timeout in milliseconds
     * @return true if gripper opened, false if timeout
     */
    public boolean waitForGripper1Open(long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (!gripper1IO.getIsOpen()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return false;
            }
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                logger.warn("Gripper 1 open timeout");
                return false;
            }
        }
        return true;
    }
    
    /**
     * Wait for Gripper 1 to reach closed position
     * @param timeoutMs Timeout in milliseconds
     * @return true if gripper closed, false if timeout
     */
    public boolean waitForGripper1Close(long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (!gripper1IO.getIsClosed()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return false;
            }
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                logger.warn("Gripper 1 close timeout");
                return false;
            }
        }
        return true;
    }
    
    /**
     * Wait for Gripper 2 to reach open position
     * @param timeoutMs Timeout in milliseconds
     * @return true if gripper opened, false if timeout
     */
    public boolean waitForGripper2Open(long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (!gripper2IO.getIsOpen()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return false;
            }
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                logger.warn("Gripper 2 open timeout");
                return false;
            }
        }
        return true;
    }
    
    /**
     * Wait for Gripper 2 to reach closed position
     * @param timeoutMs Timeout in milliseconds
     * @return true if gripper closed, false if timeout
     */
    public boolean waitForGripper2Close(long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (!gripper2IO.getIsClosed()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return false;
            }
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                logger.warn("Gripper 2 close timeout");
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if Gripper 1 is fully open
     * @return true if open
     */
    public boolean isGripper1Open() {
        return gripper1IO.getIsOpen();
    }
    
    /**
     * Check if Gripper 1 is fully closed
     * @return true if closed
     */
    public boolean isGripper1Closed() {
        return gripper1IO.getIsClosed();
    }
    
    /**
     * Check if Gripper 2 is fully open
     * @return true if open
     */
    public boolean isGripper2Open() {
        return gripper2IO.getIsOpen();
    }
    
    /**
     * Check if Gripper 2 is fully closed
     * @return true if closed
     */
    public boolean isGripper2Closed() {
        return gripper2IO.getIsClosed();
    }
}
