package biemhTekniker.toolcontrol;

import biemhTekniker.logger.Logger;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.generated.ioAccess.SchunkGripperIOGroup;

import javax.inject.Inject;

/**
 * Tool control class to manage gripper operations.
 * Provides unified interface for controlling multiple grippers on the robot.
 */
public class ToolControl
{
    private static final Logger log = Logger.getLogger(ToolControl.class);

    // Timeout for gripper operations in milliseconds
    private static final long GRIPPER_TIMEOUT_MS = 5000;

    // Media Flange IO Group for grippers 1 and 2
    private final MediaFlangeIOGroup mediaFlangeIO;

    // Schunk Gripper IO Group (future implementation)
    private final SchunkGripperIOGroup schunkGripperIO;

    /**
     * Constructor with dependency injection for IO groups.
     *
     * @param mediaFlangeIO   MediaFlange IO group for robot grippers
     * @param schunkGripperIO Schunk gripper IO group (dummy for now)
     */
    @Inject
    public ToolControl(MediaFlangeIOGroup mediaFlangeIO, SchunkGripperIOGroup schunkGripperIO)
    {
        this.mediaFlangeIO   = mediaFlangeIO;
        this.schunkGripperIO = schunkGripperIO;
    }

    /**
     * Opens gripper 1 (MediaFlange).
     * Sends open command and waits for confirmation.
     *
     * @return true if gripper opened successfully, false otherwise
     */
    public boolean openGripper1()
    {
        log.info("Opening gripper 1...");
        mediaFlangeIO.setGripper1_Switch(true);

        return waitForGripperState(1, true);
    }

    /**
     * Closes gripper 1 (MediaFlange).
     * Sends close command and waits for confirmation.
     *
     * @return true if gripper closed successfully, false otherwise
     */
    public boolean closeGripper1()
    {
        log.info("Closing gripper 1...");
        mediaFlangeIO.setGripper1_Switch(false);

        return waitForGripperState(1, false);
    }

    /**
     * Toggles gripper 1 state.
     * Opens if closed, closes if open.
     *
     * @return true if operation succeeded, false otherwise
     */
    public boolean toggleGripper1()
    {
        boolean currentState = mediaFlangeIO.getGripper1_Switch();
        if (currentState)
        {
            return closeGripper1();
        }
        else
        {
            return openGripper1();
        }
    }

    /**
     * Opens gripper 2 (MediaFlange).
     * Sends open command and waits for confirmation.
     *
     * @return true if gripper opened successfully, false otherwise
     */
    public boolean openGripper2()
    {
        log.info("Opening gripper 2...");
        mediaFlangeIO.setGripper2_Switch(true);

        return waitForGripperState(2, true);
    }

    /**
     * Closes gripper 2 (MediaFlange).
     * Sends close command and waits for confirmation.
     *
     * @return true if gripper closed successfully, false otherwise
     */
    public boolean closeGripper2()
    {
        log.info("Closing gripper 2...");
        mediaFlangeIO.setGripper2_Switch(false);

        return waitForGripperState(2, false);
    }

    /**
     * Toggles gripper 2 state.
     * Opens if closed, closes if open.
     *
     * @return true if operation succeeded, false otherwise
     */
    public boolean toggleGripper2()
    {
        boolean currentState = mediaFlangeIO.getGripper2_Switch();
        if (currentState)
        {
            return closeGripper2();
        }
        else
        {
            return openGripper2();
        }
    }

    /**
     * Opens Schunk gripper.
     * DUMMY IMPLEMENTATION - To be completed when hardware is connected.
     *
     * @return true if gripper opened successfully, false otherwise
     */
    public boolean openSchunkGripper()
    {
        log.warn("Schunk gripper open - DUMMY IMPLEMENTATION");
        // TODO: Uncomment when Schunk gripper is connected
        // schunkGripperIO.setSchunkGripper_Switch(true);
        // return waitForSchunkGripperState(true);
        return true;
    }

    /**
     * Closes Schunk gripper.
     * DUMMY IMPLEMENTATION - To be completed when hardware is connected.
     *
     * @return true if gripper closed successfully, false otherwise
     */
    public boolean closeSchunkGripper()
    {
        log.warn("Schunk gripper close - DUMMY IMPLEMENTATION");
        // TODO: Uncomment when Schunk gripper is connected
        // schunkGripperIO.setSchunkGripper_Switch(false);
        // return waitForSchunkGripperState(false);
        return true;
    }

    /**
     * Toggles Schunk gripper state.
     * DUMMY IMPLEMENTATION - To be completed when hardware is connected.
     *
     * @return true if operation succeeded, false otherwise
     */
    public boolean toggleSchunkGripper()
    {
        log.warn("Schunk gripper toggle - DUMMY IMPLEMENTATION");
        // TODO: Uncomment when Schunk gripper is connected
        // boolean currentState = schunkGripperIO.getSchunkGripper_Switch();
        // if (currentState)
        // {
        //     return closeSchunkGripper();
        // }
        // else
        // {
        //     return openSchunkGripper();
        // }
        return true;
    }

    /**
     * Gets the current state of gripper 1.
     *
     * @return true if gripper is open, false if closed
     */
    public boolean isGripper1Open()
    {
        return mediaFlangeIO.getGripper1_isOpen();
    }

    /**
     * Gets the current state of gripper 2.
     *
     * @return true if gripper is open, false if closed
     */
    public boolean isGripper2Open()
    {
        return mediaFlangeIO.getGripper2_isOpen();
    }

    /**
     * Waits for gripper to reach desired state with timeout.
     *
     * @param gripperNumber Gripper number (1 or 2)
     * @param open          true to wait for open state, false for closed state
     * @return true if desired state reached, false if timeout
     */
    private boolean waitForGripperState(int gripperNumber, boolean open)
    {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < GRIPPER_TIMEOUT_MS)
        {
            boolean currentState;
            if (gripperNumber == 1)
            {
                currentState = open ? mediaFlangeIO.getGripper1_isOpen() : mediaFlangeIO.getGripper1_isClosed();
            }
            else if (gripperNumber == 2)
            {
                currentState = open ? mediaFlangeIO.getGripper2_isOpen() : mediaFlangeIO.getGripper2_isClosed();
            }
            else
            {
                log.error("Invalid gripper number: " + gripperNumber);
                return false;
            }

            if (currentState)
            {
                log.info("Gripper " + gripperNumber + " reached " + (open ? "open" : "closed") + " state");
                return true;
            }

            try
            {
                Thread.sleep(50);
            }
            catch (InterruptedException e)
            {
                log.warn("Wait interrupted for gripper " + gripperNumber);
                Thread.currentThread().interrupt();
                return false;
            }
        }

        log.error("Timeout waiting for gripper " + gripperNumber + " to " + (open ? "open" : "close"));
        return false;
    }

    /**
     * Waits for Schunk gripper to reach desired state with timeout.
     * DUMMY IMPLEMENTATION - To be completed when hardware is connected.
     *
     * @param open true to wait for open state, false for closed state
     * @return true if desired state reached, false if timeout
     */
    private boolean waitForSchunkGripperState(boolean open)
    {
        // TODO: Implement when Schunk gripper is connected
        // long startTime = System.currentTimeMillis();
        //
        // while (System.currentTimeMillis() - startTime < GRIPPER_TIMEOUT_MS)
        // {
        //     boolean currentState = open ? schunkGripperIO.getSchunkGripper_isOpen() : schunkGripperIO.getSchunkGripper_isClosed();
        //
        //     if (currentState)
        //     {
        //         log.info("Schunk gripper reached " + (open ? "open" : "closed") + " state");
        //         return true;
        //     }
        //
        //     try
        //     {
        //         Thread.sleep(50);
        //     }
        //     catch (InterruptedException e)
        //     {
        //         log.warn("Wait interrupted for Schunk gripper");
        //         Thread.currentThread().interrupt();
        //         return false;
        //     }
        // }
        //
        // log.error("Timeout waiting for Schunk gripper to " + (open ? "open" : "close"));
        return false;
    }
}
