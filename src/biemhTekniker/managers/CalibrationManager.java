package biemhTekniker.managers;

import biemhTekniker.calibration.CalibrationRoutine;
import biemhTekniker.logger.Logger;
import biemhTekniker.vision.SmartPickingProtocol;
import biemhTekniker.vision.VisionSocketClient;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;

/**
 * Manages calibration routine execution.
 * Handles vision client connection and calibration sequence.
 */
public class CalibrationManager {
    
    private static final Logger log = Logger.getLogger(CalibrationManager.class);
    
    private final RoboticsAPIApplication application;
    private final LBR robot;
    private final String visionServerIP;
    private final int visionServerPort;
    
    /**
     * Creates a calibration manager.
     * @param application The robotics API application
     * @param robot The LBR robot
     * @param visionServerIP Vision server IP address
     * @param visionServerPort Vision server port
     */
    public CalibrationManager(RoboticsAPIApplication application, LBR robot, 
                            String visionServerIP, int visionServerPort) {
        this.application = application;
        this.robot = robot;
        this.visionServerIP = visionServerIP;
        this.visionServerPort = visionServerPort;
    }
    
    /**
     * Executes the calibration routine for the vision system.
     * @return true if calibration completed successfully, false otherwise
     */
    public boolean executeCalibration() {
        log.info("Starting calibration sequence...");
        
        VisionSocketClient visionClient = new VisionSocketClient(visionServerIP, visionServerPort);
        
        try {
            // Create vision client and protocol
            if (!visionClient.connect()) {
                log.error("Failed to connect to vision server");
                return false;
            }
            
            SmartPickingProtocol protocol = new SmartPickingProtocol(visionClient);
            
            // Create calibration routine
            CalibrationRoutine calibration = new CalibrationRoutine(
                    application,
                    robot,
                    protocol,
                    robot.getFlange()
            );
            
            // Execute calibration
            // Note: Pass null for test frame if not defined in RoboticsAPI.data.xml
            // To use a test frame, define it in the XML (e.g., "/CalibrationPoints/Test")
            boolean success = calibration.executeCalibration(
                    "/CalibrationPoints",
                    "/CalibrationPoints/P16"
            );
            
            if (success) {
                log.info("Calibration completed successfully");
            } else {
                log.error("Calibration failed");
            }
            
            return success;
            
        } finally {
            // Ensure vision client is always closed
            visionClient.close();
        }
    }
}
