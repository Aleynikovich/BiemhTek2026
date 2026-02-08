package biemhTekniker.programs;

import biemhTekniker.vision.SmartPickingThread;
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
     * Registers all standard programs and tasks.
     */
    public void registerDefaultPrograms(SmartPickingThread smartPickingThread)
    {
        log.info("Registering default programs...");

        // Robot Programs (1-99)
        registerRobotProgram(1, new PickNewWorkpieceProgram());
        registerRobotProgram(2, new PlaceNewWorkpieceProgram());
        registerRobotProgram(3, new PickMeasuredWorkpieceProgram());
        registerRobotProgram(4, new PlaceMeasuredWorkpieceProgram());
        
        // Calibration programs (coordinated - need protocol access)
        CalibrationProgram calibProgram = new CalibrationProgram();
        calibProgram.setProtocol(smartPickingThread.getProtocol());
        registerRobotProgram(5, calibProgram);
        
        TestCalibrationProgram testCalibProgram = new TestCalibrationProgram();
        testCalibProgram.setProtocol(smartPickingThread.getProtocol());
        registerRobotProgram(6, testCalibProgram);

        // Vision Tasks (100-199)
        registerVisionTask(100, new LoadReferencesTask());
        registerVisionTask(101, new IndividualVisionCommandTask(biemhTekniker.vision.SmartPickingProtocol.Command.SET_AUTO_MODE));
        registerVisionTask(102, new IndividualVisionCommandTask(biemhTekniker.vision.SmartPickingProtocol.Command.SET_CALIB_MODE));
        registerVisionTask(103, new IndividualVisionCommandTask(biemhTekniker.vision.SmartPickingProtocol.Command.CAPTURE_DATA));
        registerVisionTask(104, new IndividualVisionCommandTask(biemhTekniker.vision.SmartPickingProtocol.Command.LOCATE_CONTAINER));
        registerVisionTask(105, new IndividualVisionCommandTask(biemhTekniker.vision.SmartPickingProtocol.Command.GET_CONTAINER_POS));
        registerVisionTask(106, new IndividualVisionCommandTask(biemhTekniker.vision.SmartPickingProtocol.Command.LOCATE_PARTS));
        registerVisionTask(107, new IndividualVisionCommandTask(biemhTekniker.vision.SmartPickingProtocol.Command.GET_PART_POS));
        registerVisionTask(108, new IndividualVisionCommandTask(biemhTekniker.vision.SmartPickingProtocol.Command.GET_NEXT_PART_POS));
        registerVisionTask(109, new FullScanTask());
        // Program 110 is Send Custom Message - not registered as it needs message parameter

        // Legacy vision task for backward compatibility
        registerVisionTask(111, new GetNewWorkpiecePositionProgram());

        log.info("Default programs registered successfully");
    }

    /**
     * Checks if a vision task is currently running.
     *
     * @return true if a vision task is active
     */
    public boolean isVisionTaskRunning()
    {
        return visionManager.isTaskRunning();
    }

    /**
     * Gets the vision manager.
     *
     * @return Vision manager instance
     */
    public VisionManager getVisionManager()
    {
        return visionManager;
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
