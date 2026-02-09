package biemhTekniker.programs;

import biemhTekniker.data.WorkpieceData;
import biemhTekniker.data.WorkpieceQueue;
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
 * Program to pick a new workpiece using position from the workpiece queue.
 */
public class PickNewWorkpieceProgram implements RobotProgram
{
    private static final Logger log = Logger.getLogger(PickNewWorkpieceProgram.class);
    private static final int PRE_PICK_Z_OFFSET_MM = 100;

    /**
     * Executes the pick operation for a new workpiece.
     */
    public void execute(RobotContext context) throws Exception
    {
        log.info("Picking new workpiece...");

        // Get dependencies from context
        LBR robot = context.getRobot();
        Tool gripper = context.getGripper();
        MediaFlangeIOGroup gripperIO = context.getGripperIO();
        RoboticsAPIApplication app = context.getApplication();
        WorkpieceQueue queue = context.getWorkpieceQueue();

        // Get next workpiece from queue
        WorkpieceData workpieceData = queue.takeNextForPicking();
        if (workpieceData == null)
        {
            log.error("No workpieces available to pick");
            throw new Exception("No workpieces available");
        }

        if (!workpieceData.isValid())
        {
            log.error("Cannot pick workpiece - no valid position data available");
            throw new Exception("Invalid workpiece data");
        }

        log.debug("Using workpiece position: " + workpieceData);
        gripperIO.setGripper1_Switch(false);
        gripperIO.setGripper2_Switch(false);

        // Gripper TCP declaration - use only gripper A
        ObjectFrame tcpA = gripper.getFrame("TCPA");

        // Frame sent by camera
        Frame pickPosition = workpieceData.getWorkPiecePickFrame();
        Frame prePickPosition = workpieceData.getWorkPiecePickFrame();

        // Pick position with offset
        prePickPosition.setZ(prePickPosition.getZ() + PRE_PICK_Z_OFFSET_MM);

        // Generate motion strategies using the generator utility
        List<MotionStrategy> motionStrategies = MotionStrategyGenerator.generateStrategies(tcpA, robot);

        // Create gripper activation action
        final MediaFlangeIOGroup finalGripperIO = gripperIO;
        MotionStrategy.MotionAction gripperAction = new MotionStrategy.MotionAction()
        {
            public void execute() throws Exception
            {
                finalGripperIO.setGripper1_Switch(true);
                ThreadUtil.milliSleep(500);
            }
        };

        // Try each strategy until one succeeds
        boolean pickSucceeded = false;
        for (int i = 0; i < motionStrategies.size(); i++)
        {
            MotionStrategy strategy = motionStrategies.get(i);
            if (strategy.executeMotion(pickPosition, prePickPosition, gripperAction))
            {
                pickSucceeded = true;
                break;
            }
        }

        if (!pickSucceeded)
        {
            log.error("All pick strategies failed for workpiece: " + workpieceData.getId());
            throw new Exception("Failed to pick workpiece - all strategies exhausted");
        }

        // Return to home position
        gripper.move(ptp(app.getApplicationData().getFrame("/BiemhHome")));
    }
}
