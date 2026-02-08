package biemhTekniker.managers;

import biemhTekniker.data.WorkpieceQueue;
import biemhTekniker.logger.Logger;
import biemhTekniker.programs.ProgramDispatcher;
import biemhTekniker.programs.ProgramRange;
import biemhTekniker.vision.SmartPickingThread;
import com.kuka.generated.ioAccess.AutExtIOGroup;
import com.kuka.generated.ioAccess.VisionStateIOGroup;

/**
 * Manages communication and synchronization with the PLC.
 * Handles program selection handshakes and status updates.
 */
public class PLCManager {
    private static final Logger log = Logger.getLogger(PLCManager.class);

    private final AutExtIOGroup AutExtIO;
    private final VisionStateIOGroup visionIO;
    private final ProgramDispatcher programDispatcher;
    private final SmartPickingThread smartPickingThread;
    private final WorkpieceQueue workpieceQueue;

    public PLCManager(AutExtIOGroup AutExtIO, VisionStateIOGroup visionIO, 
                      ProgramDispatcher programDispatcher, SmartPickingThread smartPickingThread,
                      WorkpieceQueue workpieceQueue) {
        this.AutExtIO = AutExtIO;
        this.visionIO = visionIO;
        this.programDispatcher = programDispatcher;
        this.smartPickingThread = smartPickingThread;
        this.workpieceQueue = workpieceQueue;
    }

    /**
     * Updates all status IOs for the PLC.
     */
    public void updateStatus() {
        updateVisionStatus();
        // Add other status updates here if needed (e.g. robot state)
    }

    /**
     * Updates vision system status IOs for the PLC.
     */
    private void updateVisionStatus() {
        if (visionIO == null) return;

        // 1. Connection status
        boolean isConnected = smartPickingThread != null && smartPickingThread.isConnected();
        visionIO.setVisionServerOnline(isConnected);

        // 2. Busy status (task running or pending)
        boolean isBusy = programDispatcher.isVisionTaskRunning();
        visionIO.setVisionServerBusy(isBusy);

        // 3. Workpiece found status
        boolean hasWorkpieces = workpieceQueue != null && workpieceQueue.getAvailableCount() > 0;
        visionIO.setNewWorkpieceFound(hasWorkpieces);

        // 4. Mode status
        visionIO.setCameraModeRun(isConnected);
        visionIO.setCameraModeCalibration(false);

        // 5. References loaded
        visionIO.setReferencesLoaded(isConnected);
    }

    /**
     * Checks if PLC is providing a program number via handshake.
     * 
     * @return The program number received from PLC, or 0 if none.
     */
    public int checkProgramRequest() {
        // 1. Set request signal to PLC
        AutExtIO.setProgramNumberRequest(true);

        // 2. Read program number from PLC
        int plcProgram = AutExtIO.getProgramNumberIN();

        if (ProgramRange.isValid(plcProgram) && plcProgram != ProgramRange.IDLE) {
            log.info("Program " + plcProgram + " received from PLC");
            // 3. Handshake complete: Reset request signal
            AutExtIO.setProgramNumberRequest(false);
            return plcProgram;
        }

        return 0;
    }

    /**
     * Echoes the current program number to the PLC.
     *
     * @param programNumber Current active program number
     */
    public void echoProgramNumber(int programNumber) {
        AutExtIO.setCurrentProgramNumber(programNumber);
    }

    /**
     * Signals a program execution error to the PLC.
     *
     * @param programNumber Program number that failed
     */
    public void signalProgramError(int programNumber) {
        log.error("Signaling program error to PLC for program: " + programNumber);
        // Set error flag to PLC (assuming there's an error output available)
        // This would need to be mapped to actual PLC I/O
        // For now, just log the error
        // TODO: Add actual PLC error signaling once I/O is configured
    }
}
