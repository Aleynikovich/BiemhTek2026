package biemhTekniker.lib.managers;

import biemhTekniker.lib.exceptions.HomePositionException;
import biemhTekniker.lib.logger.Logger;
import com.kuka.roboticsAPI.deviceModel.LBR;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptpHome;

/**
 * Manages robot home position logic.
 * Ensures the robot returns to home position after completing programs
 * to allow safe operation of the Zeiss measuring machine and vision system.
 */
public class HomePositionManager
{
    private static final Logger log = Logger.getLogger(HomePositionManager.class);

    private volatile boolean needsHomeMove = false;

    /**
     * Requests a home position move after the current program completes.
     * This is typically called after a robot program finishes execution.
     */
    public void requestHomeMove()
    {
        needsHomeMove = true;
        log.debug("Home position move requested");
    }

    /**
     * Checks if a home move should be executed based on current conditions.
     *
     * @param currentProgram  Current program number (0 = idle)
     * @param isVisionRunning Whether a vision task is currently running
     * @return true if home move should be executed now
     */
    public boolean shouldMoveHome(int currentProgram, boolean isVisionRunning)
    {
        // Only move home when:
        // 1. A home move has been requested
        // 2. No program is currently running (system is idle)
        // 3. No vision task is running (to avoid collisions)
        return needsHomeMove && currentProgram == 0 && !isVisionRunning;
    }

    /**
     * Executes the home position move.
     *
     * @param robot Robot to move
     * @throws HomePositionException if the move fails
     */
    public void executeHomeMove(LBR robot) throws HomePositionException
    {
        if (!needsHomeMove)
        {
            log.warn("executeHomeMove called but no home move was requested");
            return;
        }

        log.info("Moving to Home position...");
        try
        {
            robot.move(ptpHome());
            needsHomeMove = false;
            log.info("Home position reached successfully");
        } catch (Exception e)
        {
            log.error("Failed to move home: " + e.getMessage(), e);
            throw new HomePositionException("Failed to move to home position", e);
        }
    }

    /**
     * Resets the home move request flag.
     * Used if a home move is no longer needed or was cancelled.
     */
    public void cancelHomeMove()
    {
        needsHomeMove = false;
        log.debug("Home position move cancelled");
    }

    /**
     * Checks if a home move is currently requested.
     *
     * @return true if a home move is pending
     */
    public boolean isHomeMoveRequested()
    {
        return needsHomeMove;
    }
}
