package biemhTekniker.programs;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.lin;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

import biemhTekniker.logger.Logger;

import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Program to place a new workpiece at a predefined location.
 */
public class PlaceNewWorkpieceProgram extends RoboticsAPIApplication
{

    private static final Logger log = Logger.getLogger(PlaceNewWorkpieceProgram.class);

    @Inject
    private LBR iiwa;

    @Inject
    @Named("Gripper")
    private Tool gripper;

    @Inject
    private MediaFlangeIOGroup gripperIO;

    /**
     * Executes the place operation for a new workpiece.
     */
    @Override
    public void run() throws Exception
    {
        log.info("Placing new workpiece...");

        ObjectFrame tcpA = gripper.getFrame("TCPA");

        tcpA.move(ptp(getApplicationData().getFrame("/SchunkBase/App1")));
        tcpA.move(ptp(getApplicationData().getFrame("/SchunkBase/App2")));
        tcpA.move(lin(getApplicationData().getFrame("/SchunkBase/PickPlace")));
        gripperIO.setGripper1_Switch(false);
        gripperIO.setGripper2_Switch(false);
        ThreadUtil.milliSleep(500);
        tcpA.move(lin(getApplicationData().getFrame("/SchunkBase/Exit")));
        tcpA.move(ptp(getApplicationData().getFrame("/BiemhHome")));


        log.warn("PlaceNewWorkpieceProgram: Motion not yet implemented");
    }
}
