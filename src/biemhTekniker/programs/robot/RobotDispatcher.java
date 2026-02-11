package biemhTekniker.programs.robot;

import biemhTekniker.lib.logger.Logger;
import biemhTekniker.lib.robot.RobotProgram;
import biemhTekniker.lib.vision.SmartPickingThread;
import biemhTekniker.programs.ProgramRange;
import biemhTekniker.programs.robot.subprograms.CalibrationProgram;
import biemhTekniker.programs.robot.subprograms.PickNewWorkpieceProgram;
import biemhTekniker.programs.robot.subprograms.PlaceNewWorkpieceProgram;
import biemhTekniker.programs.robot.subprograms.TestCalibrationProgram;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dispatches robot program execution based on program number.
 * Handles robot programs (1-99) on the main thread.
 * Provides busy status for PLC handshaking.
 */
public class RobotDispatcher
{
    private static final Logger log = Logger.getLogger(RobotDispatcher.class);
    private final Map<Integer, RobotProgram> robotPrograms;
    private final RobotContext robotContext;
    private final AtomicBoolean busy;

    /**
     * Creates a robot dispatcher.
     *
     * @param robotContext Context for robot programs
     */
    public RobotDispatcher(RobotContext robotContext)
    {
        this.robotContext = robotContext;
        this.robotPrograms = new HashMap<Integer, RobotProgram>();
        this.busy = new AtomicBoolean(false);
    }

    /**
     * Registers a robot program.
     *
     * @param programNumber Program number (1-99)
     * @param program       Robot program instance
     */
    public void registerRobotProgram(int programNumber, RobotProgram program)
    {
        if (!ProgramRange.isRobotProgram(programNumber))
        {
            throw new IllegalArgumentException("Robot program number must be " + ProgramRange.ROBOT_MIN + "-" + ProgramRange.ROBOT_MAX + ", got: " + programNumber);
        }
        robotPrograms.put(Integer.valueOf(programNumber), program);
        log.debug("Registered robot program " + programNumber + ": " + program.getClass().getSimpleName());
    }

    /**
     * Registers robot programs with protocol access for coordination.
     */
    public void registerRobotPrograms(SmartPickingThread smartPickingThread)
    {
        log.info("Registering robot programs...");

        // Basic robot programs (1-4)
        registerRobotProgram(1, new PickNewWorkpieceProgram());
        registerRobotProgram(2, new PlaceNewWorkpieceProgram());

        // Calibration programs (5-6) - need protocol access
        CalibrationProgram calibProgram = new CalibrationProgram();
        calibProgram.setProtocol(smartPickingThread.getProtocol());
        registerRobotProgram(5, calibProgram);

        TestCalibrationProgram testCalibProgram = new TestCalibrationProgram();
        testCalibProgram.setProtocol(smartPickingThread.getProtocol());
        registerRobotProgram(6, testCalibProgram);

        log.info("Robot programs registered successfully");
    }

    /**
     * Dispatches a robot program for execution.
     * Blocks until program completes.
     *
     * @param programNumber Robot program number (1-99)
     * @return true if execution succeeded, false if program not found or failed
     */
    public boolean dispatch(int programNumber)
    {
        if (!ProgramRange.isRobotProgram(programNumber))
        {
            log.warn("Invalid robot program number: " + programNumber + " (valid range: " + ProgramRange.ROBOT_MIN + "-" + ProgramRange.ROBOT_MAX + ")");
            return false;
        }

        RobotProgram program = robotPrograms.get(Integer.valueOf(programNumber));
        if (program == null)
        {
            log.warn("Robot program " + programNumber + " not registered");
            return false;
        }

        // Set busy flag
        busy.set(true);
        try
        {
            log.info("Executing robot program " + programNumber + ": " + program.getClass().getSimpleName());
            program.execute(robotContext);
            log.info("Robot program " + programNumber + " completed successfully");
            return true;
        } catch (Exception e)
        {
            log.error("Robot program " + programNumber + " failed: " + e.getMessage(), e);
            return false;
        } finally
        {
            // Clear busy flag
            busy.set(false);
        }
    }

    /**
     * Checks if a robot program is currently executing.
     *
     * @return true if robot is busy executing a program
     */
    public boolean isBusy()
    {
        return busy.get();
    }
}
