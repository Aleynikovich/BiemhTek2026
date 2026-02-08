package biemhTekniker;

import biemhTekniker.config.ConfigManager;
import biemhTekniker.console.ConsoleServer;
import biemhTekniker.console.ConsoleServerInterface;
import biemhTekniker.data.WorkpieceQueue;
import biemhTekniker.logger.Logger;
import biemhTekniker.managers.LoggingManager;
import biemhTekniker.programs.*;
import biemhTekniker.vision.SmartPickingProtocol.Command;
import biemhTekniker.vision.SmartPickingThread;
import biemhTekniker.vision.VisionManager;
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
 * Thin orchestrator that manages program execution via ProgramDispatcher.
 * Implements ConsoleServerInterface for GUI control.
 */
public class Main extends RoboticsAPIApplication implements ConsoleServerInterface
{
    private static final Logger             log = Logger.getLogger(Main.class);

    @Inject private      LBR                iiwa;
    @Inject private      RobotSafetyIOGroup safetyIO;

    // Gripper
    @Inject @Named("Gripper")
    private Tool gripper;

    @Inject private MediaFlangeIOGroup           gripperIO;
    @Inject private RobotCartesianPositionIOGroup currentCartesianPosition;

    // Managers and threads
    private LoggingManager     loggingManager;
    private SmartPickingThread smartPickingThread;
    private VisionManager      visionManager;
    private ConsoleServer      consoleServer;

    // Shared data and dispatching
    private WorkpieceQueue    workpieceQueue;
    private ProgramDispatcher programDispatcher;
    private RobotContext      robotContext;
    private VisionContext     visionContext;

    private volatile int     programNumber      = 0;
    private          int     lastProgramNumber  = 0;
    private          boolean needsHomeMove      = false;

    @Override public void initialize()
    {
        log.info("Main application initializing...");

        // Initialize configuration
        ConfigManager config = ConfigManager.getInstance();

        // Initialize logging
        loggingManager = new LoggingManager();
        loggingManager.initialize();

        // Initialize shared data structures
        workpieceQueue = new WorkpieceQueue();

        // Gripper setup
        gripper.attachTo(iiwa.getFlange());

        // Initialize and start SmartPicking thread
        String visionIP   = config.getString("vision.server.ip", "172.31.1.69");
        int    visionPort = config.getInt("vision.server.port", 59002);
        smartPickingThread = new SmartPickingThread(visionIP, visionPort);
        smartPickingThread.initialize();
        smartPickingThread.start();

        // Initialize contexts
        robotContext  = new RobotContext(iiwa, gripper, gripperIO, this, workpieceQueue);
        visionContext = new VisionContext(smartPickingThread.getProtocol(), workpieceQueue);

        // Initialize vision manager
        visionManager = new VisionManager(smartPickingThread, visionContext);
        visionManager.initialize();

        // Initialize program dispatcher and register programs
        programDispatcher = new ProgramDispatcher(robotContext, visionManager);
        registerPrograms();

        // Initialize and start console server for GUI control
        int consolePort = config.getInt("console.server.port", 30001);
        consoleServer   = new ConsoleServer(this, consolePort);
        consoleServer.initialize();

        // Set robot control parameters
        getApplicationControl().setApplicationOverride(0.5);
        getApplicationControl().clipManualOverride(0.0);

        // Move to home position and set as home
        iiwa.getFlange().move(ptp(getApplicationData().getFrame("/BiemhHome")));
        iiwa.setHomePosition(iiwa.getCurrentJointPosition());

        log.info("Main application initialized successfully");
    }

    /**
     * Registers all robot programs and vision tasks with the dispatcher.
     */
    private void registerPrograms()
    {
        log.info("Registering programs...");

        // Robot Programs (1-99)
        programDispatcher.registerRobotProgram(1, new PickNewWorkpieceProgram());
        programDispatcher.registerRobotProgram(2, new PlaceNewWorkpieceProgram());
        programDispatcher.registerRobotProgram(3, new PickMeasuredWorkpieceProgram());
        programDispatcher.registerRobotProgram(4, new PlaceMeasuredWorkpieceProgram());
        
        // Calibration programs (coordinated - need protocol access)
        CalibrationProgram calibProgram = new CalibrationProgram();
        calibProgram.setProtocol(smartPickingThread.getProtocol());
        programDispatcher.registerRobotProgram(5, calibProgram);
        
        TestCalibrationProgram testCalibProgram = new TestCalibrationProgram();
        testCalibProgram.setProtocol(smartPickingThread.getProtocol());
        programDispatcher.registerRobotProgram(6, testCalibProgram);

        // Vision Tasks (100-199)
        programDispatcher.registerVisionTask(100, new LoadReferencesTask());
        programDispatcher.registerVisionTask(101, new IndividualVisionCommandTask(Command.SET_AUTO_MODE));
        programDispatcher.registerVisionTask(102, new IndividualVisionCommandTask(Command.SET_CALIB_MODE));
        programDispatcher.registerVisionTask(103, new IndividualVisionCommandTask(Command.CAPTURE_DATA));
        programDispatcher.registerVisionTask(104, new IndividualVisionCommandTask(Command.LOCATE_CONTAINER));
        programDispatcher.registerVisionTask(105, new IndividualVisionCommandTask(Command.GET_CONTAINER_POS));
        programDispatcher.registerVisionTask(106, new IndividualVisionCommandTask(Command.LOCATE_PARTS));
        programDispatcher.registerVisionTask(107, new IndividualVisionCommandTask(Command.GET_PART_POS));
        programDispatcher.registerVisionTask(108, new IndividualVisionCommandTask(Command.GET_NEXT_PART_POS));
        programDispatcher.registerVisionTask(109, new FullScanTask());
        // Program 110 is Send Custom Message - not registered as it needs message parameter

        // Legacy vision task for backward compatibility
        programDispatcher.registerVisionTask(111, new GetNewWorkpiecePositionProgram());

        log.info("Programs registered successfully");
    }

    @Override public void dispose()
    {
        log.info("Main application shutting down");

        // Shutdown console server
        if (consoleServer != null)
        {
            consoleServer.dispose();
        }

        // Shutdown vision manager
        if (visionManager != null)
        {
            visionManager.shutdown();
        }

        // Shutdown SmartPicking thread
        if (smartPickingThread != null)
        {
            smartPickingThread.shutdown();
            try
            {
                smartPickingThread.join(15000);
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
            int currentProgram = programNumber;

            // Only move home when transitioning from active program to idle
            if (needsHomeMove && currentProgram == 0)
            {
                iiwa.move(ptpHome());
                needsHomeMove = false;
            }

            if (currentProgram != 0)
            {
                // Dispatch program
                boolean success = programDispatcher.dispatch(currentProgram);
                if (success)
                {
                    log.info("Program " + currentProgram + " completed");
                }
                else
                {
                    log.error("Program " + currentProgram + " failed");
                }

                // Reset to idle after execution
                programNumber = 0;
                lastProgramNumber = currentProgram;
                needsHomeMove = true;
            }

            ThreadUtil.milliSleep(200);
        }
    }

    // ========== ConsoleServerInterface Implementation ==========

    @Override public void setProgramNumber(int programNumber)
    {
        if (programNumber >= 0 && programNumber <= 199)
        {
            this.programNumber = programNumber;
            log.info("Program number set to: " + programNumber + " via console");
        }
        else
        {
            log.warn("Invalid program number requested: " + programNumber + " (valid range: 0-199)");
        }
    }

    @Override public int getCurrentProgram()
    {
        return programNumber;
    }

    @Override public boolean isVisionConnected()
    {
        return visionManager != null && visionManager.isConnected();
    }

    @Override public String getWorkpiecePosition()
    {
        if (workpieceQueue != null && workpieceQueue.getAvailableCount() > 0)
        {
            // Return summary of next available workpiece (without removing it from queue)
            return "Available: " + workpieceQueue.getAvailableCount() + ", Total: " + workpieceQueue.getTotalCount();
        }
        return "No workpieces available";
    }

    @Override public String getQueueStatus()
    {
        if (workpieceQueue != null)
        {
            return workpieceQueue.getQueueStatus();
        }
        return "Queue not initialized";
    }
}
