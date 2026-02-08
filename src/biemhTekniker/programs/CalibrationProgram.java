package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import biemhTekniker.vision.SmartPickingProtocol;
import biemhTekniker.vision.VisionRoutines;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;

/**
 * Calibration program for the vision system.
 * Executes the full calibration sequence using the SmartPicking connection.
 * This program requires BOTH robot access and vision protocol access for coordinated operations.
 */
public class CalibrationProgram implements RobotProgram
{

    private static final Logger log = Logger.getLogger(CalibrationProgram.class);

    private SmartPickingProtocol protocol;

    /**
     * Sets the SmartPicking protocol dependency.
     * Must be called before execute() is invoked.
     * This is a special case - calibration needs both robot and vision access.
     *
     * @param protocol SmartPicking protocol connected to vision server
     */
    public void setProtocol(SmartPickingProtocol protocol)
    {
        this.protocol = protocol;
    }

    /**
     * Executes the calibration routine for the vision system.
     * Uses the existing connection maintained by SmartPickingThread.
     */
    public void execute(RobotContext context) throws Exception
    {
        log.info("Starting calibration program...");

        if (protocol == null)
        {
            throw new Exception("Protocol not set - call setProtocol() before execute()");
        }

        // Get dependencies from context
        LBR robot = context.getRobot();
        Tool gripper = context.getGripper();
        RoboticsAPIApplication app = context.getApplication();

        // Create calibration routine
        VisionRoutines calibration = new VisionRoutines(app, robot, protocol, robot.getFlange(), gripper);

        // Execute calibration
        boolean success = calibration.executeCalibration("/CalibrationPoints", "/CalibrationPoints/P16");

        if (success)
        {
            log.info("Calibration program completed successfully");
        } else
        {
            log.error("Calibration program failed");
            throw new Exception("Calibration failed");
        }
    }
}
