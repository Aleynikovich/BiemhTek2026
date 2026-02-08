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

import javax.inject.Inject;
import javax.inject.Named;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.lin;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

/**
 * Program to pick a new workpiece using position from GetNewWorkpiecePositionProgram.
 */
public class PickNewWorkpieceProgram extends RoboticsAPIApplication
{

    private static final Logger log = Logger.getLogger(PickNewWorkpieceProgram.class);

    @Inject
    private LBR iiwa;

    @Inject
    @Named("Gripper")
    private Tool gripper;

    @Inject
    private MediaFlangeIOGroup gripperIO;

    private WorkpieceData workpieceData;

    /**
     * Sets the workpiece data dependency.
     * Called from Main before run() is invoked.
     *
     * @param workpieceData Shared workpiece data object
     */
    public void setWorkpieceData(WorkpieceData workpieceData)
    {
        this.workpieceData = workpieceData;
    }

    /**
     * Executes the pick operation for a new workpiece.
     */
    @Override
    public void run() throws Exception
    {
        log.info("Picking new workpiece...");

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

        gripper.move(ptp(getApplicationData().getFrame("/BiemhHome")));
    }
}
