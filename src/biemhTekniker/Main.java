package biemhTekniker;

import biemhTekniker.console.ConsoleServer;
import biemhTekniker.console.ConsoleServerInterface;
import biemhTekniker.data.WorkpieceData;
import biemhTekniker.logger.Logger;
import biemhTekniker.managers.LoggingManager;
import biemhTekniker.programs.*;
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

/**
 * Main robot application.
 * Manages program execution, logging, and vision system integration.
 * Implements ConsoleServerInterface for GUI control.
 */
public class Main extends RoboticsAPIApplication implements ConsoleServerInterface
{
    private static final Logger             log                = Logger.getLogger(Main.class);
    // Configuration
    private static final String             VISION_SERVER_IP   = "172.31.1.69";
    private static final int                VISION_SERVER_PORT = 59002;
    @Inject private      LBR                iiwa;
    @Inject private      RobotSafetyIOGroup safetyIO;
    // Managers and threads
    private              LoggingManager     loggingManager;
    private              SmartPickingThread smartPickingThread;
    private              ConsoleServer      consoleServer;
    private              ProgramRegistry    programRegistry;

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

        // Initialize program registry for factory-based program loading
        programRegistry = new ProgramRegistry();

        // Set robot control parameters
        getApplicationControl().setApplicationOverride(0.5);
        getApplicationControl().clipManualOverride(0.0);
        log.info("Main application initialized");
    }

    @Override public void dispose()
    {
        log.info("Main application shutting down");

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

    @Override public void run()
    {
        log.info("Main application running, moving home");
        iiwa.getFlange().move(ptp(getApplicationData().getFrame("/BiemhHome")));

        while (true)
        {
            if (programNumber != 0)
            {
                // Try to execute via ProgramRegistry first
                boolean executed = executeProgramViaRegistry(programNumber);

                if (!executed)
                {
                    // Fallback to hard-coded program execution
                    executeProgramDirect(programNumber);
                }

                programNumber = 0;
            }

            ThreadUtil.milliSleep(200);
        }
    }

    /**
     * Attempts to execute a program via ProgramRegistry.
     *
     * @param programId The program ID to execute
     * @return true if program was executed via registry, false if registry doesn't have mapping
     */
    private boolean executeProgramViaRegistry(int programId)
    {
        if (programRegistry == null || !programRegistry.hasMapping(programId))
        {
            log.debug("No registry mapping for program " + programId + ", using fallback");
            return false;
        }

        // Special pre-execution handling for program 2 (Calibration)
        if (programId == 2)
        {
            iiwa.getFlange().move(ptp(getApplicationData().getFrame("/P1")));
        }

        // Check vision connection for programs that need it
        if (programId == 1 || programId == 2 || programId == 3)
        {
            if (!checkVisionConnection())
            {
                return true; // Return true to prevent fallback execution
            }
        }

        try
        {
            log.info("Executing program " + programId + " via ProgramRegistry");
            ProgramFactory factory = programRegistry.lookup(programId);
            if (factory == null)
            {
                log.error("Failed to instantiate factory for program " + programId);
                return false;
            }

            // Create program context with all dependencies
            ProgramContext context = new ProgramContext(this, iiwa, gripper, gripperIO, workpieceData, smartPickingThread != null ? smartPickingThread.getProtocol() : null);

            // Create and execute program
            ProgramAdapter program = factory.create(context);
            boolean success = program.execute();

            // Log result
            logProgramResult("Program " + programId, success);
            return true;
        }
        catch (Exception e)
        {
            log.error("Exception executing program " + programId + " via registry: " + e.getMessage());
            return false;
        }
    }

    /**
     * Executes a program using the original hard-coded method (fallback).
     *
     * @param programId The program ID to execute
     */
    private void executeProgramDirect(int programId)
    {
        switch (programId)
        {
            case 1:
                // Program 1 - Get New Workpiece Position
                getNewWorkpiecePosition();
                break;

            case 2:
                // Program 2 - Calibration
                iiwa.getFlange().move(ptp(getApplicationData().getFrame("/P1")));
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
                log.warn("Unknown program number: " + programId);
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

        GetNewWorkpiecePositionProgram program = new GetNewWorkpiecePositionProgram(smartPickingThread.getProtocol(), workpieceData);

        boolean success = program.execute();
        logProgramResult("Get New Workpiece Position", success);
    }

    private void executeCalibration()
    {
        if (!checkVisionConnection())
        {
            return;
        }

        CalibrationProgram program = new CalibrationProgram(this, iiwa, smartPickingThread.getProtocol(), gripper);

        boolean success = program.execute();
        logProgramResult("Calibration", success);
    }

    private void testCalibration()
    {
        if (!checkVisionConnection())
        {
            return;
        }

        TestCalibrationProgram program = new TestCalibrationProgram(this, iiwa, smartPickingThread.getProtocol(), gripper);

        boolean success = program.execute();
        logProgramResult("Test Calibration", success);
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


        PickNewWorkpieceProgram program = new PickNewWorkpieceProgram(this, iiwa, workpieceData, gripper, gripperIO);

        boolean success = program.execute();
        logProgramResult("Pick New Workpiece", success);
    }

    // ========== Program Execution Methods ==========

    private void placeNewWorkpiece()
    {
        PlaceNewWorkpieceProgram program = new PlaceNewWorkpieceProgram(this, iiwa, gripper, gripperIO);

        boolean success = program.execute();
        logProgramResult("Place New Workpiece", success);
    }

    private void pickMeasuredWorkpiece()
    {
        PickMeasuredWorkpieceProgram program = new PickMeasuredWorkpieceProgram(this, iiwa);

        boolean success = program.execute();
        logProgramResult("Pick Measured Workpiece", success);
    }

    private void placeMeasuredWorkpiece()
    {
        PlaceMeasuredWorkpieceProgram program = new PlaceMeasuredWorkpieceProgram(this, iiwa);

        boolean success = program.execute();
        logProgramResult("Place Measured Workpiece", success);
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

    private void logProgramResult(String programName, boolean success)
    {
        if (success)
        {
            log.info(programName + " program completed successfully");
        }
        else
        {
            log.error(programName + " program failed");
        }
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
