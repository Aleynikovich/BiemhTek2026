package biemhTekniker;

import biemhTekniker.console.ConsoleServer;
import biemhTekniker.console.ConsoleServerInterface;
import biemhTekniker.data.WorkpieceData;
import biemhTekniker.dispatcher.DefaultProgramTaskFactory;
import biemhTekniker.dispatcher.ProgramDispatcher;
import biemhTekniker.logger.Logger;
import biemhTekniker.managers.LoggingManager;
import biemhTekniker.programs.*;
import biemhTekniker.registry.ProgramRegistry;
import biemhTekniker.vision.SmartPickingThread;
import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.generated.ioAccess.RobotCartesianPositionIOGroup;
import com.kuka.generated.ioAccess.RobotSafetyIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;

import javax.inject.Inject;
import javax.inject.Named;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptpHome;

/**
 * Main robot application.
 * Manages program execution, logging, and vision system integration.
 * Implements ConsoleServerInterface for GUI control.
 * 
 * Configuration:
 * - CONFIG_SERVICE_BASE_URL: Base URL of the config service REST API
 *   Example: "http://172.31.1.100:8080"
 *   Set to empty string to disable config service integration
 */
public class Main extends RoboticsAPIApplication implements ConsoleServerInterface
{
    private static final Logger             log                = Logger.getLogger(Main.class);
    // Configuration
    private static final String             VISION_SERVER_IP   = "172.31.1.69";
    private static final int                VISION_SERVER_PORT = 59002;
    
    // Config Service Configuration
    // TODO: Load from properties file or external config for production
    private static final String             CONFIG_SERVICE_BASE_URL = "http://172.31.1.100:8080";
    
    @Inject private      LBR                iiwa;
    @Inject private      RobotSafetyIOGroup safetyIO;
    // Managers and threads
    private              LoggingManager     loggingManager;
    private              SmartPickingThread smartPickingThread;
    private              ConsoleServer      consoleServer;
    
    // Dispatcher and registry
    private              ProgramRegistry    programRegistry;
    private              ProgramDispatcher  programDispatcher;

    // Gripper data
    @Inject @Named("Gripper") // Matches the name defined in your Station Setup
    private Tool gripper;

    // Gripper IOs
    @Inject private MediaFlangeIOGroup gripperIO;

    @Inject private RobotCartesianPositionIOGroup currentCartesianPosition;

    // Shared data
    private WorkpieceData workpieceData;

    private volatile int programNumber = 0;

    @Override public void initialize()
    {
        // Initialize logging
        loggingManager = new LoggingManager();
        loggingManager.initialize();

        // Initialize shared data, workpiece data from camera
        workpieceData = new WorkpieceData();

        // Gripper
        gripper.attachTo(iiwa.getFlange());

        // Initialize and start SmartPicking thread
        smartPickingThread = new SmartPickingThread(VISION_SERVER_IP, VISION_SERVER_PORT);
        smartPickingThread.initialize();
        smartPickingThread.start();

        // Initialize and start console server for GUI control
        consoleServer = new ConsoleServer(this);
        consoleServer.initialize();

        // Set robot control parameters
        getApplicationControl().setApplicationOverride(0.5);
        getApplicationControl().clipManualOverride(0.0);

        // Initialize program registry and dispatcher
        if (CONFIG_SERVICE_BASE_URL != null && !CONFIG_SERVICE_BASE_URL.isEmpty()) {
            log.info("Initializing program registry with config service: " + CONFIG_SERVICE_BASE_URL);
            programRegistry = new ProgramRegistry(CONFIG_SERVICE_BASE_URL);
            programDispatcher = new ProgramDispatcher(programRegistry);
            
            // Register default task factory
            DefaultProgramTaskFactory factory = new DefaultProgramTaskFactory(
                    smartPickingThread.getProtocol(),
                    workpieceData,
                    CONFIG_SERVICE_BASE_URL
            );
            programDispatcher.registerFactory(factory);
            
            // Refresh program cache
            programRegistry.refreshCache();
        } else {
            log.warn("Config service not configured, dispatcher will not be available");
        }

        iiwa.getFlange().move(ptp(getApplicationData().getFrame("/BiemhHome")));
        iiwa.setHomePosition(iiwa.getCurrentJointPosition());
    }

    @Override public void dispose()
    {
        log.info("Main application shutting down");

        // Shutdown dispatcher
        if (programDispatcher != null)
        {
            programDispatcher.shutdown();
        }

        // Shutdown console server
        if (consoleServer != null)
        {
            consoleServer.dispose();
        }

        // Shutdown SmartPicking thread
        if (smartPickingThread != null)
        {
            smartPickingThread.shutdown();
            try
            {
                smartPickingThread.join(15000); // Increased timeout to 15 seconds
                if (smartPickingThread.isAlive())
                {
                    log.warn("SmartPicking thread did not stop gracefully, interrupting");
                    smartPickingThread.interrupt();
                }
            }
            catch (InterruptedException e)
            {
                log.warn("Interrupted while waiting for SmartPicking thread to finish");
                Thread.currentThread().interrupt();
            }
        }

        // Shutdown logging
        if (loggingManager != null)
        {
            loggingManager.shutdown();
        }

        super.dispose();
    }

    @Override public void run() throws Exception
    {
        log.info("Main application running, entering main loop.");

        while (true)
        {
            iiwa.move(ptpHome());
            
            if (programNumber != 0)
            {
                // Use dispatcher if available, otherwise fall back to legacy switch
                if (programDispatcher != null)
                {
                    // Check vision connection for vision tasks
                    if (programNumber >= 1 && programNumber <= 3) {
                        if (!checkVisionConnection()) {
                            programNumber = 0;
                            ThreadUtil.milliSleep(200);
                            continue;
                        }
                    }
                    
                    // Dispatch the program
                    // The dispatcher will handle async execution for VISION tasks
                    // and sync execution for ROBOT tasks
                    final int currentProgram = programNumber;
                    programDispatcher.dispatch(currentProgram, new Runnable() {
                        @Override
                        public void run() {
                            programNumber = 0;
                        }
                    });
                    
                    // For ROBOT tasks, the dispatcher blocks until complete
                    // For VISION tasks, it returns immediately and callback resets programNumber
                }
                else
                {
                    // Legacy fallback if dispatcher not configured
                    log.warn("Dispatcher not configured, using legacy switch statement");
                    executeLegacyProgram(programNumber);
                    programNumber = 0;
                }
            }
            
            ThreadUtil.milliSleep(200);
        }
    }
    
    /**
     * Legacy program execution using switch statement.
     * Used as fallback when dispatcher is not configured.
     */
    private void executeLegacyProgram(int progNum)
    {
        switch (progNum)
        {
            case 0:
                // Program 0 - Idle
                break;

            case 1:
                // Program 1 - Get New Workpiece Position
                getNewWorkpiecePosition();
                break;

            case 2:
                // Program 2 - Calibration
                executeCalibration();
                break;

            case 3:
                // Program 3 - Test Calibration
                testCalibration();
                break;

            case 4:
                // Program 4 - Pick New Workpiece
                pickNewWorkpiece();
                break;

            case 5:
                // Program 5 - Place New Workpiece
                placeNewWorkpiece();
                break;

            case 6:
                // Program 6 - Pick Measured Workpiece
                pickMeasuredWorkpiece();
                break;

            case 7:
                // Program 7 - Place Measured Workpiece
                placeMeasuredWorkpiece();
                break;

            default:
                log.warn("Unknown program number: " + progNum);
                break;
        }
    }

    // ========== ConsoleServerInterface Implementation ==========

    private void getNewWorkpiecePosition()
    {
        if (!checkVisionConnection())
        {
            return;
        }

        GetNewWorkpiecePositionProgram program = new GetNewWorkpiecePositionProgram();
        program.setDependencies(smartPickingThread.getProtocol(), workpieceData);

        try
        {
            program.run();
            log.info("Get New Workpiece Position program completed successfully");
        }
        catch (Exception e)
        {
            log.error("Get New Workpiece Position program failed: " + e.getMessage());
        }
    }

    private void executeCalibration()
    {
        if (!checkVisionConnection())
        {
            return;
        }

        CalibrationProgram program = new CalibrationProgram();
        program.setProtocol(smartPickingThread.getProtocol());

        try
        {
            program.run();
            log.info("Calibration program completed successfully");
        }
        catch (Exception e)
        {
            log.error("Calibration program failed: " + e.getMessage());
        }
    }

    private void testCalibration()
    {
        if (!checkVisionConnection())
        {
            return;
        }

        TestCalibrationProgram program = new TestCalibrationProgram();
        program.setProtocol(smartPickingThread.getProtocol());

        try
        {
            program.run();
            log.info("Test Calibration program completed successfully");
        }
        catch (Exception e)
        {
            log.error("Test Calibration program failed: " + e.getMessage());
        }
    }

    private void pickNewWorkpiece()
    {
        //TODO REMOVE HARDCODE
        // Check if workpieceData is null; if so, create a new instance
/*        if (this.workpieceData == null) {
        	log.warn("No workpiece data, generating dummy workpiece");
            this.workpieceData = new WorkpieceData();
        }
        if (this.workpieceData.getScore() == 0)
        {
        	log.warn("Workpiece has been instanced but contains no data, populating dummy workpiece.");
            workpieceData.set(300.0, -320, 200,-180 , 0.0,45, 0.95);
        }*/

        //REMOVE HARDCODE IN PRODUCTION


        PickNewWorkpieceProgram program = new PickNewWorkpieceProgram();
        program.setWorkpieceData(workpieceData);

        try
        {
            program.run();
            log.info("Pick New Workpiece program completed successfully");
        }
        catch (Exception e)
        {
            log.error("Pick New Workpiece program failed: " + e.getMessage());
        }
    }

    // ========== Program Execution Methods ==========

    private void placeNewWorkpiece()
    {
        PlaceNewWorkpieceProgram program = new PlaceNewWorkpieceProgram();

        try
        {
            program.run();
            log.info("Place New Workpiece program completed successfully");
        }
        catch (Exception e)
        {
            log.error("Place New Workpiece program failed: " + e.getMessage());
        }
    }

    private void pickMeasuredWorkpiece()
    {
        PickMeasuredWorkpieceProgram program = new PickMeasuredWorkpieceProgram();

        try
        {
            program.run();
            log.info("Pick Measured Workpiece program completed successfully");
        }
        catch (Exception e)
        {
            log.error("Pick Measured Workpiece program failed: " + e.getMessage());
        }
    }

    private void placeMeasuredWorkpiece()
    {
        PlaceMeasuredWorkpieceProgram program = new PlaceMeasuredWorkpieceProgram();

        try
        {
            program.run();
            log.info("Place Measured Workpiece program completed successfully");
        }
        catch (Exception e)
        {
            log.error("Place Measured Workpiece program failed: " + e.getMessage());
        }
    }

    private boolean checkVisionConnection()
    {
        if (!smartPickingThread.isConnected())
        {
            log.error("Cannot execute program - not connected to vision server");
            return false;
        }
        return true;
    }

    @Override public void setProgramNumber(int programNumber)
    {
        if (programNumber >= 0 && programNumber <= 7)
        {
            this.programNumber = programNumber;
            log.info("Program number set to: " + programNumber + " via console");
        }
        else
        {
            log.warn("Invalid program number requested: " + programNumber);
        }
    }

    @Override public int getCurrentProgram()
    {
        return programNumber;
    }

    // ========== Helper Methods ==========

    @Override public boolean isVisionConnected()
    {
        return smartPickingThread != null && smartPickingThread.isConnected();
    }

    @Override public String getWorkpiecePosition()
    {
        if (workpieceData != null && workpieceData.isValid())
        {
            return workpieceData.toString();
        }
        return "invalid";
    }
}
