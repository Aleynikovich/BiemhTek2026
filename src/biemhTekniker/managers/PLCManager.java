package biemhTekniker.managers;

import biemhTekniker.data.WorkpieceQueue;
import biemhTekniker.logger.Logger;
import biemhTekniker.programs.RobotDispatcher;
import biemhTekniker.programs.VisionDispatcher;
import biemhTekniker.programs.ProgramRange;
import biemhTekniker.vision.SmartPickingThread;
import com.kuka.generated.ioAccess.AutExtIOGroup;
import com.kuka.generated.ioAccess.VisionStateIOGroup;

/**
 * Manages communication and synchronization with the PLC.
 * Handles program selection handshakes and status updates.
 * Supports independent robot and vision program requests.
 */
public class PLCManager
{
    private static final Logger log = Logger.getLogger(PLCManager.class);

    private final AutExtIOGroup AutExtIO;
    private final VisionStateIOGroup visionIO;
    private final RobotDispatcher robotDispatcher;
    private final VisionDispatcher visionDispatcher;
    private final SmartPickingThread smartPickingThread;
    private final WorkpieceQueue workpieceQueue;

    public PLCManager(AutExtIOGroup AutExtIO, VisionStateIOGroup visionIO, RobotDispatcher robotDispatcher, VisionDispatcher visionDispatcher, SmartPickingThread smartPickingThread, WorkpieceQueue workpieceQueue)
    {
        this.AutExtIO = AutExtIO;
        this.visionIO = visionIO;
        this.robotDispatcher = robotDispatcher;
        this.visionDispatcher = visionDispatcher;
        this.smartPickingThread = smartPickingThread;
        this.workpieceQueue = workpieceQueue;
    }

    /**
     * Updates all status IOs for the PLC.
     */
    public void updateStatus()
    {
        updateVisionStatus();
        updateRobotStatus();
    }

    /**
     * Updates robot system status IOs for the PLC.
     */
    private void updateRobotStatus()
    {
        // Set robot busy flag (inverse of program request ready)
        boolean robotBusy = robotDispatcher.isBusy();
        AutExtIO.setProgramNumberRequest(!robotBusy);
    }

    /**
     * Updates vision system status IOs for the PLC.
     */
    private void updateVisionStatus()
    {
        if (visionIO == null)
            return;

        // 1. Connection status
        boolean isConnected = smartPickingThread != null && smartPickingThread.isConnected();
        visionIO.setVisionServerOnline(isConnected);

        // 2. Busy status (task running or pending)
        boolean isBusy = visionDispatcher.isBusy();
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
     * Only accepts robot programs when robot is not busy.
     * Only accepts vision programs when vision is not busy.
     *
     * @return The program number received from PLC, or 0 if none or system busy.
     */
    public int checkProgramRequest()
    {
        // Read program number from PLC
        int plcProgram = AutExtIO.getProgramNumberIN();

        if (!ProgramRange.isValid(plcProgram) || plcProgram == ProgramRange.IDLE)
        {
            return 0;
        }

        // Check if it's a robot program and robot is available
        if (ProgramRange.isRobotProgram(plcProgram))
        {
            if (!robotDispatcher.isBusy())
            {
                log.info("Robot program " + plcProgram + " received from PLC");
                return plcProgram;
            }
            else
            {
                log.debug("Robot program " + plcProgram + " requested but robot is busy");
                return 0;
            }
        }

        // Check if it's a vision program and vision is available
        if (ProgramRange.isVisionProgram(plcProgram))
        {
            if (!visionDispatcher.isBusy())
            {
                log.info("Vision program " + plcProgram + " received from PLC");
                return plcProgram;
            }
            else
            {
                log.debug("Vision program " + plcProgram + " requested but vision is busy");
                return 0;
            }
        }

        return 0;
    }

    /**
     * Echoes the current program number to the PLC.
     *
     * @param programNumber Current active program number
     */
    public void echoProgramNumber(int programNumber)
    {
        AutExtIO.setCurrentProgramNumber(programNumber);
    }

    /**
     * Signals a program execution error to the PLC.
     *
     * @param programNumber Program number that failed
     */
    public void signalProgramError(int programNumber)
    {
        log.error("Signaling program error to PLC for program: " + programNumber);
        // Set error flag to PLC (assuming there's an error output available)
        // This would need to be mapped to actual PLC I/O
        // For now, just log the error
        // TODO: Add actual PLC error signaling once I/O is configured
    }
}
