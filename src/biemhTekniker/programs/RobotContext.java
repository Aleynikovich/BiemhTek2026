package biemhTekniker.programs;

import biemhTekniker.data.WorkpieceQueue;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;

/**
 * Context object providing dependencies for robot programs.
 * Holds references to robot hardware and shared data structures.
 */
public class RobotContext
{
    private final LBR robot;
    private final Tool gripper;
    private final MediaFlangeIOGroup gripperIO;
    private final RoboticsAPIApplication application;
    private final WorkpieceQueue workpieceQueue;
    private volatile boolean cancellationRequested = false;

    /**
     * Creates a new robot context.
     *
     * @param robot          LBR robot instance
     * @param gripper        Gripper tool
     * @param gripperIO      Gripper I/O group
     * @param application    Main application instance (for getFrame, getApplicationData, etc.)
     * @param workpieceQueue Shared workpiece queue
     */
    public RobotContext(LBR robot, Tool gripper, MediaFlangeIOGroup gripperIO, RoboticsAPIApplication application, WorkpieceQueue workpieceQueue)
    {
        this.robot = robot;
        this.gripper = gripper;
        this.gripperIO = gripperIO;
        this.application = application;
        this.workpieceQueue = workpieceQueue;
    }

    public LBR getRobot()
    {
        return robot;
    }

    public Tool getGripper()
    {
        return gripper;
    }

    public MediaFlangeIOGroup getGripperIO()
    {
        return gripperIO;
    }

    public RoboticsAPIApplication getApplication()
    {
        return application;
    }

    public WorkpieceQueue getWorkpieceQueue()
    {
        return workpieceQueue;
    }

    /**
     * Requests cancellation of the current program execution.
     * Programs should check this flag periodically and exit gracefully.
     */
    public void requestCancellation()
    {
        cancellationRequested = true;
    }

    /**
     * Checks if cancellation has been requested.
     *
     * @return true if cancellation was requested
     */
    public boolean isCancellationRequested()
    {
        return cancellationRequested;
    }

    /**
     * Clears the cancellation flag.
     * Should be called after cancellation is handled or before starting a new program.
     */
    public void clearCancellation()
    {
        cancellationRequested = false;
    }
}
