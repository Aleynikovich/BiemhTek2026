package biemhTekniker;

import biemhTekniker.console.ConsoleServer;
import biemhTekniker.console.ConsoleServerInterface;
import biemhTekniker.data.WorkpieceData;
import biemhTekniker.logger.Logger;
import biemhTekniker.managers.LoggingManager;
import biemhTekniker.programs.*;
import biemhTekniker.vision.SmartPickingThread;
import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Main robot application.
 * Manages program execution, logging, and vision system integration.
 * Implements ConsoleServerInterface for GUI control.
 */
public class Main extends RoboticsAPIApplication implements ConsoleServerInterface
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
    private ConsoleServer consoleServer;
    
    //Gripper data
    @Inject
    @Named("Gripper") // Matches the name defined in your Station Setup
    private Tool gripper;
    
    // Shared data
    private WorkpieceData workpieceData;

    /**
     * Current program number to execute.
     * This should be updated based on PLC signals or other external input.
     */
    private volatile int programNumber = 0;

    @Override
    public void initialize()
    {
        // Initialize logging
        loggingManager = new LoggingManager();
        loggingManager.initialize();
        
        // Initialize shared data
        workpieceData = new WorkpieceData();
        
        // Gripper
        gripper.attachTo(iiwa.getFlange());
        
        // Initialize and start SmartPicking thread (maintains connection to vision server)
        smartPickingThread = new SmartPickingThread(VISION_SERVER_IP, VISION_SERVER_PORT);
        smartPickingThread.initialize();
        smartPickingThread.start();
        
        // Initialize and start console server for GUI control
        consoleServer = new ConsoleServer(this);
        consoleServer.initialize();
        
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
                    // Program 1 - Get New Workpiece Position
                    getNewWorkpiecePosition();
                    programNumber = 0;
                    break;
                    
                case 2:
                    // Program 2 - Calibration
                    executeCalibration();
                    programNumber = 0;
                    break;
                    
                case 3:
                    // Program 3 - Test Calibration
                    testCalibration();
                    programNumber = 0;
                    break;
                    
                case 4:
                    // Program 4 - Pick New Workpiece
                    pickNewWorkpiece();
                    programNumber = 0;
                    break;
                    
                case 5:
                    // Program 5 - Place New Workpiece
                    placeNewWorkpiece();
                    programNumber = 0;
                    break;
                    
                case 6:
                    // Program 6 - Pick Measured Workpiece
                    pickMeasuredWorkpiece();
                    programNumber = 0;
                    break;
                    
                case 7:
                    // Program 7 - Place Measured Workpiece
                    placeMeasuredWorkpiece();
                    programNumber = 0;
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
        
        // Shutdown console server
        if (consoleServer != null) {
            consoleServer.dispose();
        }
        
        // Shutdown SmartPicking thread
        if (smartPickingThread != null) {
            smartPickingThread.shutdown();
            try {
                smartPickingThread.join(5000);
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

    // ========== ConsoleServerInterface Implementation ==========

    @Override
    public void setProgramNumber(int programNumber) {
        if (programNumber >= 0 && programNumber <= 7) {
            this.programNumber = programNumber;
            log.info("Program number set to: " + programNumber + " via console");
        } else {
            log.warn("Invalid program number requested: " + programNumber);
        }
    }

    @Override
    public int getCurrentProgram() {
        return programNumber;
    }

    @Override
    public boolean isVisionConnected() {
        return smartPickingThread != null && smartPickingThread.isConnected();
    }

    @Override
    public String getWorkpiecePosition() {
        if (workpieceData != null && workpieceData.isValid()) {
            return workpieceData.toString();
        }
        return "invalid";
    }

    // ========== Program Execution Methods ==========

    private void getNewWorkpiecePosition() {
        if (!checkVisionConnection()) return;
        
        GetNewWorkpiecePositionProgram program = new GetNewWorkpiecePositionProgram(
                smartPickingThread.getProtocol(),
                workpieceData
        );
        
        boolean success = program.execute();
        logProgramResult("Get New Workpiece Position", success);
    }

    private void executeCalibration() {
        if (!checkVisionConnection()) return;
        
        CalibrationProgram program = new CalibrationProgram(
                this, 
                iiwa, 
                smartPickingThread.getProtocol()
        );
        
        boolean success = program.execute();
        logProgramResult("Calibration", success);
    }

    private void testCalibration() {
        if (!checkVisionConnection()) return;
        
        TestCalibrationProgram program = new TestCalibrationProgram(
                this, 
                iiwa, 
                smartPickingThread.getProtocol()
        );
        
        boolean success = program.execute();
        logProgramResult("Test Calibration", success);
    }

    private void pickNewWorkpiece() {
    	//TODO REMOVE HARDCODE
    	// Check if workpieceData is null; if so, create a new instance
        if (this.workpieceData == null) {
            this.workpieceData = new WorkpieceData();
        }
        workpieceData.set(300.0, -320, 400,Math.toRadians(-165) , 0.0, Math.toRadians(58), 0.95);
        //REMOVE HARDCODE
        PickNewWorkpieceProgram program = new PickNewWorkpieceProgram(
                this, 
                iiwa, 
                workpieceData,
                gripper
        );
        
        boolean success = program.execute();
        logProgramResult("Pick New Workpiece", success);
    }

    private void placeNewWorkpiece() {
        PlaceNewWorkpieceProgram program = new PlaceNewWorkpieceProgram(
                this, 
                iiwa
        );
        
        boolean success = program.execute();
        logProgramResult("Place New Workpiece", success);
    }

    private void pickMeasuredWorkpiece() {
        PickMeasuredWorkpieceProgram program = new PickMeasuredWorkpieceProgram(
                this, 
                iiwa
        );
        
        boolean success = program.execute();
        logProgramResult("Pick Measured Workpiece", success);
    }

    private void placeMeasuredWorkpiece() {
        PlaceMeasuredWorkpieceProgram program = new PlaceMeasuredWorkpieceProgram(
                this, 
                iiwa
        );
        
        boolean success = program.execute();
        logProgramResult("Place Measured Workpiece", success);
    }

    // ========== Helper Methods ==========

    private boolean checkVisionConnection() {
        if (!smartPickingThread.isConnected()) {
            log.error("Cannot execute program - not connected to vision server");
            return false;
        }
        return true;
    }

    private void logProgramResult(String programName, boolean success) {
        if (success) {
            log.info(programName + " program completed successfully");
        } else {
            log.error(programName + " program failed");
        }
    }
}
