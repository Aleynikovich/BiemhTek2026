package biemhTekniker.managers;

import biemhTekniker.console.ConsoleServer;
import biemhTekniker.console.ConsoleServerInterface;
import biemhTekniker.data.WorkpieceQueue;
import biemhTekniker.logger.Logger;
import biemhTekniker.programs.ProgramRange;
import biemhTekniker.lib.robot.RobotContext;
import biemhTekniker.programs.VisionDispatcher;
import biemhTekniker.vision.VisionManager;

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
    private volatile int programNumber = 0;

    public AppController(VisionManager visionManager, VisionDispatcher visionDispatcher, WorkpieceQueue workpieceQueue, 
                         RobotContext robotContext, HomePositionManager homePositionManager, int consolePort)
    {
        this.visionManager = visionManager;
        this.visionDispatcher = visionDispatcher;
        this.workpieceQueue = workpieceQueue;
        this.robotContext = robotContext;
        this.homePositionManager = homePositionManager;
        this.consoleServer = new ConsoleServer(this, consolePort);
    }

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
}
