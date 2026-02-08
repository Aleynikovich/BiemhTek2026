package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.executionModel.CommandInvalidException;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.lin;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

/**
 * Represents a pick strategy with specific TCP, position, and gripper configuration.
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

    /**
     * Creates a pick strategy.
     *
     * @param tcp                  Tool center point frame
     * @param useAlternatePosition If true, rotate position by 180 degrees
     * @param isGripperB           If true, use gripper B (2), otherwise gripper A (1)
     */
    public PickStrategy(ObjectFrame tcp, boolean useAlternatePosition, boolean isGripperB)
    {
        this.tcp = tcp;
        this.useAlternatePosition = useAlternatePosition;
        this.isGripperB = isGripperB;
        this.gripperNumber = isGripperB ? 2 : 1;
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

        if (useAlternatePosition)
        {
            targetPick = pickPosition;
            targetPrePick = prePickPosition;
            // Alternate position logic (180 degree rotation) would go here
            // Currently commented out in original code
        }

        String strategyDesc = "gripper" + (isGripperB ? "B" : "A") + gripperNumber + (useAlternatePosition ? " (alternate)" : " (regular)");

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
        return "PickStrategy{tcp=" + tcp.getName() + ", gripper=" + (isGripperB ? "B" : "A") + gripperNumber + ", alternate=" + useAlternatePosition + "}";
    }
}
