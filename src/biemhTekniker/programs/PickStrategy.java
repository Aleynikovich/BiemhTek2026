package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;

/**
 * Represents a pick strategy with specific TCP, position, and gripper configuration.
 * Supports redundancy (null space) motion to find alternative robot configurations.
 * This class wraps MotionStrategy to provide pick-specific functionality.
 */
public class PickStrategy
{
    private static final Logger log = Logger.getLogger(PickStrategy.class);

    private static final int GRIPPER_ACTIVATION_DELAY_MS = 500;

    private final MotionStrategy motionStrategy;
    private final boolean isGripperB;
    private final int gripperNumber;

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
        this.motionStrategy = new MotionStrategy(tcp, useAlternatePosition, redundancyE1Offset, robot);
        this.isGripperB = isGripperB;
        this.gripperNumber = isGripperB ? 2 : 1;
    }

    /**
     * Attempts to execute the pick operation with this strategy.
     *
     * @param pickPosition    Target pick position
     * @param prePickPosition Pre-pick approach position
     * @param gripperIO       Gripper I/O control
     * @param context         Robot context for cancellation support
     * @return true if pick succeeded, false if motion failed
     */
    public boolean execute(Frame pickPosition, Frame prePickPosition, final MediaFlangeIOGroup gripperIO, RobotContext context)
    {
        // Create gripper activation action
        MotionStrategy.MotionAction gripperAction = new MotionStrategy.MotionAction()
        {
            public void execute() throws Exception
            {
                // Activate gripper
                if (isGripperB)
                {
                    gripperIO.setGripper2_Switch(true);
                } else
                {
                    gripperIO.setGripper1_Switch(true);
                }
                ThreadUtil.milliSleep(GRIPPER_ACTIVATION_DELAY_MS);
            }
        };

        // Execute motion with gripper activation
        return motionStrategy.executeMotion(pickPosition, prePickPosition, gripperAction, context);
    }

    @Override
    public String toString()
    {
        return "PickStrategy{gripper=" + (isGripperB ? "B" : "A") + gripperNumber 
            + ", " + motionStrategy.toString() + "}";
    }
}
