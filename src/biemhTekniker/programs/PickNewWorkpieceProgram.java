package biemhTekniker.programs;

import biemhTekniker.data.WorkpieceData;
import biemhTekniker.data.WorkpieceQueue;
import biemhTekniker.logger.Logger;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;

import java.util.ArrayList;
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

        // Gripper TCP declaration A=1, B=2
        ObjectFrame tcpA = gripper.getFrame("TCPA");
        ObjectFrame tcpB = gripper.getFrame("TCPB");

        // Frame sent by camera
        Frame pickPosition = workpieceData.getWorkPiecePickFrame();
        Frame prePickPosition = workpieceData.getWorkPiecePickFrame();

        // Pick position with offset
        prePickPosition.setZ(prePickPosition.getZ() + PRE_PICK_Z_OFFSET_MM);

        // Create pick strategies in priority order
        List<PickStrategy> strategies = new ArrayList<PickStrategy>();
        strategies.add(new PickStrategy(tcpA, false, false)); // Regular position, gripper A
        strategies.add(new PickStrategy(tcpA, true, false));  // Alternate position, gripper A
        strategies.add(new PickStrategy(tcpB, false, true));  // Regular position, gripper B
        strategies.add(new PickStrategy(tcpB, true, true));   // Alternate position, gripper B

        // Try each strategy until one succeeds
        boolean pickSucceeded = false;
        for (int i = 0; i < strategies.size(); i++)
        {
            PickStrategy strategy = strategies.get(i);
            if (strategy.execute(pickPosition, prePickPosition, gripperIO))
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
