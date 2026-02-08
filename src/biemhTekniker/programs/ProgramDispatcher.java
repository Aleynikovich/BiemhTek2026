package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import biemhTekniker.vision.VisionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Dispatches program execution based on program number.
 * Routes robot programs (1-99) to main thread execution.
 * Routes vision programs (100-199) to vision thread execution via VisionManager.
 */
public class ProgramDispatcher
{
    private static final Logger                         log = Logger.getLogger(ProgramDispatcher.class);
    private final        Map<Integer, RobotProgram>     robotPrograms;
    private final        Map<Integer, VisionTask>       visionTasks;
    private final        RobotContext                   robotContext;
    private final        VisionManager                  visionManager;

    /**
     * Creates a program dispatcher.
     *
     * @param robotContext  Context for robot programs
     * @param visionManager Vision manager for vision tasks
     */
    public ProgramDispatcher(RobotContext robotContext, VisionManager visionManager)
    {
        this.robotContext  = robotContext;
        this.visionManager = visionManager;
        this.robotPrograms = new HashMap<Integer, RobotProgram>();
        this.visionTasks   = new HashMap<Integer, VisionTask>();
    }

    /**
     * Registers a robot program.
     *
     * @param programNumber Program number (1-99)
     * @param program       Robot program instance
     */
    public void registerRobotProgram(int programNumber, RobotProgram program)
    {
        if (programNumber < 1 || programNumber > 99)
        {
            throw new IllegalArgumentException("Robot program number must be 1-99, got: " + programNumber);
        }
        robotPrograms.put(Integer.valueOf(programNumber), program);
        log.debug("Registered robot program " + programNumber + ": " + program.getClass().getSimpleName());
    }

    /**
     * Registers a vision task.
     *
     * @param programNumber Program number (100-199)
     * @param task          Vision task instance
     */
    public void registerVisionTask(int programNumber, VisionTask task)
    {
        if (programNumber < 100 || programNumber > 199)
        {
            throw new IllegalArgumentException("Vision task number must be 100-199, got: " + programNumber);
        }
        visionTasks.put(Integer.valueOf(programNumber), task);
        log.debug("Registered vision task " + programNumber + ": " + task.getClass().getSimpleName());
    }

    /**
     * Dispatches a program based on its number.
     * Robot programs (1-99) are executed synchronously on the calling thread.
     * Vision programs (100-199) are submitted to VisionManager and return immediately.
     *
     * @param programNumber Program number to dispatch (0-199)
     * @return true if dispatch succeeded, false if program not found or failed
     */
    public boolean dispatch(int programNumber)
    {
        if (programNumber == 0)
        {
            // Program 0 is idle, do nothing
            return true;
        }

        // Robot programs (1-99)
        if (programNumber >= 1 && programNumber <= 99)
        {
            RobotProgram program = robotPrograms.get(Integer.valueOf(programNumber));
            if (program == null)
            {
                log.warn("Robot program " + programNumber + " not registered");
                return false;
            }

            try
            {
                log.info("Executing robot program " + programNumber + ": " + program.getClass().getSimpleName());
                program.execute(robotContext);
                log.info("Robot program " + programNumber + " completed successfully");
                return true;
            }
            catch (Exception e)
            {
                log.error("Robot program " + programNumber + " failed: " + e.getMessage(), e);
                return false;
            }
        }

        // Vision programs (100-199)
        if (programNumber >= 100 && programNumber <= 199)
        {
            VisionTask task = visionTasks.get(Integer.valueOf(programNumber));
            if (task == null)
            {
                log.warn("Vision task " + programNumber + " not registered");
                return false;
            }

            log.info("Submitting vision task " + programNumber + ": " + task.getClass().getSimpleName());
            visionManager.submitVisionTask(task);
            return true;
        }

        log.warn("Invalid program number: " + programNumber + " (valid range: 0-199)");
        return false;
    }
}
