package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;

import java.util.List;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

/**
 * Program to place a new workpiece at a predefined location.
 * Uses MotionStrategy pattern with redundancy support.
 */
public class PlaceNewWorkpieceProgram implements RobotProgram
{

    private static final Logger log = Logger.getLogger(PlaceNewWorkpieceProgram.class);
    private static final int PRE_PLACE_Z_OFFSET_MM = 100;
    private static final int GRIPPER_RELEASE_DELAY_MS = 500;

    /**
     * Executes the place operation for a new workpiece.
     */
    public void execute(RobotContext context) throws Exception
    {
        log.info("Placing new workpiece...");

        // Get dependencies from context
        LBR robot = context.getRobot();
        Tool gripper = context.getGripper();
        MediaFlangeIOGroup gripperIO = context.getGripperIO();
        RoboticsAPIApplication app = context.getApplication();

        ObjectFrame tcpA = gripper.getFrame("TCPA");

        // Define place position (example: using predefined frame)
        ObjectFrame placeFrame = app.getApplicationData().getFrame("/SchunkBase/PickPlace");
        Frame placePosition = placeFrame.copyWithRedundancy();
        Frame prePlacePosition = new Frame(placePosition.copy());
        prePlacePosition.setZ(prePlacePosition.getZ() + PRE_PLACE_Z_OFFSET_MM);

        // Generate motion strategies using the generator utility
        // For placement, we might not need alternate position, so use simplified version
        List<MotionStrategy> motionStrategies = MotionStrategyGenerator.generateStrategiesWithoutAlternate(tcpA, robot);

        // Create gripper release action
        final MediaFlangeIOGroup finalGripperIO = gripperIO;
        MotionStrategy.MotionAction gripperReleaseAction = new MotionStrategy.MotionAction()
        {
            public void execute() throws Exception
            {
                finalGripperIO.setGripper1_Switch(false);
                finalGripperIO.setGripper2_Switch(false);
                ThreadUtil.milliSleep(GRIPPER_RELEASE_DELAY_MS);
            }
        };

        // Try each strategy until one succeeds
        boolean placeSucceeded = false;
        for (int i = 0; i < motionStrategies.size(); i++)
        {
            MotionStrategy strategy = motionStrategies.get(i);
            if (strategy.executeMotion(placePosition, prePlacePosition, gripperReleaseAction))
            {
                placeSucceeded = true;
                break;
            }
        }

        if (!placeSucceeded)
        {
            log.error("All place strategies failed");
            throw new Exception("Failed to place workpiece - all strategies exhausted");
        }

        // Return to home position
        gripper.move(ptp(app.getApplicationData().getFrame("/BiemhHome")));

        log.info("PlaceNewWorkpieceProgram: Placement completed successfully");
    }
}
