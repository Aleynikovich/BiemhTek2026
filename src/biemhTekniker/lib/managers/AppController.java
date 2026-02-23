package biemhTekniker.lib.managers;

import biemhTekniker.console.ConsoleServer;
import biemhTekniker.console.ConsoleServerInterface;
import biemhTekniker.lib.data.WorkpieceQueue;
import biemhTekniker.lib.logger.Logger;
import biemhTekniker.lib.vision.VisionManager;
import biemhTekniker.programs.ProgramRange;
import biemhTekniker.programs.robot.RobotContext;
import biemhTekniker.programs.robot.RobotDispatcher;
import biemhTekniker.programs.vision.VisionDispatcher;
import com.kuka.roboticsAPI.conditionModel.ObserverManager;
import com.kuka.generated.ioAccess.AutExtIOGroup;

/**
 * Controller class that manages the application state and implements the ConsoleServerInterface.
 * This helps in keeping the Main class thin.
 * Supports immediate dispatch of vision programs via console even when robot is busy.
 */
public class AppController implements ConsoleServerInterface
{
    private static final Logger log = Logger.getLogger(AppController.class);

    private final VisionManager visionManager;
    private final VisionDispatcher visionDispatcher;
    private final WorkpieceQueue workpieceQueue;
    private final ConsoleServer consoleServer;
    private final RobotContext robotContext;
    private final HomePositionManager homePositionManager;
    private final RobotDispatcher robotDispatcher;
    private final AutoCycleManager autoCycleManager;
    private volatile int programNumber = 0;
/*
    public AppController(VisionManager visionManager, VisionDispatcher visionDispatcher, WorkpieceQueue workpieceQueue,
                         RobotContext robotContext, HomePositionManager homePositionManager, RobotDispatcher robotDispatcher, int consolePort)
    {
        this.visionManager = visionManager;
        this.visionDispatcher = visionDispatcher;
        this.workpieceQueue = workpieceQueue;
        this.robotContext = robotContext;
        this.homePositionManager = homePositionManager;
        this.robotDispatcher = robotDispatcher;
        this.consoleServer = new ConsoleServer(this, consolePort);
        this.autoCycleManager = new AutoCycleManager(robotDispatcher, visionDispatcher, homePositionManager);
    }*/

    //JAVI
    public AppController(VisionManager visionManager,
                         VisionDispatcher visionDispatcher,
                         WorkpieceQueue workpieceQueue,
                         RobotContext robotContext,
                         HomePositionManager homePositionManager,
                         RobotDispatcher robotDispatcher,
                         AutExtIOGroup autExtIO,
                         ObserverManager observerManager,
                         int consolePort)
    {
        this.visionManager = visionManager;
        this.visionDispatcher = visionDispatcher;
        this.workpieceQueue = workpieceQueue;
        this.robotContext = robotContext;
        this.homePositionManager = homePositionManager;
        this.robotDispatcher = robotDispatcher;
        this.consoleServer = new ConsoleServer(this, consolePort);
        this.autoCycleManager = new AutoCycleManager(
                robotDispatcher,
                visionDispatcher,
                homePositionManager,
                autExtIO,
                observerManager
        );
    }
    //NEW JAVI

    public void initialize()
    {
        consoleServer.initialize();
    }

    public void shutdown()
    {
        if (consoleServer != null)
        {
            consoleServer.dispose();
        }
    }

    @Override
    public void setProgramNumber(int programNumber)
    {
        if (ProgramRange.isValid(programNumber))
        {
            // If it's a vision program, dispatch it immediately (async)
            // This allows vision to run even when robot is busy
            if (ProgramRange.isVisionProgram(programNumber))
            {
                log.info("Vision program " + programNumber + " requested via console - dispatching immediately");
                visionDispatcher.dispatch(programNumber);
                // Don't set programNumber variable as vision programs are async
            }
            else
            {
                // Robot programs go through the main loop
                this.programNumber = programNumber;
                log.info("Program number set to: " + programNumber + " via console");
            }
        } else
        {
            log.warn("Invalid program number requested: " + programNumber + " (valid range: " + ProgramRange.IDLE + "-" + ProgramRange.VISION_MAX + ")");
        }
    }

    @Override
    public int getCurrentProgram()
    {
        return programNumber;
    }

    public void resetProgramNumber()
    {
        this.programNumber = 0;
    }

    public void setProgramNumberFromPLC(int programNumber)
    {
        this.programNumber = programNumber;
    }

    @Override
    public boolean isVisionConnected()
    {
        return visionManager != null && visionManager.isConnected();
    }

    @Override
    public String getWorkpiecePosition()
    {
        if (workpieceQueue != null && workpieceQueue.getAvailableCount() > 0)
        {
            return "Available: " + workpieceQueue.getAvailableCount() + ", Total: " + workpieceQueue.getTotalCount();
        }
        return "No workpieces available";
    }

    @Override
    public String getQueueStatus()
    {
        if (workpieceQueue != null)
        {
            return workpieceQueue.getQueueStatus();
        }
        return "Queue not initialized";
    }

    @Override
    public boolean hasActiveClients()
    {
        return consoleServer != null && consoleServer.hasActiveClients();
    }

    @Override
    public void cancelCurrentProgram()
    {
        log.info("Cancellation requested - setting cancellation flag and requesting home move");
        
        // Set cancellation flag in robot context
        if (robotContext != null)
        {
            robotContext.requestCancellation();
        }
        
        // Reset program to idle
        this.programNumber = 0;
        
        // Request return to home position
        if (homePositionManager != null)
        {
            homePositionManager.requestHomeMove();
        }
    }
    
    @Override
    public String getWorkpiecesJson()
    {
        if (workpieceQueue == null)
        {
            return "[]";
        }
        
        return workpieceQueue.getWorkpiecesJson();
    }
    
    @Override
    public void clearWorkpieceQueue()
    {
        if (workpieceQueue != null)
        {
            workpieceQueue.clear();
            log.info("Workpiece queue cleared via console command");
        }
    }
    
    @Override
    public boolean removeWorkpiece(long workpieceId)
    {
        if (workpieceQueue != null)
        {
            return workpieceQueue.removeWorkpiece(workpieceId);
        }
        return false;
    }

    @Override
    public boolean isGripper1Closed()
    {
        try
        {
            return robotContext != null && robotContext.getGripperIO() != null && robotContext.getGripperIO().getGripper1_Switch();
        } catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public boolean isGripper2Closed()
    {
        try
        {
            return robotContext != null && robotContext.getGripperIO() != null && robotContext.getGripperIO().getGripper2_Switch();
        } catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public boolean isGripper3Closed()
    {
        // If only two physical grippers exist, report false for third
        return false;
    }
    
    @Override
    public void startAutoCycle()
    {
        if (autoCycleManager != null)
        {
            autoCycleManager.startCycle();
            log.info("Auto cycle started via console command");
        }
    }
    
    @Override
    public void stopAutoCycle()
    {
        if (autoCycleManager != null)
        {
            autoCycleManager.stopCycle();
            log.info("Auto cycle stopped via console command");
        }
    }
    
    @Override
    public boolean isAutoCycleRunning()
    {
        return autoCycleManager != null && autoCycleManager.isRunning();
    }
}
