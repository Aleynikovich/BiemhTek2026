package biemhTekniker.programs;

import biemhTekniker.exceptions.ProgramCancelledException;
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
 * Program to pick a measured workpiece from the SchunkBase location.
 * Sequence: Exit -> PickPlace (pick) -> Exit
 * Uses TCP B (gripper 2) for measured workpiece handling.
 */
public class PickMeasuredWorkpieceProgram implements RobotProgram
{

    private static final Logger log = Logger.getLogger(PickMeasuredWorkpieceProgram.class);
    private static final int PRE_PICK_Z_OFFSET_MM = 100;
    private static final int GRIPPER_ACTIVATION_DELAY_MS = 500;

    /**
     * Executes the pick operation for a measured workpiece.
     */
    public void execute(RobotContext context) throws Exception
    {
        log.info("Picking measured workpiece...");

        // Get dependencies from context
        LBR robot = context.getRobot();
        Tool gripper = context.getGripper();
        MediaFlangeIOGroup gripperIO = context.getGripperIO();
        RoboticsAPIApplication app = context.getApplication();

        // Ensure gripper 2 is open before picking
        gripperIO.setGripper2_Switch(false);

        // Use TCP B for measured workpiece handling
        ObjectFrame tcpB = gripper.getFrame("TCPB");

        ObjectFrame pickPlaceFrame = app.getApplicationData().getFrame("/SchunkBase/PickPlaceB");

        Frame pickPosition = pickPlaceFrame.copyWithRedundancy();
        Frame prePickPosition = new Frame(pickPosition.copyWithRedundancy());
        prePickPosition.setZ(prePickPosition.getZ() + PRE_PICK_Z_OFFSET_MM);

        // Generate motion strategies for pick operation
        List<MotionStrategy> motionStrategies = MotionStrategyGenerator.generateStrategiesWithoutAlternate(tcpB, robot);

        // Create gripper activation action (close gripper 2)
        final MediaFlangeIOGroup finalGripperIO = gripperIO;
        MotionStrategy.MotionAction gripperAction = new MotionStrategy.MotionAction()
        {
            public void execute() throws Exception
            {
                finalGripperIO.setGripper2_Switch(true);
                ThreadUtil.milliSleep(GRIPPER_ACTIVATION_DELAY_MS);
            }
        };

        // Try each strategy until one succeeds
        boolean pickSucceeded = false;
        for (int i = 0; i < motionStrategies.size(); i++)
        {
            MotionStrategy strategy = motionStrategies.get(i);
            if (strategy.executeMotion(pickPosition, prePickPosition, gripperAction, context))
            {
                pickSucceeded = true;
                break;
            }
            
            // Check for cancellation after failed strategy - stop trying other strategies
            if (context.isCancellationRequested())
            {
                log.warn("Program cancelled after pick measured strategy failure");
                throw new ProgramCancelledException("Program cancelled by user");
            }
        }

        if (!pickSucceeded)
        {
            log.error("All pick strategies failed for measured workpiece");
            throw new Exception("Failed to pick measured workpiece - all strategies exhausted");
        }

        log.info("Pick measured workpiece completed successfully");
    }
}
