package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import biemhTekniker.vision.SmartPickingProtocol;
import biemhTekniker.vision.VisionRoutines;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;

/**
 * Program to test the calibration of the vision system.
 */
public class TestCalibrationProgram
{

    private static final Logger log = Logger.getLogger(TestCalibrationProgram.class);

    private final RoboticsAPIApplication application;
    private final LBR                    robot;
    private final SmartPickingProtocol   protocol;
    private final Tool gripper;

    public TestCalibrationProgram(RoboticsAPIApplication application, LBR robot, SmartPickingProtocol protocol, Tool gripper)
    {
        this.application = application;
        this.robot       = robot;
        this.protocol    = protocol;
        this.gripper	= gripper;
    }

    /**
     * Executes the calibration test routine.
     *
     * @return true if test passed, false otherwise
     */
    public boolean execute()
    {
        log.info("Testing calibration...");

        VisionRoutines routines = new VisionRoutines(application, robot, protocol, robot.getFlange(),gripper);

        // Test calibration at a specific point
        boolean success = routines.testCalibration("/CalibrationPoints/P16");

        if (success)
        {
            log.info("Calibration test passed");
        }
        else
        {
            log.error("Calibration test failed");
        }

        return success;
    }
}
