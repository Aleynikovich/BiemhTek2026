package biemhTekniker;

import biemhTekniker.console.ConsoleServerInterface;
import biemhTekniker.lib.config.ConfigManager;
import biemhTekniker.lib.config.FrameRepository;
import biemhTekniker.lib.data.WorkpieceQueue;
import biemhTekniker.lib.exceptions.HomePositionException;
import biemhTekniker.lib.logger.Logger;
import biemhTekniker.lib.managers.AppController;
import biemhTekniker.lib.managers.HomePositionManager;
import biemhTekniker.lib.managers.LoggingManager;
import biemhTekniker.lib.managers.PLCManager;
import biemhTekniker.lib.vision.SmartPickingThread;
import biemhTekniker.lib.vision.VisionManager;
import biemhTekniker.programs.ProgramRange;
import biemhTekniker.programs.robot.RobotContext;
import biemhTekniker.programs.robot.RobotDispatcher;
import biemhTekniker.programs.vision.VisionContext;
import biemhTekniker.programs.vision.VisionDispatcher;
import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.*;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.conditionModel.ObserverManager;
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


    @Override
    public void run() throws Exception
    {
        log.info("Main application running, entering main loop.");
        moveToHomePosition();
        gripperIO.setSecuritySwitch(true);
        while (true) //AutExtIO.getMoveEnable()
        {
            processMainLoop();
            ThreadUtil.milliSleep(MAIN_LOOP_DELAY_MS);
        }

        //log.warn("MoveEnable signal lost. Exiting main loop.");
    }

    /**
     * Single iteration of the main application loop.
     */
    private void processMainLoop()
    {
        // 1. Update PLC and handle program selection
        plcManager.updateStatus();
        int programNumber = appController.getCurrentProgram();
        plcManager.echoProgramNumber(programNumber);

        
        if (programNumber == 0 && !appController.hasActiveClients())
        	 {
        	 programNumber = plcManager.checkProgramRequest();
            if (programNumber != 0)
            	 {
            	 appController.setProgramNumberFromPLC(programNumber);
                }
            }
      

        // 2. Handle Home Position moves
        boolean isVisionRunning = visionDispatcher.isBusy();
        if (homePositionManager.shouldMoveHome(programNumber, isVisionRunning))
        	 {
        	 try
            {
            	 homePositionManager.executeHomeMove(iiwa);
                } catch (HomePositionException e)
            {
            	 log.error("Home position move failed: " + e.getMessage(), e);
                }
            }
     

    
        // 3. Execute programs
        if ( programNumber != 0)
        {
            executeProgram(programNumber);
        }
    }

    /**
     * Dispatches and executes the specified program.
     */
    private void executeProgram(int programNumber)
    {
        log.info("Starting execution of Program " + programNumber);

        // Clear cancellation flag before starting new program
        robotContext.clearCancellation();

        boolean isVisionProgram = ProgramRange.isVisionProgram(programNumber);
        boolean success = false;

        if (isVisionProgram)
        {
            appController.clearWorkpieceQueue();
            success = visionDispatcher.dispatch(programNumber);
        } else if (ProgramRange.isRobotProgram(programNumber))
        {
            success = robotDispatcher.dispatch(programNumber);
        }

        if (success)
        {
            log.info("Program " + programNumber + (isVisionProgram ? " submitted successfully" : " completed successfully"));
        } else
        {
            log.error("Program " + programNumber + (isVisionProgram ? " submission failed" : " failed during execution"));
            plcManager.signalProgramError(programNumber);
        }

        // Reset to idle after execution
        appController.resetProgramNumber();

        // Request home move if it was a robot program
        if (ProgramRange.isRobotProgram(programNumber))
        {
            homePositionManager.requestHomeMove();
        }

        // Echo back the reset to PLC
        plcManager.echoProgramNumber(0);
    }

    @Override
    public void initialize()
    {
        log.info("Main application initializing...");

        try
        {
            initializeConfiguration();
            initializeHardware();
            initializeSharedData();
            initializeVisionSystem();
            initializeDispatchers();
            initializeManagers();
            initializeAppControl();

            log.info("Main application initialized successfully");
        } catch (Exception e)
        {
            log.error("Failed to initialize application: " + e.getMessage(), e);
            throw new RuntimeException("Initialization failed", e);
        }
    }

    private void initializeConfiguration()
    {
        ConfigManager.getInstance();
        loggingManager = new LoggingManager();
        loggingManager.initialize();
    }

    private void initializeHardware()
    {
        gripper.attachTo(iiwa.getFlange());
    }

    private void initializeSharedData()
    {
        workpieceQueue = new WorkpieceQueue();
    }

    private void initializeVisionSystem()
    {
        ConfigManager config = ConfigManager.getInstance();
        String visionIP = config.getString("vision.server.ip", "172.31.1.69");
        int visionPort = config.getInt("vision.server.port", 59002);

        smartPickingThread = new SmartPickingThread(visionIP, visionPort);
        smartPickingThread.initialize();
        smartPickingThread.start();

        FrameRepository frameRepository = new FrameRepository(this);
        robotContext = new RobotContext(iiwa, gripper, gripperIO, this, workpieceQueue, frameRepository);
        robotContext.setProtocol(smartPickingThread.getProtocol());

        visionContext = new VisionContext(smartPickingThread.getProtocol(), workpieceQueue);

        VisionManager visionManager = new VisionManager(smartPickingThread, visionContext);
        visionManager.initialize();

        visionDispatcher = new VisionDispatcher(visionManager);
        visionDispatcher.registerVisionTasks();
    }

    private void initializeDispatchers()
    {
        robotDispatcher = new RobotDispatcher(robotContext);
        robotDispatcher.registerRobotPrograms(smartPickingThread);
    }

    private void initializeManagers()
    {
        plcManager = new PLCManager(AutExtIO, visionIO, robotDispatcher, visionDispatcher, smartPickingThread, workpieceQueue);
        homePositionManager = new HomePositionManager();
    }

    /*
    private void initializeAppControl()
    {
        ConfigManager config = ConfigManager.getInstance();
        int consolePort = config.getInt("console.server.port", 30001);

        appController = new AppController(visionDispatcher.getVisionManager(), visionDispatcher, workpieceQueue, robotContext, homePositionManager, robotDispatcher, consolePort);
        appController.initialize();

        getApplicationControl().setApplicationOverride(0.5);
        getApplicationControl().clipManualOverride(0.0);
    }*/
    //JAVI
    private void initializeAppControl()
    {
        ConfigManager config = ConfigManager.getInstance();
        int consolePort = config.getInt("console.server.port", 30001);

        AutExtIOGroup autExtIO = this.AutExtIO;

        ObserverManager observerManager = this.getObserverManager();

        appController = new AppController(visionDispatcher.getVisionManager(), visionDispatcher, workpieceQueue, robotContext, homePositionManager, robotDispatcher, AutExtIO, observerManager, consolePort);
        appController.initialize();

        getApplicationControl().setApplicationOverride(0.5);
        getApplicationControl().clipManualOverride(0.0);
    }
    //ENDJAVI

    private void moveToHomePosition()
    {
        log.info("Moving to initial home position...");
        iiwa.getFlange().move(ptp(getApplicationData().getFrame("/BiemhHome")));
        iiwa.setHomePosition(iiwa.getCurrentJointPosition());
    }


    @Override
    public void dispose()
    {
        log.info("Main application shutting down");

        shutdownAppController();
        shutdownVisionSystem();
        shutdownSmartPickingThread();
        shutdownLogging();

        super.dispose();
    }

    private void shutdownAppController()
    {
        if (appController != null)
        {
            appController.shutdown();
        }
    }

    private void shutdownVisionSystem()
    {
        if (visionDispatcher != null && visionDispatcher.getVisionManager() != null)
        {
            visionDispatcher.getVisionManager().shutdown();
        }
    }

    private void shutdownSmartPickingThread()
    {
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
    }

    private void shutdownLogging()
    {
        if (loggingManager != null)
        {
            loggingManager.shutdown();
        }
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

    @Override
    public void clearWorkpieceQueue()
    {
        appController.clearWorkpieceQueue();
    }

    @Override
    public boolean removeWorkpiece(long workpieceId)
    {
        return appController.removeWorkpiece(workpieceId);
    }

    @Override
    public boolean isGripper1Closed()
    {
        return appController.isGripper1Closed();
    }

    @Override
    public boolean isGripper2Closed()
    {
        return appController.isGripper2Closed();
    }

    @Override
    public boolean isGripper3Closed()
    {
        return appController.isGripper3Closed();
    }

    @Override
    public void startAutoCycle()
    {
        appController.startAutoCycle();
    }

    @Override
    public void stopAutoCycle()
    {
        appController.stopAutoCycle();
    }

    @Override
    public boolean isAutoCycleRunning()
    {
        return appController.isAutoCycleRunning();
    }

}
