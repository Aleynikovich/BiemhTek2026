package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.lin;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

/**
 * Program to place a new workpiece at a predefined location.
 */
public class PlaceNewWorkpieceProgram implements RobotProgram
{

    private static final Logger log = Logger.getLogger(PlaceNewWorkpieceProgram.class);

    /**
     * Executes the place operation for a new workpiece.
     */
    public void execute(RobotContext context) throws Exception
    {
        log.info("Placing new workpiece...");

        // Get dependencies from context
        Tool gripper = context.getGripper();
        MediaFlangeIOGroup gripperIO = context.getGripperIO();
        RoboticsAPIApplication app = context.getApplication();

        ObjectFrame tcpA = gripper.getFrame("TCPA");

        tcpA.move(ptp(app.getApplicationData().getFrame("/SchunkBase/App1")));
        tcpA.move(ptp(app.getApplicationData().getFrame("/SchunkBase/App2")));
        tcpA.move(lin(app.getApplicationData().getFrame("/SchunkBase/PickPlace")));
        gripperIO.setGripper1_Switch(false);
        gripperIO.setGripper2_Switch(false);
        ThreadUtil.milliSleep(500);
        tcpA.move(lin(app.getApplicationData().getFrame("/SchunkBase/Exit")));
        tcpA.move(ptp(app.getApplicationData().getFrame("/BiemhHome")));


        log.warn("PlaceNewWorkpieceProgram: Motion not yet implemented");
    }
}
