package biemhTekniker.programs;

import biemhTekniker.data.WorkpieceData;
import biemhTekniker.logger.Logger;
import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.executionModel.CommandInvalidException;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.lin;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

/**
 * Program to pick a new workpiece using position from GetNewWorkpiecePositionProgram.
 */
public class PickNewWorkpieceProgram
{

    private static final Logger log = Logger.getLogger(PickNewWorkpieceProgram.class);

    private final RoboticsAPIApplication application;
    private final LBR                    iiwa;
    private final WorkpieceData          workpieceData;
    private final Tool                   gripper;
    private final MediaFlangeIOGroup     gripperIO;

    public PickNewWorkpieceProgram(RoboticsAPIApplication application, LBR robot, WorkpieceData workpieceData, Tool gripper, MediaFlangeIOGroup gripperIO)
    {
        this.application   = application;
        this.iiwa          = robot;
        this.workpieceData = workpieceData;
        this.gripper       = gripper;
        this.gripperIO     = gripperIO;
    }

    /**
     * Executes the pick operation for a new workpiece.
     *
     * @return true if pick succeeded, false otherwise
     */
    public boolean execute()
    {
        log.info("Picking new workpiece...");

        if (!workpieceData.isValid())
        {
            log.error("Cannot pick workpiece - no valid position data available");
            return false;
        }

        log.debug("Using workpiece position: " + workpieceData);
        gripperIO.setGripper1_Switch(false);
        gripperIO.setGripper2_Switch(false);
        // Gripper TCP declaration A=1, B=2
        ObjectFrame tcpA = gripper.getFrame("TCPA");
        ObjectFrame tcpB = gripper.getFrame("TCPB");

        // Frame sent by camera
        Frame pickPosition    = workpieceData.getWorkPiecePickFrame();
        Frame prePickPosition = workpieceData.getWorkPiecePickFrame();

        // Pick position with offset
        prePickPosition.setZ(prePickPosition.getZ() + 100);


        // Alternate picking positions, 180deg
        Frame alternatePickPosition    = pickPosition;
        Frame alternatePrePickPosition = prePickPosition;
        //alternatePickPosition.setAlphaRad(alternatePickPosition.getAlphaRad() + Math.PI);
        //alternatePrePickPosition.setAlphaRad(alternatePrePickPosition.getAlphaRad() + Math.PI);

        try
        {
            log.info("Attempting to pick with regular position, gripperA1: " + pickPosition);
            tcpA.move(ptp(prePickPosition).setJointVelocityRel(0.5));
            tcpA.move(lin(pickPosition).setJointVelocityRel(0.25));
            gripperIO.setGripper1_Switch(true);
            ThreadUtil.milliSleep(500);
            tcpA.move(lin(prePickPosition).setJointVelocityRel(0.25));
        }
        catch (CommandInvalidException e)
        {
            log.error(e.getMessage());
            try
            {
                log.info("Attempting to pick with alternate position, gripperA1: " + alternatePickPosition);
                tcpA.move(ptp(alternatePrePickPosition).setJointVelocityRel(0.5));
                tcpA.move(lin(alternatePickPosition).setJointVelocityRel(0.25));
                gripperIO.setGripper1_Switch(true);
                ThreadUtil.milliSleep(500);
                tcpA.move(lin(alternatePrePickPosition).setJointVelocityRel(0.25));
            }
            catch (CommandInvalidException e2)
            {
                log.error(e.getMessage());
                try
                {
                    log.info("Attempting to pick with regular position, gripperB2: " + pickPosition);
                    tcpB.move(ptp(prePickPosition).setJointVelocityRel(0.5));
                    tcpB.move(lin(pickPosition).setJointVelocityRel(0.25));
                    gripperIO.setGripper2_Switch(true);
                    ThreadUtil.milliSleep(500);
                    tcpB.move(lin(prePickPosition).setJointVelocityRel(0.25));
                }
                catch (CommandInvalidException e3)
                {
                    log.info("Attempting to pick with alternate position, gripperB2: " + alternatePickPosition);
                    tcpB.move(ptp(alternatePrePickPosition).setJointVelocityRel(0.5));
                    tcpB.move(lin(alternatePickPosition).setJointVelocityRel(0.25));
                    gripperIO.setGripper2_Switch(true);
                    ThreadUtil.milliSleep(500);
                    tcpB.move(lin(alternatePrePickPosition).setJointVelocityRel(0.25));
                }


            }
        }

        gripper.move(ptp(application.getApplicationData().getFrame("/BiemhHome")));

        return true;
    }
}
