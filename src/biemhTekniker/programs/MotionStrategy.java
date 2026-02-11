package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.deviceModel.LBRE1Redundancy;
import com.kuka.roboticsAPI.executionModel.CommandInvalidException;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.math.Transformation;
import com.kuka.roboticsAPI.motionModel.IMotionContainer;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.lin;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

/**
 * Generic motion strategy with support for alternate position (180° rotation) 
 * and redundancy (null space) motion.
 * Can be used for any robot motion operation (pick, place, etc.).
 */
public class MotionStrategy
{
    private static final Logger log = Logger.getLogger(MotionStrategy.class);

    private static final double APPROACH_VELOCITY = 0.5;
    private static final double ACTION_VELOCITY = 0.25;

    private final ObjectFrame tcp;
    private final boolean useAlternatePosition;
    private final Double redundancyE1Offset; // E1 offset in radians for null space motion (null if not used)
    private final LBR robot; // Robot instance needed for redundancy

    /**
     * Creates a motion strategy without redundancy.
     *
     * @param tcp                  Tool center point frame
     * @param useAlternatePosition If true, rotate position by 180 degrees around Z-axis
     */
    public MotionStrategy(ObjectFrame tcp, boolean useAlternatePosition)
    {
        this(tcp, useAlternatePosition, null, null);
    }

    /**
     * Creates a motion strategy with redundancy support.
     *
     * @param tcp                  Tool center point frame
     * @param useAlternatePosition If true, rotate position by 180 degrees around Z-axis
     * @param redundancyE1Offset   E1 offset in radians for null space motion (null to disable)
     * @param robot                Robot instance (required when redundancyE1Offset is not null)
     */
    public MotionStrategy(ObjectFrame tcp, boolean useAlternatePosition, 
                         Double redundancyE1Offset, LBR robot)
    {
        this.tcp = tcp;
        this.useAlternatePosition = useAlternatePosition;
        this.redundancyE1Offset = redundancyE1Offset;
        this.robot = robot;
    }

    /**
     * Attempts to execute a motion to target position with approach and retract.
     *
     * @param targetPosition   Target position for the action
     * @param approachPosition Approach position before action
     * @param action           Action to execute at target position (can be null for motion-only)
     * @param context          Robot context for cancellation support (can be null if cancellation not needed)
     * @return true if motion succeeded, false if motion failed
     */
    public boolean executeMotion(Frame targetPosition, Frame approachPosition, MotionAction action, RobotContext context)
    {
        Frame finalTarget = targetPosition;
        Frame finalApproach = approachPosition;

        // Apply alternate position transformation (180 degree rotation around Z-axis)
        if (useAlternatePosition)
        {
            // Create transformation: 180 degrees around Z-axis (alpha/C rotation)
            Transformation rotationZ180 = Transformation.ofRad(0, 0, 0, 0, 0, Math.PI);
            
            // Apply rotation to both frames
            finalTarget = new Frame(targetPosition.copy());
            finalTarget.transform(rotationZ180);
            
            finalApproach = new Frame(approachPosition.copy());
            finalApproach.transform(rotationZ180);
        }

        // Apply redundancy information if specified
        if (redundancyE1Offset != null && robot != null)
        {
            LBRE1Redundancy redundancy = new LBRE1Redundancy().setE1(redundancyE1Offset.doubleValue());
            finalTarget.setRedundancyInformation(robot, redundancy);
            finalApproach.setRedundancyInformation(robot, redundancy);
        }

        String strategyDesc = getDescription();

        try
        {
            log.info("Attempting motion with " + strategyDesc + ": " + finalTarget);

            // Approach
            IMotionContainer motionContainer = 
                tcp.moveAsync(ptp(finalApproach).setJointVelocityRel(APPROACH_VELOCITY));
            if (context != null)
            {
                context.setActiveMotion(motionContainer);
            }
            motionContainer.await();

            // Move to target position
            motionContainer = tcp.moveAsync(lin(finalTarget).setJointVelocityRel(ACTION_VELOCITY));
            if (context != null)
            {
                context.setActiveMotion(motionContainer);
            }
            motionContainer.await();

            // Check for cancellation before executing action
            if (context != null && context.isCancellationRequested())
            {
                log.warn("Motion cancelled before action execution");
                context.setActiveMotion(null);
                return false;
            }

            // Execute action at target position (e.g., activate/deactivate gripper)
            // The robot has already reached the target position (await() completed above)
            if (action != null)
            {
                action.execute();
            }

            // Check for cancellation before retract
            if (context != null && context.isCancellationRequested())
            {
                log.warn("Motion cancelled before retract");
                context.setActiveMotion(null);
                return false;
            }

            // Retract
            motionContainer = tcp.moveAsync(lin(finalApproach).setJointVelocityRel(ACTION_VELOCITY));
            if (context != null)
            {
                context.setActiveMotion(motionContainer);
            }
            motionContainer.await();
            
            if (context != null)
            {
                context.setActiveMotion(null);
            }

            log.info("Motion succeeded with " + strategyDesc);
            return true;
        } catch (CommandInvalidException e)
        {
            if (context != null)
            {
                context.setActiveMotion(null);
                // Check if cancellation was requested - if so, propagate as cancellation exception
                if (context.isCancellationRequested())
                {
                    log.warn("Motion failed due to cancellation: " + strategyDesc);
                    return false;
                }
            }
            log.warn("Motion failed with " + strategyDesc + ": " + e.getMessage());
            return false;
        } catch (Exception e)
        {
            if (context != null)
            {
                context.setActiveMotion(null);
                // Check if cancellation was requested - if so, propagate as cancellation exception
                if (context.isCancellationRequested())
                {
                    log.warn("Motion cancelled: " + strategyDesc);
                    return false;
                }
            }
            log.warn("Motion action failed with " + strategyDesc + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets a human-readable description of this strategy.
     */
    public String getDescription()
    {
        String desc = "tcp=" + tcp.getName()
            + (useAlternatePosition ? " (alternate)" : " (regular)")
            + (redundancyE1Offset != null ? " [E1=" + Math.toDegrees(redundancyE1Offset.doubleValue()) + "°]" : "");
        return desc;
    }

    @Override
    public String toString()
    {
        String redundancyStr = redundancyE1Offset != null ? ", E1=" + Math.toDegrees(redundancyE1Offset.doubleValue()) + "°" : "";
        return "MotionStrategy{tcp=" + tcp.getName() 
            + ", alternate=" + useAlternatePosition + redundancyStr + "}";
    }

    /**
     * Interface for actions to execute at the target position.
     */
    public interface MotionAction
    {
        /**
         * Execute the action at the target position.
         * @throws Exception if the action fails
         */
        void execute() throws Exception;
    }
}
