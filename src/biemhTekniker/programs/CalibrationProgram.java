package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import biemhTekniker.vision.SmartPickingProtocol;
import biemhTekniker.vision.VisionRoutines;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;

/**
 * Calibration program for the vision system.
 * Executes the full calibration sequence using the SmartPicking connection.
 */
public class CalibrationProgram
{

    private static final Logger log = Logger.getLogger(CalibrationProgram.class);

    private final RoboticsAPIApplication application;
    private final LBR                    robot;
    private final SmartPickingProtocol   protocol;

    /**
     * Creates a calibration program.
     *
     * @param application The robotics API application
     * @param robot       The LBR robot
     * @param protocol    SmartPicking protocol connected to vision server
     */
    public CalibrationProgram(RoboticsAPIApplication application, LBR robot, SmartPickingProtocol protocol)
    {
        this.application = application;
        this.robot       = robot;
        this.protocol    = protocol;
    }

    /**
     * Executes the calibration routine for the vision system.
     * Uses the existing connection maintained by SmartPickingThread.
     *
     * @return true if calibration completed successfully, false otherwise
     */
    public boolean execute()
    {
        log.info("Starting calibration program...");

        // Create calibration routine
        VisionRoutines calibration = new VisionRoutines(application, robot, protocol, robot.getFlange());

        // Execute calibration
        boolean success = calibration.executeCalibration("/CalibrationPoints", "/CalibrationPoints/P16");

        if (success)
        {
            log.info("Calibration program completed successfully");
        }
        else
        {
            log.error("Calibration program failed");
        }

        return success;
    }
}
