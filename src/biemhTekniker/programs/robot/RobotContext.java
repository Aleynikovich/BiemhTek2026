package biemhTekniker.programs.robot;

import biemhTekniker.lib.config.FrameRepository;
import biemhTekniker.lib.data.WorkpieceQueue;
import biemhTekniker.lib.logger.Logger;
import biemhTekniker.lib.vision.SmartPickingProtocol;
import com.kuka.generated.ioAccess.AutExtIOGroup;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.motionModel.IMotionContainer;

/**
 * Context object providing dependencies for robot programs.
 * Holds references to robot hardware and shared data structures.
 * Also manages active motion containers for cancellation support.
 */
public class RobotContext
{
    private static final Logger log = Logger.getLogger(RobotContext.class);
    
    private final LBR robot;
    private final Tool gripper;
    private final MediaFlangeIOGroup gripperIO;
    private final AutExtIOGroup autExtIO;
    private final RoboticsAPIApplication application;
    private final WorkpieceQueue workpieceQueue;
    private final FrameRepository frameRepository;
    private SmartPickingProtocol protocol;
    private volatile boolean cancellationRequested = false;
    private volatile IMotionContainer activeMotion = null;

    /**
     * Creates a new robot context.
     *
     * @param robot          LBR robot instance
     * @param gripper        Gripper tool
     * @param gripperIO      Gripper I/O group
     * @param autExtIO       AutExt I/O group for PLC communication
     * @param application    Main application instance (for getFrame, getApplicationData, etc.)
     * @param workpieceQueue Shared workpiece queue
     * @param frameRepository Frame repository for accessing station frames
     */
    public RobotContext(LBR robot, Tool gripper, MediaFlangeIOGroup gripperIO, AutExtIOGroup autExtIO, RoboticsAPIApplication application, WorkpieceQueue workpieceQueue, FrameRepository frameRepository)
    {
        this.robot = robot;
        this.gripper = gripper;
        this.gripperIO = gripperIO;
        this.autExtIO = autExtIO;
        this.application = application;
        this.workpieceQueue = workpieceQueue;
        this.frameRepository = frameRepository;
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

    public AutExtIOGroup getAutExtIO()
    {
        return autExtIO;
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
     * Gets the frame repository for accessing station frames.
     *
     * @return Frame repository
     */
    public FrameRepository getFrameRepository()
    {
        return frameRepository;
    }

    /**
     * Sets the SmartPicking protocol for camera interaction.
     * Optional - only needed for programs that directly interact with the camera.
     *
     * @param protocol SmartPicking protocol instance
     */
    public void setProtocol(SmartPickingProtocol protocol)
    {
        this.protocol = protocol;
    }

    /**
     * Gets the SmartPicking protocol for camera interaction.
     * May be null if not set.
     *
     * @return SmartPicking protocol instance or null
     */
    public SmartPickingProtocol getProtocol()
    {
        return protocol;
    }

    /**
     * Requests cancellation of the current program execution.
     * Programs should check this flag periodically and exit gracefully.
     * Also cancels any active motion immediately.
     */
    public void requestCancellation()
    {
        cancellationRequested = true;
        cancelActiveMotion();
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
        activeMotion = null;
    }
    
    /**
     * Sets the currently active motion container.
     * This allows the context to cancel the motion if cancellation is requested.
     *
     * @param motion Active motion container (null to clear)
     */
    public void setActiveMotion(IMotionContainer motion)
    {
        this.activeMotion = motion;
    }
    
    /**
     * Cancels the currently active motion if one is running.
     */
    public void cancelActiveMotion()
    {
        IMotionContainer motion = this.activeMotion;
        if (motion != null)
        {
            try
            {
                log.info("Cancelling active motion");
                motion.cancel();
                this.activeMotion = null;
            } catch (Exception e)
            {
                log.warn("Failed to cancel active motion: " + e.getMessage());
            }
        }
    }
}
