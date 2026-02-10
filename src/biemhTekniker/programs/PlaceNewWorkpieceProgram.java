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
    private static final int PRE_PLACE_Z_OFFSET_MM = 200;
    private static final int GRIPPER_RELEASE_DELAY_MS = 500;
    private static final int GRIPPER_ACTIVATION_DELAY_MS = 500;


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
        ObjectFrame pickPlaceFrameA = app.getApplicationData().getFrame("/SchunkBase/PickPlaceA");
        Frame pickPlacePositionA = pickPlaceFrameA.copyWithRedundancy();

        ObjectFrame pickPlaceFrameB = app.getApplicationData().getFrame("/SchunkBase/PickPlaceB");
        Frame pickPlacePositionB = pickPlaceFrameB.copyWithRedundancy();

        Frame prepickPlacePositionAZ = new Frame(pickPlacePositionA.copyWithRedundancy());
        prepickPlacePositionAZ.setZ(prepickPlacePositionAZ.getZ() + PRE_PLACE_Z_OFFSET_MM);

        Frame prepickPlacePositionBZ = new Frame(pickPlacePositionB.copyWithRedundancy());
        prepickPlacePositionBZ.setZ(prepickPlacePositionBZ.getZ() + PRE_PLACE_Z_OFFSET_MM);

        if (gripperIO.getGripper3_PartPresence())
        {
            log.info("Measured workpiece detected in gripper 3 - picking it before placing new workpiece");
            pickMeasuredWorkpieceWithTcpB(robot, tcpB, gripperIO, pickPlacePositionB, prepickPlacePositionBZ, context);
        }

        // Place new workpiece with TCP A (gripper 1)
        placeNewWorkpieceWithTcpA(robot, tcpA, gripperIO, pickPlacePositionA, prepickPlacePositionAZ, context);

        if (gripperIO.getGripper3_PartPresence())
        {
            gripperIO.setGripper3_Switch(true); // Close to hold
        }

        // Return to home position
        gripper.move(ptp(app.getApplicationData().getFrame("/BiemhHome")));

        log.info("PlaceNewWorkpieceProgram: Placement completed successfully");
    }

    /**
     * Pick measured workpiece using TCP B (gripper 2).
     * 
     * @param robot Robot instance
     * @param tcpB TCP B frame (gripper 2)
     * @param gripperIO Gripper IO group
     * @param pickPlacePositionB Place position frame
     * @param prepickPlacePositionB Pre-place position frame (with Z offset)
     * @param context Robot context for cancellation support
     * @throws Exception if pick operation fails
     */
    private void pickMeasuredWorkpieceWithTcpB(LBR robot, ObjectFrame tcpB, 
                                               MediaFlangeIOGroup gripperIO,
                                               Frame pickPlacePositionB, Frame prepickPlacePositionB,
                                               RobotContext context) throws Exception
    {
        log.info("Picking measured workpiece with TCP B...");

        gripperIO.setGripper2_Switch(false);
        // Generate motion strategies for TCP B
        List<MotionStrategy> motionStrategies = MotionStrategyGenerator.generateStrategiesWithoutAlternate(tcpB, robot);

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
            if (strategy.executeMotion(pickPlacePositionB, prepickPlacePositionB, gripperActivateAction, context))
            {
                pickSucceeded = true;
                break;
            }
        }
        
        if (!pickSucceeded)
        {
            gripperIO.setGripper3_Switch(false);
            ThreadUtil.milliSleep(GRIPPER_RELEASE_DELAY_MS);
            log.error("Failed to pick measured workpiece with TCP B");
            throw new Exception("Failed to pick measured workpiece - all strategies exhausted");
        }

        log.info("Measured workpiece picked successfully with TCP B");
    }

    /**
     * Place new workpiece using TCP A (gripper 1).
     * 
     * @param robot Robot instance
     * @param tcpA TCP A frame (gripper 1)
     * @param gripperIO Gripper IO group
     * @param placePositionA Place position frame
     * @param prepickPlacePositionA Pre-place position frame (with Z offset)
     * @param context Robot context for cancellation support
     * @throws Exception if place operation fails
     */
    private void placeNewWorkpieceWithTcpA(LBR robot, ObjectFrame tcpA,
                                          MediaFlangeIOGroup gripperIO,
                                          Frame placePositionA, Frame prepickPlacePositionA,
                                          RobotContext context) throws Exception
    {
        log.info("Placing new workpiece with TCP A...");
        gripperIO.setGripper3_Switch(false);

        // Generate motion strategies for TCP A
        List<MotionStrategy> motionStrategies = MotionStrategyGenerator.generateStrategiesWithoutAlternate(tcpA, robot);

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
        for (MotionStrategy strategy : motionStrategies)
        {
            if (strategy.executeMotion(placePositionA, prepickPlacePositionA, gripperReleaseAction, context))
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
        gripperIO.setGripper3_PartPresence(true);
        log.info("New workpiece placed successfully with TCP A");
    }
}
