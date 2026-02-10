package biemhTekniker;

import biemhTekniker.config.ConfigManager;
import biemhTekniker.config.FrameRepository;
import biemhTekniker.console.ConsoleServerInterface;
import biemhTekniker.data.WorkpieceQueue;
import biemhTekniker.exceptions.HomePositionException;
import biemhTekniker.logger.Logger;
import biemhTekniker.managers.AppController;
import biemhTekniker.managers.HomePositionManager;
import biemhTekniker.managers.LoggingManager;
import biemhTekniker.managers.PLCManager;
import biemhTekniker.programs.RobotDispatcher;
import biemhTekniker.programs.VisionDispatcher;
import biemhTekniker.programs.ProgramRange;
import biemhTekniker.programs.RobotContext;
import biemhTekniker.programs.VisionContext;
import biemhTekniker.vision.SmartPickingThread;
import biemhTekniker.vision.VisionManager;
import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.*;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;

import javax.inject.Inject;
import javax.inject.Named;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

/**
 * Main robot application.
 * Thin orchestrator that manages program execution via RobotDispatcher and VisionDispatcher.
 * Implements ConsoleServerInterface for GUI control.
 */
public class Main extends RoboticsAPIApplication implements ConsoleServerInterface
{
    private static final Logger log = Logger.getLogger(Main.class);
    private static final int MAIN_LOOP_DELAY_MS = 200;
    private static final int SMARTPICKING_SHUTDOWN_TIMEOUT_MS = 20000;

    @Inject
    private LBR iiwa;
    @Inject
    private RobotSafetyIOGroup safetyIO;

    // Gripper
    @Inject
    @Named("Gripper")
    private Tool gripper;

    @Inject
    private MediaFlangeIOGroup gripperIO;
    @Inject
    private RobotCartesianPositionIOGroup currentCartesianPosition;
    @Inject
    private AutExtIOGroup AutExtIO;
    @Inject
    private VisionStateIOGroup visionIO;

    // Managers and threads
    private LoggingManager loggingManager;
    private PLCManager plcManager;
    private AppController appController;
    private HomePositionManager homePositionManager;
    private SmartPickingThread smartPickingThread;

    // Shared data and dispatching
    private WorkpieceQueue workpieceQueue;
    private RobotDispatcher robotDispatcher;
    private VisionDispatcher visionDispatcher;
    private RobotContext robotContext;
    private VisionContext visionContext;

    private int lastProgramNumber = 0;

    @Override
    public void initialize()
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
        String visionIP = config.getString("vision.server.ip", "172.31.1.69");
        int visionPort = config.getInt("vision.server.port", 59002);
        smartPickingThread = new SmartPickingThread(visionIP, visionPort);
        smartPickingThread.initialize();
        smartPickingThread.start();

        // Initialize contexts
        FrameRepository frameRepository = new FrameRepository(this);
        robotContext = new RobotContext(iiwa, gripper, gripperIO, this, workpieceQueue, frameRepository);
        robotContext.setProtocol(smartPickingThread.getProtocol());
        visionContext = new VisionContext(smartPickingThread.getProtocol(), workpieceQueue);

        // Initialize vision manager
        VisionManager visionManager = new VisionManager(smartPickingThread, visionContext);
        visionManager.initialize();

        // Initialize dispatchers
        robotDispatcher = new RobotDispatcher(robotContext);
        robotDispatcher.registerRobotPrograms(smartPickingThread);

        visionDispatcher = new VisionDispatcher(visionManager);
        visionDispatcher.registerVisionTasks();

        // Initialize PLC manager
        plcManager = new PLCManager(AutExtIO, visionIO, robotDispatcher, visionDispatcher, smartPickingThread, workpieceQueue);

        // Initialize home position manager
        homePositionManager = new HomePositionManager();

        // Initialize app controller
        int consolePort = config.getInt("console.server.port", 30001);
        appController = new AppController(visionManager, workpieceQueue, robotContext, homePositionManager, consolePort);
        appController.initialize();

        // Set robot control parameters
        getApplicationControl().setApplicationOverride(0.5);
        getApplicationControl().clipManualOverride(0.0);

        // Move to home position and set as home
        iiwa.getFlange().move(ptp(getApplicationData().getFrame("/BiemhHome")));
        iiwa.setHomePosition(iiwa.getCurrentJointPosition());

        log.info("Main application initialized successfully");
    }

    @Override
    public void run() throws Exception
    {
        log.info("Main application running, entering main loop.");

        while (AutExtIO.getMoveEnable())
        {
            // Update PLC status
            plcManager.updateStatus();

            int programNumber = appController.getCurrentProgram();

            // Update current program echo to PLC
            plcManager.echoProgramNumber(programNumber);

            // Handle program selection via PLC if no console is connected
            if (programNumber == 0 && !appController.hasActiveClients())
            {
                programNumber = plcManager.checkProgramRequest();
                if (programNumber != 0)
                {
                    appController.setProgramNumberFromPLC(programNumber);
                }
            }

            int currentProgram = programNumber;
            boolean isVisionRunning = visionDispatcher.isBusy();

            // Check if home position move should be executed
            if (homePositionManager.shouldMoveHome(currentProgram, isVisionRunning))
            {
                try
                {
                    homePositionManager.executeHomeMove(iiwa);
                } catch (HomePositionException e)
                {
                    log.error("Home position move failed: " + e.getMessage(), e);
                }
            }

            if (currentProgram != 0)
            {
                // Clear cancellation flag before starting new program
                robotContext.clearCancellation();
                
                // Dispatch program to appropriate dispatcher
                log.info("Starting execution of Program " + currentProgram);
                boolean isVisionProgram = ProgramRange.isVisionProgram(currentProgram);
                boolean success = false;

                if (isVisionProgram)
                {
                    success = visionDispatcher.dispatch(currentProgram);
                }
                else if (ProgramRange.isRobotProgram(currentProgram))
                {
                    success = robotDispatcher.dispatch(currentProgram);
                }

                if (success)
                {
                    log.info("Program " + currentProgram + (isVisionProgram ? " submitted successfully" : " completed successfully"));
                } else
                {
                    log.error("Program " + currentProgram + (isVisionProgram ? " submission failed" : " failed during execution"));
                    plcManager.signalProgramError(currentProgram);
                }

                // Reset to idle after execution (or submission for vision)
                appController.resetProgramNumber();
                lastProgramNumber = currentProgram;

                // Only request home move if it was a robot program
                if (ProgramRange.isRobotProgram(currentProgram))
                {
                    homePositionManager.requestHomeMove();
                }

                // Echo back the reset to PLC immediately
                plcManager.echoProgramNumber(0);
            }

            ThreadUtil.milliSleep(MAIN_LOOP_DELAY_MS);
        }

        log.warn("MoveEnable signal lost. Exiting main loop.");
    }

    @Override
    public void dispose()
    {
        log.info("Main application shutting down");

        // Shutdown app controller (includes console server)
        if (appController != null)
        {
            appController.shutdown();
        }

        // Shutdown vision manager and threads
        if (visionDispatcher != null && visionDispatcher.getVisionManager() != null)
        {
            visionDispatcher.getVisionManager().shutdown();
        }

        // Shutdown SmartPicking thread
        if (smartPickingThread != null)
        {
            smartPickingThread.shutdown();
            try
            {
                smartPickingThread.join(SMARTPICKING_SHUTDOWN_TIMEOUT_MS);
                if (smartPickingThread.isAlive())
                {
                    log.warn("SmartPicking thread did not stop gracefully, interrupting");
                    smartPickingThread.interrupt();
                }
            } catch (InterruptedException e)
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

    // ========== ConsoleServerInterface Implementation ==========

    @Override
    public void setProgramNumber(int programNumber)
    {
        appController.setProgramNumber(programNumber);
    }

    @Override
    public int getCurrentProgram()
    {
        return appController.getCurrentProgram();
    }

    @Override
    public boolean isVisionConnected()
    {
        return appController.isVisionConnected();
    }

    @Override
    public String getWorkpiecePosition()
    {
        return appController.getWorkpiecePosition();
    }

    @Override
    public String getQueueStatus()
    {
        return appController.getQueueStatus();
    }

    @Override
    public boolean hasActiveClients()
    {
        return appController.hasActiveClients();
    }

    @Override
    public void cancelCurrentProgram()
    {
        appController.cancelCurrentProgram();
    }
    
    @Override
    public String getWorkpiecesJson()
    {
        return appController.getWorkpiecesJson();
    }

}
