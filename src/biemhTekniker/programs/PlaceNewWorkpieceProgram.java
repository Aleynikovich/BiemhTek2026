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

import static com.kuka.roboticsAPI.motionModel.BasicMotions.lin;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

/**
 * Program to place a new workpiece at a predefined location.
 * Uses MotionStrategy pattern with redundancy support.
 */
public class PlaceNewWorkpieceProgram implements RobotProgram
{

    private static final Logger log = Logger.getLogger(PlaceNewWorkpieceProgram.class);
    private static final int PRE_PLACE_Y_OFFSET_MM = 400;
    private static final int PRE_PLACE_Z_OFFSET_MM = 200;
    private static final int GRIPPER_RELEASE_DELAY_MS = 500;
    private static final int GRIPPER_ACTIVATION_DELAY_MS = 500;

    /**
     * Executes the place operation for a new workpiece.
     * If a measured workpiece is present in gripper 3 at SchunkBase, it will:
     * 1. Open gripper 3 (holding the measured piece)
     * 2. Pick the measured piece with TCP B (gripper 2)
     * 3. Place the new workpiece with TCP A (gripper 1)
     * 4. Close gripper 3
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
        ObjectFrame tcpB = gripper.getFrame("TCPB");

        // Get frames from station setup
        ObjectFrame pickPlaceFrame = app.getApplicationData().getFrame("/SchunkBase/PickPlace");
        
        Frame pickPlacePosition = pickPlaceFrame.copyWithRedundancy();

        Frame prePickPlacePositionZ = new Frame(pickPlacePosition.copy());
        prePickPlacePositionZ.setZ(prePickPlacePositionZ.getZ() - PRE_PLACE_Z_OFFSET_MM);
        Frame prePickPlacePositionY = new Frame(pickPlacePosition.copy());
        prePickPlacePositionY.setY(prePickPlacePositionY.getY() - PRE_PLACE_Y_OFFSET_MM);

        // TODO: Check if measured workpiece is present in gripper 3
        // This is a placeholder - actual IO signal needs to be configured
        boolean measuredWorkpiecePresent = checkMeasuredWorkpiecePresence();

        if (measuredWorkpiecePresent)
        {
            log.info("Measured workpiece detected in gripper 3 - picking it before placing new workpiece");
            
            // TODO: Open gripper 3 (holding the measured workpiece)
            // This is a placeholder - actual IO signal needs to be configured
            // Example: gripperIO.setGripper3_Switch(true); // Open to release
            
            // Pick measured workpiece with TCP B (gripper 2)
            pickMeasuredWorkpieceWithTcpB(robot, tcpB, gripperIO, pickPlacePosition, prePickPlacePositionZ);
        }

        // Place new workpiece with TCP A (gripper 1)
        placeNewWorkpieceWithTcpA(robot, tcpA, gripperIO, pickPlacePosition, prePickPlacePositionY);

        if (measuredWorkpiecePresent)
        {
            // TODO: Close gripper 3 to secure the new workpiece
            // This is a placeholder - actual IO signal needs to be configured
            // Example: gripperIO.setGripper3_Switch(false); // Close to hold
        }

        // Return to home position
        gripper.move(ptp(app.getApplicationData().getFrame("/BiemhHome")));

        log.info("PlaceNewWorkpieceProgram: Placement completed successfully");
    }

    /**
     * Check if a measured workpiece is present in gripper 3.
     * TODO: This is a placeholder method. Actual implementation requires 
     * configuration of the part presence sensor IO signal.
     * 
     * @return true if measured workpiece is present, false otherwise
     */
    private boolean checkMeasuredWorkpiecePresence()
    {
        return gripperIO.getGripper3_PartPresence();
        log.debug("Checking measured workpiece presence in gripper 3 (placeholder)");
        return false; // Default to false until IO is configured
    }

    /**
     * Pick measured workpiece using TCP B (gripper 2).
     * 
     * @param robot Robot instance
     * @param tcpB TCP B frame (gripper 2)
     * @param gripperIO Gripper IO group
     * @param pickPlacePosition Place position frame
     * @param prePickPlacePosition Pre-place position frame (with Z offset)
     * @throws Exception if pick operation fails
     */
    private void pickMeasuredWorkpieceWithTcpB(LBR robot, ObjectFrame tcpB, 
                                               MediaFlangeIOGroup gripperIO,
                                               Frame pickPlacePosition, Frame prePickPlacePosition) throws Exception
    {
        log.info("Picking measured workpiece with TCP B...");
        
        // Ensure gripper 2 is open
        gripperIO.setGripper2_Switch(false);
        
        // Move to exit position for safe approach
        tcpB.move(ptp(prePickPlacePosition));
        
        // Generate motion strategies for TCP B
        List<MotionStrategy> motionStrategies = MotionStrategyGenerator.generateStrategiesWithoutAlternate(tcpB, robot);
        
        // Create gripper activation action for TCP B
        final MediaFlangeIOGroup finalGripperIO = gripperIO;
        MotionStrategy.MotionAction gripperActivateAction = new MotionStrategy.MotionAction()
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
            if (strategy.executeMotion(pickPlacePosition, prePickPlacePosition, gripperActivateAction))
            {
                pickSucceeded = true;
                break;
            }
        }
        
        if (!pickSucceeded)
        {
            log.error("Failed to pick measured workpiece with TCP B");
            throw new Exception("Failed to pick measured workpiece - all strategies exhausted");
        }
        
        // Move back to exit position with measured workpiece
        tcpB.move(lin(prePickPlacePosition));
        log.info("Measured workpiece picked successfully with TCP B");
    }

    /**
     * Place new workpiece using TCP A (gripper 1).
     * 
     * @param robot Robot instance
     * @param tcpA TCP A frame (gripper 1)
     * @param gripperIO Gripper IO group
     * @param placePosition Place position frame
     * @param prePickPlacePosition Pre-place position frame (with Z offset)
     * @throws Exception if place operation fails
     */
    private void placeNewWorkpieceWithTcpA(LBR robot, ObjectFrame tcpA,
                                          MediaFlangeIOGroup gripperIO,
                                          Frame placePosition, Frame prePickPlacePosition) throws Exception
    {
        log.info("Placing new workpiece with TCP A...");
        
        // Generate motion strategies for TCP A
        List<MotionStrategy> motionStrategies = MotionStrategyGenerator.generateStrategiesWithoutAlternate(tcpA, robot);

        // Create gripper release action for TCP A
        final MediaFlangeIOGroup finalGripperIO = gripperIO;
        MotionStrategy.MotionAction gripperReleaseAction = new MotionStrategy.MotionAction()
        {
            public void execute() throws Exception
            {
                // Release only gripper A (gripper1)
                finalGripperIO.setGripper1_Switch(false);
                ThreadUtil.milliSleep(GRIPPER_RELEASE_DELAY_MS);
            }
        };

        // Try each strategy until one succeeds
        boolean placeSucceeded = false;
        for (int i = 0; i < motionStrategies.size(); i++)
        {
            MotionStrategy strategy = motionStrategies.get(i);
            if (strategy.executeMotion(placePosition, prePickPlacePosition, gripperReleaseAction))
            {
                placeSucceeded = true;
                break;
            }
        }

        if (!placeSucceeded)
        {
            log.error("All place strategies failed for new workpiece");
            throw new Exception("Failed to place new workpiece - all strategies exhausted");
        }
        
        log.info("New workpiece placed successfully with TCP A");
    }
}
