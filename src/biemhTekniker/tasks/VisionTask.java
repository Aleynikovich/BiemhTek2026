package biemhTekniker.tasks;

import biemhTekniker.logger.Logger;

/**
 * Abstract base class for VISION tasks.
 * Vision tasks run asynchronously and do NOT perform robot motions.
 * They interact with the vision system and may post results to the config service.
 */
public abstract class VisionTask implements ProgramTask
{
    protected static final Logger log = Logger.getLogger(VisionTask.class);

    protected final int    programNumber;
    protected final String taskName;

    public VisionTask(int programNumber, String taskName)
    {
        this.programNumber = programNumber;
        this.taskName      = taskName;
    }

    @Override public int getProgramNumber()
    {
        return programNumber;
    }

    @Override public String getTaskName()
    {
        return taskName;
    }

    /**
     * Execute the vision task.
     * This method runs asynchronously and must NOT call any robot motion APIs.
     * 
     * @return TaskResult indicating success or failure
     * @throws Exception if task execution fails
     */
    @Override public abstract TaskResult execute() throws Exception;
}
