package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import biemhTekniker.vision.SmartPickingProtocol;
import biemhTekniker.vision.VisionRoutines;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Calibration program for the vision system.
 * Executes the full calibration sequence using the SmartPicking connection.
 */
public class CalibrationProgram extends RoboticsAPIApplication
{

    private static final Logger log = Logger.getLogger(CalibrationProgram.class);

    @Inject
    private LBR iiwa;

    @Inject
    @Named("Gripper")
    private Tool gripper;

    private SmartPickingProtocol protocol;

    /**
     * Sets the SmartPicking protocol dependency.
     * Called from Main before run() is invoked.
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
    @Override
    public void run() throws Exception
    {
        log.info("Starting calibration program...");

        // Create calibration routine
        VisionRoutines calibration = new VisionRoutines(this, iiwa, protocol, iiwa.getFlange(), gripper);

        // Execute calibration
        boolean success = calibration.executeCalibration("/CalibrationPoints", "/CalibrationPoints/P16");

        if (success)
        {
            log.info("Calibration program completed successfully");
        }
        else
        {
            log.error("Calibration program failed");
            throw new Exception("Calibration failed");
        }
    }
}
