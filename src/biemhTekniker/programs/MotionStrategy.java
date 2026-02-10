package biemhTekniker.programs;

import biemhTekniker.config.ImpedanceConfig;
import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.deviceModel.LBRE1Redundancy;
import com.kuka.roboticsAPI.executionModel.CommandInvalidException;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.math.Transformation;
import com.kuka.roboticsAPI.motionModel.IMotionContainer;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.lin;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

/**
 * Generic motion strategy with support for alternate position (180° rotation),
 * Z-axis rotation freedom, redundancy (null space) motion, and impedance control.
 * Can be used for any robot motion operation (pick, place, etc.).
 * Supports tool coordinate system approach (Z+) for perpendicular approach to workpieces.
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
    private final boolean allowZRotation; // Allow free rotation around Z-axis
    private final Double zRotationAngle; // Specific Z-axis rotation angle in radians (null if allowZRotation is false)
    private final boolean useToolCoordinates; // Use tool coordinate system (Z+) for approach/retract
    private final CartesianImpedanceControlMode impedanceMode; // Impedance control for compliance (null if disabled)

    /**
     * Creates a motion strategy without redundancy.
     *
     * @param tcp                  Tool center point frame
     * @param useAlternatePosition If true, rotate position by 180 degrees around Z-axis
     */
    public MotionStrategy(ObjectFrame tcp, boolean useAlternatePosition)
    {
        this(tcp, useAlternatePosition, null, null, false, null, false, null);
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
        this(tcp, useAlternatePosition, redundancyE1Offset, robot, false, null, false, null);
    }

    /**
     * Creates a motion strategy with Z-axis rotation support.
     *
     * @param tcp                  Tool center point frame
     * @param useAlternatePosition If true, rotate position by 180 degrees around Z-axis
     * @param redundancyE1Offset   E1 offset in radians for null space motion (null to disable)
     * @param robot                Robot instance (required when redundancyE1Offset is not null)
     * @param allowZRotation       If true, apply Z-axis rotation for place operations
     * @param zRotationAngle       Z-axis rotation angle in radians (null for default 0)
     * @param useToolCoordinates   If true, use tool coordinate system (Z+) for approach/retract
     */
    public MotionStrategy(ObjectFrame tcp, boolean useAlternatePosition, 
                         Double redundancyE1Offset, LBR robot,
                         boolean allowZRotation, Double zRotationAngle, 
                         boolean useToolCoordinates)
    {
        this(tcp, useAlternatePosition, redundancyE1Offset, robot, allowZRotation, 
             zRotationAngle, useToolCoordinates, null);
    }

    /**
     * Creates a motion strategy with full feature support including impedance control.
     *
     * @param tcp                  Tool center point frame
     * @param useAlternatePosition If true, rotate position by 180 degrees around Z-axis
     * @param redundancyE1Offset   E1 offset in radians for null space motion (null to disable)
     * @param robot                Robot instance (required when redundancyE1Offset is not null)
     * @param allowZRotation       If true, apply Z-axis rotation for place operations
     * @param zRotationAngle       Z-axis rotation angle in radians (null for default 0)
     * @param useToolCoordinates   If true, use tool coordinate system (Z+) for approach/retract
     * @param impedanceMode        Cartesian impedance control mode for compliance (null to disable)
     */
    public MotionStrategy(ObjectFrame tcp, boolean useAlternatePosition, 
                         Double redundancyE1Offset, LBR robot,
                         boolean allowZRotation, Double zRotationAngle, 
                         boolean useToolCoordinates, CartesianImpedanceControlMode impedanceMode)
    {
        this.tcp = tcp;
        this.useAlternatePosition = useAlternatePosition;
        this.redundancyE1Offset = redundancyE1Offset;
        this.robot = robot;
        this.allowZRotation = allowZRotation;
        this.zRotationAngle = zRotationAngle;
        this.useToolCoordinates = useToolCoordinates;
        this.impedanceMode = impedanceMode;
    }

    /**
     * Attempts to execute a motion to target position with approach and retract.
     *
     * @param targetPosition   Target position for the action
     * @param approachOffset   Approach offset distance in mm (used with tool coordinates) or Frame (used without)
     * @param action           Action to execute at target position (can be null for motion-only)
     * @param context          Robot context for cancellation support (can be null if cancellation not needed)
     * @return true if motion succeeded, false if motion failed
     */
    public boolean executeMotion(Frame targetPosition, Object approachOffset, MotionAction action, RobotContext context)
    {
        Frame finalTarget = targetPosition;

        // Apply alternate position transformation (180 degree rotation around Z-axis)
        if (useAlternatePosition)
        {
            // Create transformation: 180 degrees around Z-axis (alpha/C rotation)
            Transformation rotationZ180 = Transformation.ofRad(0, 0, 0, 0, 0, Math.PI);
            
            // Apply rotation to target frame
            finalTarget = new Frame(targetPosition.copy());
            finalTarget.transform(rotationZ180);
        }

        // Apply Z-axis rotation if enabled
        if (allowZRotation && zRotationAngle != null)
        {
            Transformation rotationZ = Transformation.ofRad(0, 0, 0, 0, 0, zRotationAngle.doubleValue());
            Frame rotatedTarget = new Frame(finalTarget.copy());
            rotatedTarget.transform(rotationZ);
            finalTarget = rotatedTarget;
        }

        // Apply redundancy information if specified
        if (redundancyE1Offset != null && robot != null)
        {
            LBRE1Redundancy redundancy = new LBRE1Redundancy().setE1(redundancyE1Offset.doubleValue());
            finalTarget.setRedundancyInformation(robot, redundancy);
        }

        String strategyDesc = getDescription();

        try
        {
            log.info("Attempting motion with " + strategyDesc + ": " + finalTarget);

            if (useToolCoordinates)
            {
                // Use tool coordinate system for approach/retract (Z+ direction)
                double offsetMm = 0;
                if (approachOffset instanceof Double)
                {
                    offsetMm = ((Double) approachOffset).doubleValue();
                }
                else
                {
                    log.error("Invalid approach offset type for tool coordinates: " + approachOffset.getClass() + " (expected Double)");
                    return false;
                }

                // Create approach frame by applying offset in tool Z direction
                // The offset is applied in the local (tool) coordinate system
                // ofRad takes (x, y, z translation in mm, a, b, c rotation in radians)
                Transformation offsetTransform = Transformation.ofRad(0, 0, offsetMm, 0, 0, 0);
                Frame approachFrame = new Frame(finalTarget.copy());
                approachFrame.transform(offsetTransform);

                // Apply redundancy to approach frame if needed
                if (redundancyE1Offset != null && robot != null)
                {
                    LBRE1Redundancy redundancy = new LBRE1Redundancy().setE1(redundancyE1Offset.doubleValue());
                    approachFrame.setRedundancyInformation(robot, redundancy);
                }

                // Approach - move to position above target with PTP
                IMotionContainer motionContainer;
                if (impedanceMode != null)
                {
                    motionContainer = tcp.moveAsync(ptp(approachFrame).setJointVelocityRel(APPROACH_VELOCITY).setMode(impedanceMode));
                }
                else
                {
                    motionContainer = tcp.moveAsync(ptp(approachFrame).setJointVelocityRel(APPROACH_VELOCITY));
                }
                if (context != null)
                {
                    context.setActiveMotion(motionContainer);
                }
                motionContainer.await();

                // Move down to target using LIN (world coordinate)
                if (impedanceMode != null)
                {
                    motionContainer = tcp.moveAsync(lin(finalTarget).setJointVelocityRel(ACTION_VELOCITY).setMode(impedanceMode));
                }
                else
                {
                    motionContainer = tcp.moveAsync(lin(finalTarget).setJointVelocityRel(ACTION_VELOCITY));
                }
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

                // Retract back to approach position
                if (impedanceMode != null)
                {
                    motionContainer = tcp.moveAsync(lin(approachFrame).setJointVelocityRel(ACTION_VELOCITY).setMode(impedanceMode));
                }
                else
                {
                    motionContainer = tcp.moveAsync(lin(approachFrame).setJointVelocityRel(ACTION_VELOCITY));
                }
                if (context != null)
                {
                    context.setActiveMotion(motionContainer);
                }
                motionContainer.await();
            }
            else
            {
                // Use world coordinate system (original behavior)
                Frame finalApproach = null;
                if (approachOffset instanceof Frame)
                {
                    finalApproach = (Frame) approachOffset;
                    
                    // Apply alternate position transformation to approach frame as well
                    if (useAlternatePosition)
                    {
                        Transformation rotationZ180 = Transformation.ofRad(0, 0, 0, 0, 0, Math.PI);
                        finalApproach = new Frame(finalApproach.copy());
                        finalApproach.transform(rotationZ180);
                    }

                    // Apply Z-axis rotation to approach frame if enabled
                    if (allowZRotation && zRotationAngle != null)
                    {
                        Transformation rotationZ = Transformation.ofRad(0, 0, 0, 0, 0, zRotationAngle.doubleValue());
                        Frame rotatedApproach = new Frame(finalApproach.copy());
                        rotatedApproach.transform(rotationZ);
                        finalApproach = rotatedApproach;
                    }

                    // Apply redundancy to approach frame
                    if (redundancyE1Offset != null && robot != null)
                    {
                        LBRE1Redundancy redundancy = new LBRE1Redundancy().setE1(redundancyE1Offset.doubleValue());
                        finalApproach.setRedundancyInformation(robot, redundancy);
                    }
                }
                else
                {
                    log.error("Invalid approach offset type for world coordinates: " + approachOffset.getClass());
                    return false;
                }

                // Approach
                IMotionContainer motionContainer;
                if (impedanceMode != null)
                {
                    motionContainer = tcp.moveAsync(ptp(finalApproach).setJointVelocityRel(APPROACH_VELOCITY).setMode(impedanceMode));
                }
                else
                {
                    motionContainer = tcp.moveAsync(ptp(finalApproach).setJointVelocityRel(APPROACH_VELOCITY));
                }
                if (context != null)
                {
                    context.setActiveMotion(motionContainer);
                }
                motionContainer.await();

                // Move to target position
                if (impedanceMode != null)
                {
                    motionContainer = tcp.moveAsync(lin(finalTarget).setJointVelocityRel(ACTION_VELOCITY).setMode(impedanceMode));
                }
                else
                {
                    motionContainer = tcp.moveAsync(lin(finalTarget).setJointVelocityRel(ACTION_VELOCITY));
                }
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
                if (impedanceMode != null)
                {
                    motionContainer = tcp.moveAsync(lin(finalApproach).setJointVelocityRel(ACTION_VELOCITY).setMode(impedanceMode));
                }
                else
                {
                    motionContainer = tcp.moveAsync(lin(finalApproach).setJointVelocityRel(ACTION_VELOCITY));
                }
                if (context != null)
                {
                    context.setActiveMotion(motionContainer);
                }
                motionContainer.await();
            }
            
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
            + (redundancyE1Offset != null ? " [E1=" + Math.toDegrees(redundancyE1Offset.doubleValue()) + "°]" : "")
            + (allowZRotation && zRotationAngle != null ? " [Rz=" + Math.toDegrees(zRotationAngle.doubleValue()) + "°]" : "")
            + (useToolCoordinates ? " [tool-coord]" : " [world-coord]")
            + (impedanceMode != null ? " [impedance]" : "");
        return desc;
    }

    @Override
    public String toString()
    {
        String redundancyStr = redundancyE1Offset != null ? ", E1=" + Math.toDegrees(redundancyE1Offset.doubleValue()) + "°" : "";
        String zRotationStr = allowZRotation && zRotationAngle != null ? ", Rz=" + Math.toDegrees(zRotationAngle.doubleValue()) + "°" : "";
        String coordStr = useToolCoordinates ? ", tool-coord" : ", world-coord";
        String impedanceStr = impedanceMode != null ? ", impedance" : "";
        return "MotionStrategy{tcp=" + tcp.getName() 
            + ", alternate=" + useAlternatePosition + redundancyStr + zRotationStr + coordStr + impedanceStr + "}";
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
