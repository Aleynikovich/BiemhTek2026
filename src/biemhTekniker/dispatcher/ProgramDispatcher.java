package biemhTekniker.dispatcher;

import biemhTekniker.logger.Logger;
import biemhTekniker.model.ProgramDescriptor;
import biemhTekniker.model.ProgramType;
import biemhTekniker.registry.ProgramRegistry;
import biemhTekniker.tasks.ProgramTask;
import biemhTekniker.tasks.TaskResult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Main dispatcher for program execution.
 * Routes programs to appropriate task handlers and executes them based on type:
 * - VISION programs: Execute asynchronously (non-blocking)
 * - ROBOT programs: Execute synchronously on main thread (blocking)
 */
public class ProgramDispatcher
{
    private static final Logger log = Logger.getLogger(ProgramDispatcher.class);

    private final ProgramRegistry    registry;
    private final ProgramTaskFactory taskFactory;
    private final ExecutorService    visionExecutor;

    /**
     * Create a new dispatcher.
     *
     * @param registry    program registry for loading program descriptors
     * @param taskFactory factory for creating task instances
     */
    public ProgramDispatcher(ProgramRegistry registry, ProgramTaskFactory taskFactory)
    {
        this.registry     = registry;
        this.taskFactory  = taskFactory;
        // Single-threaded executor for vision tasks to avoid concurrent camera requests
        this.visionExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * Dispatch and execute a program by its number.
     *
     * @param programNumber    the program number to execute
     * @param onComplete       callback to execute after completion (e.g., reset program number)
     * @return true if dispatch was successful, false otherwise
     */
    public boolean dispatch(int programNumber, Runnable onComplete)
    {
        try
        {
            // Load program descriptor from registry
            ProgramDescriptor descriptor = registry.getProgram(programNumber);

            if (descriptor == null)
            {
                log.warn("Program " + programNumber + " not found in registry");
                return false;
            }

            if (!descriptor.getEnabled())
            {
                log.warn("Program " + programNumber + " is disabled");
                return false;
            }

            // Create task from factory
            ProgramTask task = taskFactory.createTask(descriptor);

            if (task == null)
            {
                log.error("No task handler found for program " + programNumber);
                return false;
            }

            // Execute based on program type
            if (descriptor.getProgramType() == ProgramType.VISION)
            {
                return dispatchVisionTask(task, onComplete);
            }
            else
            {
                return dispatchRobotTask(task, onComplete);
            }
        }
        catch (Exception e)
        {
            log.error("Dispatch failed for program " + programNumber + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Dispatch a VISION task asynchronously.
     */
    private boolean dispatchVisionTask(final ProgramTask task, final Runnable onComplete)
    {
        log.info("Dispatching VISION task: " + task.getTaskName() + " (async)");

        // Submit to executor for async execution
        Future<TaskResult> future = visionExecutor.submit(new java.util.concurrent.Callable<TaskResult>()
        {
            public TaskResult call()
            {
                try
                {
                    TaskResult result = task.execute();
                    log.info("VISION task " + task.getTaskName() + " completed: " + result);
                    return result;
                }
                catch (Exception e)
                {
                    log.error("VISION task " + task.getTaskName() + " failed: " + e.getMessage());
                    return TaskResult.failure("Task execution failed: " + e.getMessage());
                }
                finally
                {
                    // Execute completion callback
                    if (onComplete != null)
                    {
                        onComplete.run();
                    }
                }
            }
        });

        // Return immediately (non-blocking)
        return true;
    }

    /**
     * Dispatch a ROBOT task synchronously on the main thread.
     */
    private boolean dispatchRobotTask(ProgramTask task, Runnable onComplete)
    {
        log.info("Dispatching ROBOT task: " + task.getTaskName() + " (sync)");

        try
        {
            TaskResult result = task.execute();
            log.info("ROBOT task " + task.getTaskName() + " completed: " + result);
            return result.isSuccess();
        }
        catch (Exception e)
        {
            log.error("ROBOT task " + task.getTaskName() + " failed: " + e.getMessage());
            return false;
        }
        finally
        {
            // Execute completion callback
            if (onComplete != null)
            {
                onComplete.run();
            }
        }
    }

    /**
     * Shutdown the dispatcher and its executor service.
     */
    public void shutdown()
    {
        log.info("Shutting down program dispatcher");
        if (visionExecutor != null)
        {
            visionExecutor.shutdown();
        }
    }
}
