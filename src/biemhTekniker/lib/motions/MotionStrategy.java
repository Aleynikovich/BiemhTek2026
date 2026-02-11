package biemhTekniker.lib.motions;

import biemhTekniker.lib.logger.Logger;
import biemhTekniker.programs.robot.RobotContext;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.deviceModel.LBRE1Redundancy;
import com.kuka.roboticsAPI.executionModel.CommandInvalidException;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.math.Transformation;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.lin;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

/**
 * Generic motion strategy with support for alternate position (180° rotation),
 * Z-axis rotation freedom, redundancy (null space) motion, and impedance control.
 * Can be used for any robot motion operation (pick, place, etc.).
 * <p>
 * Supports tool coordinate system approach for perpendicular approach to workpieces.
 * NOTE: In KUKA tool frames, Z+ typically points toward the flange/workpiece. Therefore,
 * approach offsets are NEGATED when using tool coordinates to move away from the workpiece.
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

    private MotionStrategy(Builder builder)
    {
        this.tcp = builder.tcp;
        this.useAlternatePosition = builder.useAlternatePosition;
        this.redundancyE1Offset = builder.redundancyE1Offset;
        this.robot = builder.robot;
        this.allowZRotation = builder.allowZRotation;
        this.zRotationAngle = builder.zRotationAngle;
        this.useToolCoordinates = builder.useToolCoordinates;
        this.impedanceMode = builder.impedanceMode;
    }

    /**
     * Helper method to create a PTP motion with impedance control if enabled.
     *
     * @param target      Target frame to move to
     * @param velocityRel Relative joint velocity
     */
    private void movePtp(Frame target, double velocityRel)
    {
        if (impedanceMode != null)
        {
            tcp.move(ptp(target).setJointVelocityRel(velocityRel).setMode(impedanceMode));
        } else
        {
            tcp.move(ptp(target).setJointVelocityRel(velocityRel));
        }
    }

    /**
     * Helper method to create a LIN motion with impedance control if enabled.
     *
     * @param target      Target frame to move to
     * @param velocityRel Relative joint velocity
     */
    private void moveLin(Frame target, double velocityRel)
    {
        if (impedanceMode != null)
        {
            tcp.move(lin(target).setJointVelocityRel(velocityRel).setMode(impedanceMode));
        } else
        {
            tcp.move(lin(target).setJointVelocityRel(velocityRel));
        }
    }

    /**
     * Attempts to execute a motion to target position with approach and retract.
     *
     * @param targetPosition Target position for the action
     * @param approachOffset Approach offset distance in mm (Double) or Frame
     * @param action         Action to execute at target position (can be null for motion-only)
     * @param context        Robot context for cancellation support (can be null if cancellation not needed)
     * @return true if motion succeeded, false if motion failed
     */
    public boolean executeMotion(Frame targetPosition, Object approachOffset, MotionAction action, RobotContext context)
    {
        try
        {
            Frame finalTarget = prepareFrame(targetPosition);
            Frame approachFrame = calculateApproachFrame(finalTarget, approachOffset);

            if (approachFrame == null)
            {
                return false;
            }

            String strategyDesc = getDescription();
            log.info("Attempting motion with " + strategyDesc + ": " + finalTarget);

            // 1. Approach
            if (useToolCoordinates)
            {
                moveLin(approachFrame, APPROACH_VELOCITY);
            } else
            {
                movePtp(approachFrame, APPROACH_VELOCITY);
            }

            // 2. Move to target
            moveLin(finalTarget, ACTION_VELOCITY);

            // 3. Action
            if (isCancelled(context))
            {
                log.warn("Motion cancelled before action execution");
                return false;
            }

            if (action != null)
            {
                action.execute();
            }

            // 4. Retract
            if (isCancelled(context))
            {
                log.warn("Motion cancelled before retract");
                return false;
            }

            moveLin(approachFrame, ACTION_VELOCITY);

            log.info("Motion succeeded with " + strategyDesc);
            return true;
        } catch (CommandInvalidException e)
        {
            if (isCancelled(context))
            {
                log.warn("Motion failed due to cancellation: " + getDescription());
                return false;
            }
            log.warn("Motion failed with " + getDescription() + ": " + e.getMessage());
            return false;
        } catch (Exception e)
        {
            if (isCancelled(context))
            {
                log.warn("Motion cancelled: " + getDescription());
                return false;
            }
            log.warn("Motion action failed with " + getDescription() + ": " + e.getMessage());
            return false;
        }
    }

    private Frame prepareFrame(Frame baseFrame)
    {
        Frame frame = baseFrame.copy();

        // Apply alternate position transformation (180 degree rotation around Z-axis)
        if (useAlternatePosition)
        {
            Transformation rotationZ180 = Transformation.ofRad(0, 0, 0, Math.PI, 0, 0);
            frame.transform(rotationZ180);
        }

        // Apply Z-axis rotation if enabled
        if (allowZRotation && zRotationAngle != null)
        {
            Transformation rotationZ = Transformation.ofRad(0, 0, 0, zRotationAngle, 0, 0);
            frame.transform(rotationZ);
        }

        // Apply redundancy information if specified
        if (redundancyE1Offset != null && robot != null)
        {
            LBRE1Redundancy redundancy = new LBRE1Redundancy().setE1(redundancyE1Offset);
            frame.setRedundancyInformation(robot, redundancy);
        }

        return frame;
    }

    private Frame calculateApproachFrame(Frame finalTarget, Object approachOffset)
    {
        if (useToolCoordinates)
        {
            if (!(approachOffset instanceof Double))
            {
                log.error("Invalid approach offset type for tool coordinates: " + (approachOffset != null ? approachOffset.getClass() : "null") + " (expected Double)");
                return null;
            }

            double offsetMm = (Double) approachOffset;
            Transformation offsetTransform = Transformation.ofRad(0, 0, -offsetMm, 0, 0, 0);
            Frame approachFrame = finalTarget.copy();
            approachFrame.transform(offsetTransform);

            // Ensure redundancy is preserved if it was set on finalTarget
            if (redundancyE1Offset != null && robot != null)
            {
                LBRE1Redundancy redundancy = new LBRE1Redundancy().setE1(redundancyE1Offset);
                approachFrame.setRedundancyInformation(robot, redundancy);
            }
            return approachFrame;
        } else
        {
            if (!(approachOffset instanceof Frame))
            {
                log.error("Invalid approach offset type for world coordinates: " + (approachOffset != null ? approachOffset.getClass() : "null") + " (expected Frame)");
                return null;
            }

            // For world coordinates, we need to apply the same transformations to the approach frame
            return prepareFrame((Frame) approachOffset);
        }
    }

    private boolean isCancelled(RobotContext context)
    {
        return context != null && context.isCancellationRequested();
    }

    /**
     * Gets the orientation determined by this strategy.
     *
     * @return 0 for regular position, 1 for alternate position (180° rotation)
     */
    public int getOrientation()
    {
        return useAlternatePosition ? 1 : 0;
    }

    /**
     * Gets a human-readable description of this strategy.
     */
    public String getDescription()
    {
        String desc = "tcp=" + tcp.getName() + (useAlternatePosition ? " (alternate)" : " (regular)") + (redundancyE1Offset != null ? " [E1=" + Math.toDegrees(redundancyE1Offset.doubleValue()) + "°]" : "") + (allowZRotation && zRotationAngle != null ? " [Rz=" + Math.toDegrees(zRotationAngle.doubleValue()) + "°]" : "") + (useToolCoordinates ? " [tool-coord]" : " [world-coord]") + (impedanceMode != null ? " [impedance]" : "");
        return desc;
    }

    @Override
    public String toString()
    {
        String redundancyStr = redundancyE1Offset != null ? ", E1=" + Math.toDegrees(redundancyE1Offset.doubleValue()) + "°" : "";
        String zRotationStr = allowZRotation && zRotationAngle != null ? ", Rz=" + Math.toDegrees(zRotationAngle.doubleValue()) + "°" : "";
        String coordStr = useToolCoordinates ? ", tool-coord" : ", world-coord";
        String impedanceStr = impedanceMode != null ? ", impedance" : "";
        return "MotionStrategy{tcp=" + tcp.getName() + ", alternate=" + useAlternatePosition + redundancyStr + zRotationStr + coordStr + impedanceStr + "}";
    }

    /**
     * Interface for actions to execute at the target position.
     */
    public interface MotionAction
    {
        /**
         * Execute the action at the target position.
         *
         * @throws Exception if the action fails
         */
        void execute() throws Exception;
    }

    public static class Builder
    {
        private final ObjectFrame tcp;
        private boolean useAlternatePosition = false;
        private Double redundancyE1Offset = null;
        private LBR robot = null;
        private boolean allowZRotation = false;
        private Double zRotationAngle = null;
        private boolean useToolCoordinates = false;
        private CartesianImpedanceControlMode impedanceMode = null;

        public Builder(ObjectFrame tcp)
        {
            this.tcp = tcp;
        }

        public Builder useAlternatePosition(boolean useAlternatePosition)
        {
            this.useAlternatePosition = useAlternatePosition;
            return this;
        }

        public Builder redundancy(Double e1Offset, LBR robot)
        {
            this.redundancyE1Offset = e1Offset;
            this.robot = robot;
            return this;
        }

        public Builder allowZRotation(boolean allowZRotation)
        {
            this.allowZRotation = allowZRotation;
            return this;
        }

        public Builder zRotationAngle(Double zRotationAngle)
        {
            this.zRotationAngle = zRotationAngle;
            return this;
        }

        public Builder useToolCoordinates(boolean useToolCoordinates)
        {
            this.useToolCoordinates = useToolCoordinates;
            return this;
        }

        public Builder impedanceMode(CartesianImpedanceControlMode impedanceMode)
        {
            this.impedanceMode = impedanceMode;
            return this;
        }

        public MotionStrategy build()
        {
            return new MotionStrategy(this);
        }
    }
}
