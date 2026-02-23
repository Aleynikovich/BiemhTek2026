package biemhTekniker.programs.robot.subprograms;

import biemhTekniker.lib.data.WorkpieceData;
import biemhTekniker.lib.data.WorkpieceQueue;
import biemhTekniker.lib.exceptions.ProgramCancelledException;
import biemhTekniker.lib.logger.Logger;
import biemhTekniker.lib.robot.RobotProgram;
import biemhTekniker.programs.robot.RobotContext;
import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.generated.ioAccess.AutExtIOGroup;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.lin;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

/**
 * Program to place a new workpiece at a predefined location.
 * Always picks measured workpiece with Gripper B first (ignoring part presence),
 * then places new workpiece with Gripper A.
 * This ensures a clean exchange and prevents any interference.
 * Uses MotionStrategy pattern with redundancy support.
 */
public class PlaceNewWorkpieceProgram implements RobotProgram
{

    private static final Logger log = Logger.getLogger(PlaceNewWorkpieceProgram.class);
    private static final int PRE_PLACE_Z_OFFSET_MM = 75;
    private static final int GRIPPER_RELEASE_DELAY_MS = 500;
    private static final int GRIPPER_ACTIVATION_DELAY_MS = 500;
    private final boolean forceAlternate = false;
    private static final int ALTERNATE_ORIENTATION_MULTIPLIER = 10;
    public void execute(RobotContext context) throws Exception
    {
        log.info("Placing new workpiece...");

        // Check for cancellation at program start
        if (context.isCancellationRequested())
        {
            log.warn("Program cancelled before place operation started");
            throw new ProgramCancelledException("Program cancelled by user");
        }

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
        prepickPlacePositionAZ.setZ(prepickPlacePositionAZ.getZ() - PRE_PLACE_Z_OFFSET_MM);

        Frame prepickPlacePositionBZ = new Frame(pickPlacePositionB.copyWithRedundancy());
        prepickPlacePositionBZ.setZ(prepickPlacePositionBZ.getZ() - PRE_PLACE_Z_OFFSET_MM);

        log.info("Picking measured workpiece with TCP B (override part presence check)...");
        pickMeasuredWorkpieceWithTcpB(robot, tcpB, gripperIO, pickPlacePositionB, prepickPlacePositionBZ, context);

        // Place new workpiece with TCP A (gripper 1)
        placeNewWorkpieceWithTcpA(robot, tcpA, gripperIO, pickPlacePositionA, prepickPlacePositionAZ, context, forceAlternate);
 
        
        //new alex
        // On successful placement, write PLC code representing reference + orientation
        WorkpieceQueue queue = context.getWorkpieceQueue();
        WorkpieceData wp = queue.getPickedWorkpiece(1);
        if (wp != null)
        {
            int referenceIndex = wp.getReferenceIndex();
            int orientation = wp.getOrientation();
            int plcCode = (orientation == 1) ? referenceIndex * ALTERNATE_ORIENTATION_MULTIPLIER : referenceIndex;

            AutExtIOGroup autExtIO = context.getAutExtIO();
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
        tcpB.move(ptp(prepickPlacePositionB));
        tcpB.move(lin(pickPlacePositionB));
        gripperIO.setGripper2_Switch(true);
        ThreadUtil.milliSleep(GRIPPER_ACTIVATION_DELAY_MS);
        gripperIO.setGripper3_Switch(false);
        ThreadUtil.milliSleep(GRIPPER_ACTIVATION_DELAY_MS);
        tcpB.move(lin(prepickPlacePositionB));
        log.info("Measured workpiece picked successfully with TCP B");
    }

    /**
     * Place new workpiece using TCP A (gripper 1).
     * 
     * @param robot Robot instance
     * @param tcpA TCP A frame (gripper 1)
     * @param gripperIO Gripper IO group
     * @param placePositionA Place position frame
     * @param prepickPlacePositionA Pre-place position frame (with Z offset) - NOT USED with tool coordinates
     * @param context Robot context for cancellation support
     * @throws Exception if place operation fails
     */
    private void placeNewWorkpieceWithTcpA(LBR robot, ObjectFrame tcpA,
                                          MediaFlangeIOGroup gripperIO,
                                          Frame placePositionA, Frame prepickPlacePositionA, RobotContext context, boolean forceAlternate) throws Exception
    {
        log.info("Placing new workpiece with TCP A...");
        gripperIO.setGripper3_Switch(false);
        if (forceAlternate)
        {
            //TODO: Add alternate position
        } else
        {
            tcpA.move(ptp(prepickPlacePositionA));
            tcpA.move(lin(placePositionA));
            //gripperIO.setGripper1_Switch(false);
            gripperIO.setGripper3_Switch(true);
            gripperIO.setGripper3_PartPresence(true);
            ThreadUtil.milliSleep(GRIPPER_ACTIVATION_DELAY_MS);
            //gripperIO.setGripper3_Switch(true);
            gripperIO.setGripper1_Switch(false);
            ThreadUtil.milliSleep(GRIPPER_ACTIVATION_DELAY_MS);
            tcpA.move(lin(prepickPlacePositionA));
            log.info("New workpiece placed successfully with TCP A");
        }

    }
}
