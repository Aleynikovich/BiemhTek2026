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

/**
 * Program to place a new workpiece at a predefined location.
 */
public class PlaceNewWorkpieceProgram
{

    private static final Logger log = Logger.getLogger(PlaceNewWorkpieceProgram.class);

    private final RoboticsAPIApplication application;
    private final LBR                    iiwa;
    private final Tool                   gripper;
    private final MediaFlangeIOGroup     gripperIO;

    public PlaceNewWorkpieceProgram(RoboticsAPIApplication application, LBR robot, Tool gripper, MediaFlangeIOGroup gripperIO)
    {
        this.application = application;
        this.iiwa       = robot;
        this.gripper     = gripper;
        this.gripperIO   = gripperIO;
    }

    /**
     * Executes the place operation for a new workpiece.
     *
     * @return true if place succeeded, false otherwise
     */
    public boolean execute()
    {
        log.info("Placing new workpiece...");

        ObjectFrame tcpA = gripper.getFrame("TCPA");

        tcpA.move(ptp(application.getApplicationData().getFrame("/SchunkBase/App1")));
        tcpA.move(ptp(application.getApplicationData().getFrame("/SchunkBase/App2")));
        tcpA.move(lin(application.getApplicationData().getFrame("/SchunkBase/PickPlace")));
        gripperIO.setGripper1_Switch(false);
        ThreadUtil.milliSleep(500);
        tcpA.move(lin(application.getApplicationData().getFrame("/SchunkBase/Exit")));
        iiwa.move(ptp(application.getApplicationData().getFrame("/BiemhHome")));
        

        log.warn("PlaceNewWorkpieceProgram: Motion not yet implemented");
        return true;
    }
}
