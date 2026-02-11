package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import biemhTekniker.vision.VisionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Dispatches vision task execution based on program number.
 * Handles vision programs (100-199) via VisionManager.
 * Provides busy status for PLC handshaking.
 */
public class VisionDispatcher
{
    private static final Logger log = Logger.getLogger(VisionDispatcher.class);
    private final Map<Integer, VisionTask> visionTasks;
    private final VisionManager visionManager;

    /**
     * Creates a vision dispatcher.
     *
     * @param visionManager Vision manager for task execution
     */
    public VisionDispatcher(VisionManager visionManager)
    {
        this.visionManager = visionManager;
        this.visionTasks = new HashMap<Integer, VisionTask>();
    }

    /**
     * Registers a vision task.
     *
     * @param programNumber Program number (100-199)
     * @param task          Vision task instance
     */
    public void registerVisionTask(int programNumber, VisionTask task)
    {
        if (!ProgramRange.isVisionProgram(programNumber))
        {
            throw new IllegalArgumentException("Vision task number must be " + ProgramRange.VISION_MIN + "-" + ProgramRange.VISION_MAX + ", got: " + programNumber);
        }
        visionTasks.put(Integer.valueOf(programNumber), task);
        log.debug("Registered vision task " + programNumber + ": " + task.getClass().getSimpleName());
    }

    /**
     * Registers all standard vision tasks.
     */
    public void registerVisionTasks()
    {
        log.info("Registering vision tasks...");

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
        registerVisionTask(110, new ScanPickedWorkpiece());

        log.info("Vision tasks registered successfully");
    }

    /**
     * Dispatches a vision task for execution.
     * Non-blocking - submits task to VisionManager and returns immediately.
     *
     * @param programNumber Vision task number (100-199)
     * @return true if submission succeeded, false if task not found
     */
    public boolean dispatch(int programNumber)
    {
        if (!ProgramRange.isVisionProgram(programNumber))
        {
            log.warn("Invalid vision task number: " + programNumber + " (valid range: " + ProgramRange.VISION_MIN + "-" + ProgramRange.VISION_MAX + ")");
            return false;
        }

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

    /**
     * Checks if a vision task is currently running.
     *
     * @return true if vision is busy with a task
     */
    public boolean isBusy()
    {
        return visionManager.isTaskRunning();
    }

    /**
     * Gets the vision manager instance.
     *
     * @return Vision manager
     */
    public VisionManager getVisionManager()
    {
        return visionManager;
    }
}
