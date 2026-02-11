package biemhTekniker.programs.robot;

import biemhTekniker.lib.logger.Logger;
import biemhTekniker.lib.robot.RobotProgram;
import biemhTekniker.lib.vision.SmartPickingProtocol;
import biemhTekniker.lib.vision.VisionRoutines;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;

/**
 * Program to test the calibration of the vision system.
 * This program requires BOTH robot access and vision protocol access for coordinated operations.
 */
public class TestCalibrationProgram implements RobotProgram
{

    private static final Logger log = Logger.getLogger(TestCalibrationProgram.class);

    private SmartPickingProtocol protocol;

    /**
     * Sets the SmartPicking protocol dependency.
     * Must be called before execute() is invoked.
     * This is a special case - calibration test needs both robot and vision access.
     *
     * @param protocol SmartPicking protocol connected to vision server
     */
    public void setProtocol(SmartPickingProtocol protocol)
    {
        this.protocol = protocol;
    }

    /**
     * Executes the calibration test routine.
     */
    public void execute(RobotContext context) throws Exception
    {
        log.info("Testing calibration...");

        if (protocol == null)
        {
            throw new Exception("Protocol not set - call setProtocol() before execute()");
        }

        // Get dependencies from context
        LBR robot = context.getRobot();
        Tool gripper = context.getGripper();
        RoboticsAPIApplication app = context.getApplication();

        VisionRoutines routines = new VisionRoutines(app, robot, protocol, robot.getFlange(), gripper);

        // Test calibration at a specific point
        boolean success = routines.testCalibration("/CalibrationPoints/P16");

        if (success)
        {
            log.info("Calibration test passed");
        } else
        {
            log.error("Calibration test failed");
            throw new Exception("Calibration test failed");
        }
    }
}
