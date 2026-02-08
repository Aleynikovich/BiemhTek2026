package biemhTekniker.managers;

import biemhTekniker.console.ConsoleServer;
import biemhTekniker.console.ConsoleServerInterface;
import biemhTekniker.data.WorkpieceQueue;
import biemhTekniker.logger.Logger;
import biemhTekniker.programs.ProgramRange;
import biemhTekniker.vision.VisionManager;

/**
 * Controller class that manages the application state and implements the ConsoleServerInterface.
 * This helps in keeping the Main class thin.
 */
public class AppController implements ConsoleServerInterface {
    private static final Logger log = Logger.getLogger(AppController.class);

    private final VisionManager visionManager;
    private final WorkpieceQueue workpieceQueue;
    private final ConsoleServer consoleServer;
    private volatile int programNumber = 0;

    public AppController(VisionManager visionManager, WorkpieceQueue workpieceQueue, int consolePort) {
        this.visionManager = visionManager;
        this.workpieceQueue = workpieceQueue;
        this.consoleServer = new ConsoleServer(this, consolePort);
    }

    public void initialize() {
        consoleServer.initialize();
    }

    public void shutdown() {
        if (consoleServer != null) {
            consoleServer.dispose();
        }
    }

    @Override
    public void setProgramNumber(int programNumber) {
        if (ProgramRange.isValid(programNumber)) {
            this.programNumber = programNumber;
            log.info("Program number set to: " + programNumber + " via console");
        } else {
            log.warn("Invalid program number requested: " + programNumber + " (valid range: " + ProgramRange.IDLE + "-" + ProgramRange.VISION_MAX + ")");
        }
    }

    @Override
    public int getCurrentProgram() {
        return programNumber;
    }

    public void resetProgramNumber() {
        this.programNumber = 0;
    }

    public void setProgramNumberFromPLC(int programNumber) {
        this.programNumber = programNumber;
    }

    @Override
    public boolean isVisionConnected() {
        return visionManager != null && visionManager.isConnected();
    }

    @Override
    public String getWorkpiecePosition() {
        if (workpieceQueue != null && workpieceQueue.getAvailableCount() > 0) {
            return "Available: " + workpieceQueue.getAvailableCount() + ", Total: " + workpieceQueue.getTotalCount();
        }
        return "No workpieces available";
    }

    @Override
    public String getQueueStatus() {
        if (workpieceQueue != null) {
            return workpieceQueue.getQueueStatus();
        }
        return "Queue not initialized";
    }

    @Override
    public boolean hasActiveClients() {
        return consoleServer != null && consoleServer.hasActiveClients();
    }
}
