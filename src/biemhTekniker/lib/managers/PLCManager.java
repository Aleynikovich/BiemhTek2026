package biemhTekniker.lib.managers;

import biemhTekniker.lib.data.WorkpieceQueue;
import biemhTekniker.lib.logger.Logger;
import biemhTekniker.lib.vision.SmartPickingThread;
import biemhTekniker.programs.ProgramRange;
import biemhTekniker.programs.robot.RobotDispatcher;
import biemhTekniker.programs.vision.VisionDispatcher;
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

        // Should not reach here as isValid() check covers all cases, but return 0 as safety
        log.warn("Unexpected program number state: " + plcProgram);
        return 0;
    }

    /**
     * Echoes the current program number to the PLC.
     *
     * @param programNumber Current active program number
     */
    public void echoProgramNumber(int programNumber)
    {
        try
        {
            //AutExtIO.setCurrentProgramNumber(programNumber);
        } catch (Exception e)
        {
            // Log error but continue - PLC may be in STOP mode
            log.error("Failed to echo program number to PLC: " + e.getMessage());
        }
    }

    /**
     * Signals a program execution error to the PLC.
     * Currently logs the error as PLC error signals are reserved by station state.
     * Error outputs (DefaultAppError, StationError) cannot be set programmatically.
     *
     * @param programNumber Program number that failed
     */
    public void signalProgramError(int programNumber)
    {
        log.error("Program execution error for program: " + programNumber);
        // Note: DefaultAppError and StationError outputs are reserved by Sunrise station state
        // and cannot be set programmatically (OutputReservedException would be thrown).
        // The PLC can monitor CurrentProgramNumber remaining non-zero for extended periods
        // or implement a timeout mechanism to detect failures.
        // Alternative: Use a dedicated non-reserved output for error signaling if configured.
    }
}
