package biemhTekniker;

import biemhTekniker.calibration.CalibrationRoutine;
import biemhTekniker.logger.LogCollector;
import biemhTekniker.logger.LogManager;
import biemhTekniker.logger.LogPublisher;
import biemhTekniker.logger.Logger;
import biemhTekniker.vision.SmartPickingProtocol;
import biemhTekniker.vision.VisionDataBridge;
import biemhTekniker.vision.VisionSocketClient;
import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import javax.inject.Inject;

@SuppressWarnings("unused")
public class Main extends RoboticsAPIApplication
{
    @Inject
    private LBR iiwa;
    
	private String VisionServerIP = "172.31.1.69";  
	private int VisionServerPort = 59002;
	
	private boolean calibrationSuccess = true;
	
    private LogPublisher _logPublisher;
    private static final Logger log = Logger.getLogger(Main.class);

    @Override
    public void initialize()
    {
        initializeLogging();
        getApplicationControl().setApplicationOverride(0.5);
        getApplicationControl().clipManualOverride(0.00);
    }

    @Override
    public void run()
    {
        log.info("Main Application Running. Monitoring Vision Bridge...");

        while (true)
        {
            // Check if the Background Task has put new data in the bridge
           // if (VisionDataBridge.get().hasNewData()) {
             //   displayPartData();
            //}
            
            if (!calibrationSuccess)
            {
            	calibrationSuccess = executeCalibration(VisionServerIP, VisionServerPort);
            }
            

            ThreadUtil.milliSleep(100);
        }
    }

    private void displayPartData() {
        VisionDataBridge bridge = VisionDataBridge.get();

        // Log the raw data from the bridge
        log.info(">>> NEW PART DETECTED <<<");
        log.info(String.format("Position (m): X=%.4f, Y=%.4f, Z=%.4f",
                bridge.getX(), bridge.getY(), bridge.getZ()));
        log.info(String.format("Rotation (rad): Rx=%.4f, Ry=%.4f, Rz=%.4f",
                bridge.getRx(), bridge.getRy(), bridge.getRz()));

        // CRITICAL: Mark data as 'consumed' so we don't log the same part 100 times a second
        bridge.consume();
    }

    @Override
    public void dispose()
    {
        if (_logPublisher != null) _logPublisher.stop();
        super.dispose();
    }

    public void initializeLogging()
    {
        try
        {
            LogCollector _logCollector = new LogCollector();
            LogManager.register(_logCollector);

            _logPublisher = new LogPublisher(_logCollector);
            _logPublisher.start();

            log.info("Logging initialized");

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes the calibration routine for the vision system.
     * This method can be called separately to perform calibration.
     * 
     * @param visionServerIP IP address of the vision system server
     * @param visionServerPort Port number of the vision system server
     * @return true if calibration completed successfully, false otherwise
     */
    public boolean executeCalibration(String visionServerIP, int visionServerPort) {
        log.info("Starting calibration sequence...");

        // Create vision client and protocol
        VisionSocketClient visionClient = new VisionSocketClient(visionServerIP, visionServerPort);
        if (!visionClient.connect()) {
            log.error("Failed to connect to vision server");
            return false;
        }
      

        SmartPickingProtocol protocol = new SmartPickingProtocol(visionClient);

        // Create calibration routine
        CalibrationRoutine calibration = new CalibrationRoutine(
                this,
                iiwa, 
                protocol, 
                iiwa.getFlange()
        );

        // Execute calibration
        // Note: Pass null for test frame if not defined in RoboticsAPI.data.xml
        // To use a test frame, define it in the XML (e.g., "/CalibrationPoints/Test")
        boolean success = calibration.executeCalibration(
                "/CalibrationPoints",
                null
        );

        // Clean up
        visionClient.close();

        if (success) {
            log.info("Calibration completed successfully");
        } else {
            log.error("Calibration failed");
        }

        return success;
    }
}