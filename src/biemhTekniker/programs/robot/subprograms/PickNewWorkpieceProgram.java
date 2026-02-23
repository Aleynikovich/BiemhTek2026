package biemhTekniker.programs.robot.subprograms;

import biemhTekniker.lib.data.WorkpieceData;
import biemhTekniker.lib.data.WorkpieceQueue;
import biemhTekniker.lib.exceptions.ProgramCancelledException;
import biemhTekniker.lib.logger.Logger;
import biemhTekniker.lib.robot.RobotProgram;
import biemhTekniker.lib.robot.motions.MotionStrategy;
import biemhTekniker.lib.robot.motions.MotionStrategyGenerator;
import biemhTekniker.programs.robot.RobotContext;
import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.AutExtIOGroup;
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
 * After picking with Gripper A, performs workpiece exchange by placing
 * measured workpiece (if present in Gripper B) at the same position.
 */
public class PickNewWorkpieceProgram implements RobotProgram
{
    private static final Logger log = Logger.getLogger(PickNewWorkpieceProgram.class);
    private static final int PRE_PICK_Z_OFFSET_MM = 100;//200
    private static final int GRIPPER_ACTIVATION_DELAY_MS = 500;
    private static final int ALTERNATE_ORIENTATION_MULTIPLIER = 10;
    

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
        AutExtIOGroup autExtIO = context.getAutExtIO();

        // Try forced selection first (from console), then fallback to next available
        WorkpieceData workpieceData = null;
        try
        {
            Long forcedId = biemhTekniker.lib.robot.motions.MotionOverrides.consumeForcedWorkpieceId();
            if (forcedId != null)
            {
                WorkpieceData forced = queue.getById(forcedId.longValue());
                if (forced != null && forced.isValid() && forced.getState() == biemhTekniker.lib.data.WorkpieceState.AVAILABLE)
                {
                    workpieceData = forced;
                    log.info("Forced pick of workpiece ID " + forcedId + " requested via console");
                } else
                {
                    log.warn("Forced workpiece ID " + forcedId + " not AVAILABLE/valid; falling back to next available");
                }
            }
        } catch (Exception ex)
        {
            log.warn("Error checking forced workpiece selection: " + ex.getMessage());
        }
        if (workpieceData == null)
        {
            workpieceData = queue.peekNextForPicking();
        }
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

        
        autExtIO.setCurrentProgramNumber(11);
        
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
        robot.getFlange().moveAsync(ptp(app.getApplicationData().getFrame("/binCenter")).setBlendingCart(0.5));
        // Try each strategy until one succeeds
        boolean pickSucceeded = false;
        MotionStrategy successfulStrategy = null;
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
                successfulStrategy = strategy;
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
        
        // Pick succeeded - set orientation and mark workpiece
        // Set orientation based on successful pick strategy
        // Robot determines orientation: 0=regular, 1=180deg rotation (alternate position)
        int orientation = successfulStrategy.getOrientation();
        workpieceData.setOrientation(orientation);
        log.info("Workpiece picked with orientation " + orientation + " (" + 
                 (orientation == 0 ? "regular" : "180deg rotation") + ")");

        // Mark workpiece as held by Gripper 1 after successful pick
        queue.markPicked(workpieceData.getId(), 1);
        
        log.info("Successfully picked workpiece with Gripper 1: " + workpieceData.getId());
        
        
        
        //new alex
        // On successful placement, write PLC code representing reference + orientation
        //WorkpieceQueue queue = context.getWorkpieceQueue();
        WorkpieceData wp = queue.getPickedWorkpiece(1);
        if (wp != null)
        {
            int referenceIndex = wp.getReferenceIndex();
            //int orientation = wp.getOrientation();
            int plcCode = (orientation == 1) ? referenceIndex * ALTERNATE_ORIENTATION_MULTIPLIER : referenceIndex;

          
            if (autExtIO != null)
            {
                autExtIO.setCurrentProgramNumber(plcCode);
                log.info("PLC output Zeiss_Part_Type_Loaded set to " + plcCode
                    + " (id=" + wp.getId() + ", ref=" + referenceIndex + ", ori=" + orientation + ")");
            }
            else
            {
                log.warn("AutExtIO not available - skipping Zeiss_Part_Type_Loaded output");
            }
        }
        else
        {
            log.warn("No workpiece found in gripper 1 after placement - skipping Zeiss_Part_Type_Loaded output");
        }

        
        //end alex
        
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


        log.info("Pick new workpiece with exchange completed successfully");

    }
}
