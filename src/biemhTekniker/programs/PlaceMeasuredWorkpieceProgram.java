package biemhTekniker.programs;

import biemhTekniker.data.WorkpieceData;
import biemhTekniker.data.WorkpieceQueue;
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
 * Program to place a measured workpiece back to its original pick position.
 * Returns the measured workpiece to the bin at the same location where it was picked.
 * Sequence: Exit -> Return Position (place) -> Exit
 * Uses TCP B (gripper 2) for measured workpiece handling.
 */
public class PlaceMeasuredWorkpieceProgram implements RobotProgram
{

    private static final Logger log = Logger.getLogger(PlaceMeasuredWorkpieceProgram.class);
    private static final int PRE_PLACE_Z_OFFSET_MM = 150;
    private static final int GRIPPER_RELEASE_DELAY_MS = 500;

    /**
     * Executes the place operation for a measured workpiece.
     * Returns the workpiece to its original pick position in the bin.
     */
    public void execute(RobotContext context) throws Exception
    {
        log.info("Placing measured workpiece...");

        // Get dependencies from context
        LBR robot = context.getRobot();
        Tool gripper = context.getGripper();
        MediaFlangeIOGroup gripperIO = context.getGripperIO();
        RoboticsAPIApplication app = context.getApplication();
        WorkpieceQueue queue = context.getWorkpieceQueue();

        // Get measured workpiece from queue
        WorkpieceData workpieceData = queue.takeMeasuredWorkpiece();
        if (workpieceData == null)
        {
            log.error("No measured workpieces available to place");
            throw new Exception("No measured workpieces available");
        }

        if (!workpieceData.isValid())
        {
            log.error("Cannot place workpiece - no valid position data available");
            throw new Exception("Invalid workpiece data");
        }

        log.debug("Placing measured workpiece back to origin: " + workpieceData);

        // Use TCP B for measured workpiece handling
        ObjectFrame tcpB = gripper.getFrame("TCPB");

        // Get exit frame from station setup
        ObjectFrame exitFrame = app.getApplicationData().getFrame("/SchunkBase/Exit");

        // Get return position (original pick location)
        Frame placePosition = workpieceData.getReturnFrame();
        Frame prePlacePosition = new Frame(placePosition.copy());
        prePlacePosition.setZ(prePlacePosition.getZ() + PRE_PLACE_Z_OFFSET_MM);

        // Create positions with redundancy
        Frame exitPosition = exitFrame.copyWithRedundancy();

        // Move to exit position (safe approach)
        log.info("Moving to exit position...");
        tcpB.move(ptp(exitPosition));

        // Generate motion strategies for place operation with Z-rotation and tool coordinates
        List<MotionStrategy> motionStrategies = MotionStrategyGenerator.generatePlaceStrategies(tcpB, robot);

        // Create gripper release action (open gripper 2)
        final MediaFlangeIOGroup finalGripperIO = gripperIO;
        MotionStrategy.MotionAction gripperReleaseAction = new MotionStrategy.MotionAction()
        {
            public void execute() throws Exception
            {
                finalGripperIO.setGripper2_Switch(false);
                ThreadUtil.milliSleep(GRIPPER_RELEASE_DELAY_MS);
            }
        };

        // Try each strategy until one succeeds
        // Pass offset as Double for tool coordinate approach
        boolean placeSucceeded = false;
        for (int i = 0; i < motionStrategies.size(); i++)
        {
            MotionStrategy strategy = motionStrategies.get(i);
            if (strategy.executeMotion(placePosition, Double.valueOf(PRE_PLACE_Z_OFFSET_MM), gripperReleaseAction, context))
            {
                placeSucceeded = true;
                break;
            }
            
            // Check for cancellation after failed strategy - stop trying other strategies
            if (context.isCancellationRequested())
            {
                log.warn("Program cancelled after place measured strategy failure");
                throw new ProgramCancelledException("Program cancelled by user");
            }
        }

        if (!placeSucceeded)
        {
            log.error("All place strategies failed for measured workpiece");
            throw new Exception("Failed to place measured workpiece - all strategies exhausted");
        }

        // Return to exit position
        log.info("Returning to exit position...");
        tcpB.move(ptp(exitPosition));

        // Mark workpiece as returned
        queue.markReturned(workpieceData.getId());

        log.info("Place measured workpiece completed successfully - returned to origin position");
    }
}
