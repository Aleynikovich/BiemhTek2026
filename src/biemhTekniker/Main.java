package biemhTekniker;

import biemhTekniker.logger.Logger;
import biemhTekniker.managers.LoggingManager;
import biemhTekniker.programs.CalibrationProgram;
import biemhTekniker.vision.SmartPickingThread;
import com.kuka.common.ThreadUtil;
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

    // Configuration
    private static final String VISION_SERVER_IP = "172.31.1.69";
    private static final int VISION_SERVER_PORT = 59002;

    // Managers and threads
    private LoggingManager loggingManager;
    private SmartPickingThread smartPickingThread;

    /**
     * Current program number to execute.
     * This should be updated based on PLC signals or other external input.
     */
    private volatile int programNumber = 2;

    @Override
    public void initialize()
    {
        // Initialize logging
        loggingManager = new LoggingManager();
        loggingManager.initialize();
        
        // Initialize and start SmartPicking thread (maintains connection to vision server)
        smartPickingThread = new SmartPickingThread(VISION_SERVER_IP, VISION_SERVER_PORT);
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
                    executeCalibration();
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

    /**
     * Executes calibration program using the SmartPicking connection.
     */
    private void executeCalibration() {
        if (!smartPickingThread.isConnected()) {
            log.error("Cannot execute calibration - not connected to vision server");
            return;
        }
        
        CalibrationProgram calibration = new CalibrationProgram(
                this, 
                iiwa, 
                smartPickingThread.getProtocol()
        );
        
        boolean success = calibration.execute();
        if (success) {
            log.info("Calibration program completed successfully");
        } else {
            log.error("Calibration program failed");
        }
    }
}