package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.deviceModel.LBRE1Redundancy;
import com.kuka.roboticsAPI.executionModel.CommandInvalidException;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.math.Transformation;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.lin;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

/**
 * Represents a pick strategy with specific TCP, position, and gripper configuration.
 * Supports redundancy (null space) motion to find alternative robot configurations.
 */
public class PickStrategy
{
    private static final Logger log = Logger.getLogger(PickStrategy.class);

    private static final int GRIPPER_ACTIVATION_DELAY_MS = 500;
    private static final double APPROACH_VELOCITY = 0.5;
    private static final double PICK_VELOCITY = 0.25;

    private final ObjectFrame tcp;
    private final boolean useAlternatePosition;
    private final boolean isGripperB;
    private final int gripperNumber;
    private final Double redundancyE1Offset; // E1 offset in radians for null space motion (null if not used)
    private final LBR robot; // Robot instance needed for redundancy

    /**
     * Creates a pick strategy without redundancy.
     *
     * @param tcp                  Tool center point frame
     * @param useAlternatePosition If true, rotate position by 180 degrees
     * @param isGripperB           If true, use gripper B (2), otherwise gripper A (1)
     */
    public PickStrategy(ObjectFrame tcp, boolean useAlternatePosition, boolean isGripperB)
    {
        this(tcp, useAlternatePosition, isGripperB, null, null);
    }

    /**
     * Creates a pick strategy with redundancy support.
     *
     * @param tcp                  Tool center point frame
     * @param useAlternatePosition If true, rotate position by 180 degrees around Z-axis
     * @param isGripperB           If true, use gripper B (2), otherwise gripper A (1)
     * @param redundancyE1Offset   E1 offset in radians for null space motion (null to disable)
     * @param robot                Robot instance (required when redundancyE1Offset is not null)
     */
    public PickStrategy(ObjectFrame tcp, boolean useAlternatePosition, boolean isGripperB, 
                       Double redundancyE1Offset, LBR robot)
    {
        this.tcp = tcp;
        this.useAlternatePosition = useAlternatePosition;
        this.isGripperB = isGripperB;
        this.gripperNumber = isGripperB ? 2 : 1;
        this.redundancyE1Offset = redundancyE1Offset;
        this.robot = robot;
    }

    /**
     * Attempts to execute the pick operation with this strategy.
     *
     * @param pickPosition    Target pick position
     * @param prePickPosition Pre-pick approach position
     * @param gripperIO       Gripper I/O control
     * @return true if pick succeeded, false if motion failed
     */
    public boolean execute(Frame pickPosition, Frame prePickPosition, MediaFlangeIOGroup gripperIO)
    {
        Frame targetPick = pickPosition;
        Frame targetPrePick = prePickPosition;

        // Apply alternate position transformation (180 degree rotation around Z-axis)
        if (useAlternatePosition)
        {
            // Create transformation: 180 degrees around Z-axis (alpha/C rotation)
            Transformation rotationZ180 = Transformation.ofRad(0, 0, 0, 0, 0, Math.PI);
            
            // Apply rotation to both frames
            targetPick = new Frame(pickPosition.copy());
            targetPick.transform(rotationZ180);
            
            targetPrePick = new Frame(prePickPosition.copy());
            targetPrePick.transform(rotationZ180);
        }

        // Apply redundancy information if specified
        if (redundancyE1Offset != null && robot != null)
        {
            LBRE1Redundancy redundancy = new LBRE1Redundancy().setE1(redundancyE1Offset.doubleValue());
            targetPick.setRedundancyInformation(robot, redundancy);
            targetPrePick.setRedundancyInformation(robot, redundancy);
        }

        String strategyDesc = "gripper" + (isGripperB ? "B" : "A") + gripperNumber 
            + (useAlternatePosition ? " (alternate)" : " (regular)")
            + (redundancyE1Offset != null ? " [E1=" + Math.toDegrees(redundancyE1Offset.doubleValue()) + "°]" : "");

        try
        {
            log.info("Attempting to pick with " + strategyDesc + ": " + targetPick);

            // Approach
            tcp.move(ptp(targetPrePick).setJointVelocityRel(APPROACH_VELOCITY));

            // Move to pick position
            tcp.move(lin(targetPick).setJointVelocityRel(PICK_VELOCITY));

            // Activate gripper
            if (isGripperB)
            {
                gripperIO.setGripper2_Switch(true);
            } else
            {
                gripperIO.setGripper1_Switch(true);
            }

            ThreadUtil.milliSleep(GRIPPER_ACTIVATION_DELAY_MS);

            // Retract
            tcp.move(lin(targetPrePick).setJointVelocityRel(PICK_VELOCITY));

            log.info("Pick succeeded with " + strategyDesc);
            return true;
        } catch (CommandInvalidException e)
        {
            log.warn("Pick failed with " + strategyDesc + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public String toString()
    {
        String redundancyStr = redundancyE1Offset != null ? ", E1=" + Math.toDegrees(redundancyE1Offset.doubleValue()) + "°" : "";
        return "PickStrategy{tcp=" + tcp.getName() + ", gripper=" + (isGripperB ? "B" : "A") + gripperNumber 
            + ", alternate=" + useAlternatePosition + redundancyStr + "}";
    }
}
