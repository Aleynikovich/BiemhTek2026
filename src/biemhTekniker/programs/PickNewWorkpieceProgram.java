package biemhTekniker.programs;

import biemhTekniker.data.WorkpieceData;
import biemhTekniker.logger.Logger;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
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
    private final LBR iiwa;
    private final WorkpieceData workpieceData;
    private final Tool gripper;
    private final MediaFlangeIOGroup gripperIO;

    public PickNewWorkpieceProgram(RoboticsAPIApplication application, LBR robot, WorkpieceData workpieceData, Tool gripper, MediaFlangeIOGroup gripperIO)
    {
        this.application = application;
        this.iiwa = robot;
        this.workpieceData = workpieceData;
        this.gripper = gripper;
        this.gripperIO = gripperIO;
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

        ObjectFrame tcp = gripper.getFrame("TCPA");
        Frame pickPosition = workpieceData.getWorkPiecePickFrame();
        Frame prePickPosition = workpieceData.getWorkPiecePickFrame();
        Frame rotatePosition = workpieceData.getWorkPiecePickFrame();
        prePickPosition.setZ(prePickPosition.getZ() + 100);
        rotatePosition.setAlphaRad(rotatePosition.getAlphaRad() + Math.toRadians(10));


        tcp.move(ptp(prePickPosition).setJointVelocityRel(0.5));
        tcp.move(lin(pickPosition).setJointVelocityRel(0.25));
        //tcp.move(ptp(pickPosition.transform(Transformation.ofDeg(0, 0, 100, 0, 0, 0))).setJointVelocityRel(0.5));
        tcp.move(ptp(rotatePosition).setJointVelocityRel(0.5));
        rotatePosition.setZ(rotatePosition.getZ() + 100);
        tcp.move(lin(rotatePosition).setJointVelocityRel(0.25));
        gripper.move(ptp(application.getApplicationData().getFrame("/BiemhHome")));

        return true;
    }
}
