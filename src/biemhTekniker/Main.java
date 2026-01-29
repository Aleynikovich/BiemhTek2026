package biemhTekniker;

import biemhTekniker.logger.Logger;
import biemhTekniker.managers.CalibrationManager;
import biemhTekniker.managers.LoggingManager;
import biemhTekniker.vision.SmartPickingThread;
import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.VisionInputsIOGroup;
import com.kuka.generated.ioAccess.VisionOutputsIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import javax.inject.Inject;

/**
 * Main robot application.
 * Manages program execution, logging, and vision system integration.
 */
public class Main extends RoboticsAPIApplication
{
    private static final Logger log = Logger.getLogger(Main.class);
    
    @Inject
    private LBR iiwa;
    
    @Inject
    private VisionInputsIOGroup visionInputs;
    
    @Inject
    private VisionOutputsIOGroup visionOutputs;

    // Configuration
    private static final String VISION_SERVER_IP = "172.31.1.69";
    private static final int VISION_SERVER_PORT = 59002;

    // Managers
    private LoggingManager loggingManager;
    private CalibrationManager calibrationManager;
    private SmartPickingThread smartPickingThread;

    /**
     * Current program number to execute.
     * This should be updated based on PLC signals or other external input.
     * Currently managed internally but designed to be set by external systems.
     */
    private volatile int programNumber = 0;

    @Override
    public void initialize()
    {
        // Initialize logging
        loggingManager = new LoggingManager();
        loggingManager.initialize();
        
        // Initialize calibration manager
        calibrationManager = new CalibrationManager(this, iiwa, VISION_SERVER_IP, VISION_SERVER_PORT);
        
        // Initialize and start SmartPicking thread
        smartPickingThread = new SmartPickingThread(visionInputs, visionOutputs, 
                                                    VISION_SERVER_IP, VISION_SERVER_PORT);
        smartPickingThread.initialize();
        smartPickingThread.start();
        
        // Set robot control parameters
        getApplicationControl().setApplicationOverride(0.5);
        getApplicationControl().clipManualOverride(0.00);
        
        log.info("Main application initialized");
    }

    @Override
    public void run()
    {
        log.info("Main application running");

        while (true)
        {
            switch (programNumber)
            {
                case 0:
                    // Program 0 - Idle
                    break;
                case 1:
                    // Program 1 - Reserved
                    break;
                case 2:
                    // Program 2 - Calibration
                    boolean calibrationSuccess = calibrationManager.executeCalibration();
                    if (calibrationSuccess) {
                        log.info("Calibration program completed successfully");
                    } else {
                        log.error("Calibration program failed");
                    }
                    programNumber = 0; // Return to idle after calibration
                    break;
                default:
                    log.warn("Unknown program number: " + programNumber);
                    break;
            }
            ThreadUtil.milliSleep(200);
        }
    }

    @Override
    public void dispose()
    {
        log.info("Main application shutting down");
        
        // Shutdown SmartPicking thread
        if (smartPickingThread != null) {
            smartPickingThread.shutdown();
            try {
                smartPickingThread.join(5000); // Wait up to 5 seconds for thread to finish
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for SmartPicking thread to finish");
                Thread.currentThread().interrupt();
            }
        }
        
        // Shutdown logging
        if (loggingManager != null) {
            loggingManager.shutdown();
        }
        
        super.dispose();
    }
}