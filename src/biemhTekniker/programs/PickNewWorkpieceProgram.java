package biemhTekniker.programs;

import biemhTekniker.data.WorkpieceData;
import biemhTekniker.data.WorkpieceQueue;
import biemhTekniker.exceptions.ProgramCancelledException;
import biemhTekniker.logger.Logger;
import biemhTekniker.vision.SmartPickingProtocol;
import biemhTekniker.vision.SmartPickingProtocol.Command;
import biemhTekniker.vision.SmartPickingProtocol.VisionResult;
import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.motionModel.IMotionContainer;

import java.util.List;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

/**
 * Program to pick a new workpiece using position from the workpiece queue.
 * After picking with Gripper A, performs workpiece exchange by placing
 * measured workpiece (if present in Gripper B) at the same position.
 */
public class PickNewWorkpieceProgram implements RobotProgram
{
    private static final Logger log = Logger.getLogger(PickNewWorkpieceProgram.class);
    private static final int PRE_PICK_Z_OFFSET_MM = 100;
    private static final int GRIPPER_ACTIVATION_DELAY_MS = 500;

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
        ObjectFrame scanWorkpiecePosition = app.getApplicationData().getFrame("/ScanWorkpiece");
        Frame scanWorkpieceFrame = scanWorkpiecePosition.copyWithRedundancy();
        
        // Peek at next workpiece without consuming it yet
        WorkpieceData workpieceData = queue.peekNextForPicking();
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
        
        // Check for cancellation before starting motion
        if (context.isCancellationRequested())
        {
            log.warn("Program cancelled before pick motion started");
            throw new ProgramCancelledException("Program cancelled by user");
        }

        // Gripper TCP declaration - use only gripper A
        ObjectFrame tcpA = gripper.getFrame("TCPA");

        // Frame sent by camera
        Frame pickPosition = workpieceData.getWorkPiecePickFrame();
        Frame prePickPosition = workpieceData.getWorkPiecePickFrame();

        // Pick position with offset
        prePickPosition.setZ(prePickPosition.getZ() + PRE_PICK_Z_OFFSET_MM);

        // Generate motion strategies using tool coordinates for pick (no Z-rotation)
        List<MotionStrategy> motionStrategies = MotionStrategyGenerator.generateStrategiesWithToolCoordinates(tcpA, robot);

        // Create gripper activation action
        final MediaFlangeIOGroup finalGripperIO = gripperIO;
        MotionStrategy.MotionAction gripperAction = new MotionStrategy.MotionAction()
        {
            public void execute() throws Exception
            {
                finalGripperIO.setGripper1_Switch(true);
                ThreadUtil.milliSleep(GRIPPER_ACTIVATION_DELAY_MS);
            }
        };

        // Try each strategy until one succeeds
        boolean pickSucceeded = false;
        for (int i = 0; i < motionStrategies.size(); i++)
        {
            // Check for cancellation between strategies
            if (context.isCancellationRequested())
            {
                log.warn("Program cancelled during pick operation");
                throw new ProgramCancelledException("Program cancelled by user");
            }
            
            MotionStrategy strategy = motionStrategies.get(i);
            if (strategy.executeMotion(pickPosition, Double.valueOf(PRE_PICK_Z_OFFSET_MM), gripperAction, context))
            {
                pickSucceeded = true;
                break;
            }
            
            // Check for cancellation after failed strategy - stop trying other strategies
            if (context.isCancellationRequested())
            {
                log.warn("Program cancelled after strategy failure");
                throw new ProgramCancelledException("Program cancelled by user");
            }
        }

        // Check for cancellation before final move
        if (context.isCancellationRequested())
        {
            log.warn("Program cancelled before final position move");
            throw new ProgramCancelledException("Program cancelled by user");
        }

        if (!pickSucceeded)
        {
            log.error("All pick strategies failed for workpiece: " + workpieceData.getId());
            throw new Exception("Failed to pick workpiece - all strategies exhausted");
        }

        // Mark workpiece as PICKED only after successful pick
        queue.markPicked(workpieceData.getId());
        workpieceData.setGripperLocation("A"); // Track that it's in gripper A
        log.info("Successfully picked workpiece with Gripper A: " + workpieceData.getId());

        // Now exchange: place measured workpiece at the same position with Gripper B
        // Go to pre-pick position with Gripper B for repositioning with different redundancies
        log.info("Repositioning to place measured workpiece with Gripper B...");
        ObjectFrame tcpB = gripper.getFrame("TCPB");
        
        // Check for cancellation before exchange
        if (context.isCancellationRequested())
        {
            log.warn("Program cancelled before workpiece exchange");
            throw new ProgramCancelledException("Program cancelled by user");
        }

        // Open gripper B before placing
        gripperIO.setGripper2_Switch(false);
        
        // Generate place strategies for TCP B with Z-rotation freedom and tool coordinates
        List<MotionStrategy> exchangeStrategies = MotionStrategyGenerator.generatePlaceStrategies(tcpB, robot);
        
        // Create gripper release action for measured workpiece
        MotionStrategy.MotionAction releaseAction = new MotionStrategy.MotionAction()
        {
            public void execute() throws Exception
            {
                // Release measured workpiece (even if we don't have one, we still do the motion)
                finalGripperIO.setGripper2_Switch(false);
                ThreadUtil.milliSleep(GRIPPER_ACTIVATION_DELAY_MS);
            }
        };

        // Try placing measured workpiece at the same position where new workpiece was picked
        // Note: pickPosition here refers to where the NEW workpiece was picked, which becomes 
        // the place position for the measured workpiece (workpiece exchange)
        boolean placeSucceeded = false;
        for (int i = 0; i < exchangeStrategies.size(); i++)
        {
            // Check for cancellation between strategies
            if (context.isCancellationRequested())
            {
                log.warn("Program cancelled during workpiece exchange");
                throw new ProgramCancelledException("Program cancelled by user");
            }
            
            MotionStrategy strategy = exchangeStrategies.get(i);
            // Using pickPosition as the target because we're placing at the same spot we just picked from
            if (strategy.executeMotion(pickPosition, Double.valueOf(PRE_PICK_Z_OFFSET_MM), releaseAction, context))
            {
                placeSucceeded = true;
                break;
            }
            
            // Check for cancellation after failed strategy - stop trying other strategies
            if (context.isCancellationRequested())
            {
                log.warn("Program cancelled during workpiece exchange after strategy failure");
                throw new ProgramCancelledException("Program cancelled by user");
            }
        }

        if (!placeSucceeded)
        {
            log.warn("Failed to place measured workpiece during exchange - continuing anyway");
        } else
        {
            log.info("Placed measured workpiece at new workpiece position");
        }

        // Move to scan position with cancellable motion
        IMotionContainer finalMotion = tcpA.moveAsync(ptp(scanWorkpieceFrame));
        context.setActiveMotion(finalMotion);
        finalMotion.await();
        context.setActiveMotion(null);
        
        // Robot is now at scan position - capture image with camera
        // This blocks the robot at the scan position until camera completes capture
        log.info("Robot at scan position, requesting camera capture...");
        SmartPickingProtocol protocol = context.getProtocol();
        if (protocol != null)
        {
            try
            {
                // Send command "2" (CAPTURE_DATA) to camera and wait for response "0" (success)
                VisionResult captureResult = protocol.execute(Command.CAPTURE_DATA, true);
                if (captureResult.isSuccess())
                {
                    log.info("Camera capture completed successfully, robot can now move");
                } else
                {
                    log.warn("Camera capture failed or returned error, but continuing");
                }
            } catch (Exception e)
            {
                log.error("Error during camera capture: " + e.getMessage());
                // Continue anyway - camera failure shouldn't stop the robot
            }
        } else
        {
            log.warn("Protocol not available - cannot trigger camera capture");
        }
        
        // Scan the picked workpiece to determine orientation
        log.info("Scanning workpiece to determine orientation...");
        
        // The vision system needs to scan the workpiece at the scan position
        // Program 110 (ScanPickedWorkpiece) will:
        // 1. Get the picked workpiece from the queue
        // 2. Send the appropriate scan command based on reference (1->53, 2->55, 3->60)
        // 3. Get the orientation result (0=regular, 1=inverted)
        // 4. Store the orientation in the workpiece data
        // Note: This is a vision task, so it runs asynchronously via ProgramDispatcher
        // The camera has already captured the image above, so the vision task can process it
        
        log.info("Pick new workpiece with exchange completed successfully");

    }
}
