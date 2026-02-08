package biemhTekniker.tasks;

/**
 * Interface for all program tasks.
 * Tasks can be either VISION (asynchronous) or ROBOT (synchronous).
 */
public interface ProgramTask
{
    /**
     * Execute the task.
     * @return TaskResult indicating success or failure
     * @throws Exception if task execution fails
     */
    TaskResult execute() throws Exception;

    /**
     * Get the program number this task handles.
     * @return program number
     */
    int getProgramNumber();

    /**
     * Get the task name.
     * @return task name
     */
    String getTaskName();
}
