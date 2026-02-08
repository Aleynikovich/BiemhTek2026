package biemhTekniker.tasks;

import biemhTekniker.logger.Logger;

/**
 * Abstract base class for ROBOT tasks.
 * Robot tasks run synchronously on the main/application thread.
 * They can perform robot motions using iiwa APIs.
 */
public abstract class RobotTask implements ProgramTask
{
    protected static final Logger log = Logger.getLogger(RobotTask.class);

    protected final int    programNumber;
    protected final String taskName;

    public RobotTask(int programNumber, String taskName)
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
     * Execute the robot task.
     * This method runs synchronously on the main thread and can perform robot motions.
     * 
     * @return TaskResult indicating success or failure
     * @throws Exception if task execution fails
     */
    @Override public abstract TaskResult execute() throws Exception;
}
