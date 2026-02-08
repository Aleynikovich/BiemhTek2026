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
 * Program to test the calibration of the vision system.
 */
public class TestCalibrationProgram extends RoboticsAPIApplication
{

    private static final Logger log = Logger.getLogger(TestCalibrationProgram.class);

    @Inject private LBR iiwa;

    @Inject @Named("Gripper") private Tool gripper;

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
     * Executes the calibration test routine.
     */
    @Override public void run() throws Exception
    {
        log.info("Testing calibration...");

        VisionRoutines routines = new VisionRoutines(this, iiwa, protocol, iiwa.getFlange(), gripper);

        // Test calibration at a specific point
        boolean success = routines.testCalibration("/CalibrationPoints/P16");

        if (success)
        {
            log.info("Calibration test passed");
        }
        else
        {
            log.error("Calibration test failed");
            throw new Exception("Calibration test failed");
        }
    }
}
