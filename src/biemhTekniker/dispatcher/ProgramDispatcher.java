package biemhTekniker.dispatcher;

import biemhTekniker.logger.Logger;
import biemhTekniker.model.ProgramDescriptor;
import biemhTekniker.model.ProgramType;
import biemhTekniker.registry.ProgramRegistry;
import biemhTekniker.tasks.ProgramTask;
import biemhTekniker.tasks.TaskResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Dispatcher for program tasks.
 * Loads program descriptors from registry and dispatches to appropriate handlers.
 * VISION tasks are executed asynchronously (non-blocking).
 * ROBOT tasks are executed synchronously on the main thread.
 * Java 7 compatible.
 */
public class ProgramDispatcher {
    
    private static final Logger log = Logger.getLogger(ProgramDispatcher.class);
    
    private final ProgramRegistry registry;
    private final List<ProgramTaskFactory> factories;
    private final ExecutorService visionExecutor;
    
    /**
     * Create a new program dispatcher.
     * 
     * @param registry Program registry to load descriptors from
     */
    public ProgramDispatcher(ProgramRegistry registry) {
        this.registry = registry;
        this.factories = new ArrayList<ProgramTaskFactory>();
        // Single-threaded executor for vision tasks to avoid concurrent camera requests
        this.visionExecutor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Register a task factory.
     * 
     * @param factory The factory to register
     */
    public void registerFactory(ProgramTaskFactory factory) {
        factories.add(factory);
        log.info("Registered task factory: " + factory.getClass().getSimpleName());
    }
    
    /**
     * Dispatch a program by program number.
     * VISION tasks are executed asynchronously (returns immediately).
     * ROBOT tasks are executed synchronously (blocks until complete).
     * 
     * @param programNumber The program number to execute
     * @param onComplete Callback to run after task completes (typically resets programNumber to 0)
     * @return TaskResult for synchronous tasks, or null for async tasks
     */
    public TaskResult dispatch(int programNumber, Runnable onComplete) {
        log.info("Dispatching program " + programNumber);
        
        // Load program descriptor from registry
        ProgramDescriptor descriptor = registry.getProgram(programNumber);
        if (descriptor == null) {
            log.error("Program " + programNumber + " not found in registry");
            if (onComplete != null) {
                onComplete.run();
            }
            return TaskResult.failure("Program not found in registry");
        }
        
        if (!descriptor.getEnabled()) {
            log.warn("Program " + programNumber + " is disabled");
            if (onComplete != null) {
                onComplete.run();
            }
            return TaskResult.failure("Program is disabled");
        }
        
        // Find a factory that can handle this program
        ProgramTask task = null;
        for (ProgramTaskFactory factory : factories) {
            if (factory.canHandle(descriptor)) {
                task = factory.createTask(descriptor);
                break;
            }
        }
        
        if (task == null) {
            log.error("No factory found for program " + programNumber + " (" + descriptor.getProgramName() + ")");
            if (onComplete != null) {
                onComplete.run();
            }
            return TaskResult.failure("No handler registered for this program");
        }
        
        // Dispatch based on program type
        if (descriptor.getProgramType() == ProgramType.VISION) {
            // Asynchronous execution for vision tasks
            log.info("Executing VISION task asynchronously: " + descriptor.getProgramName());
            executeVisionTaskAsync(task, onComplete);
            return null; // Async, no immediate result
        } else {
            // Synchronous execution for robot tasks
            log.info("Executing ROBOT task synchronously: " + descriptor.getProgramName());
            TaskResult result = executeRobotTaskSync(task);
            if (onComplete != null) {
                onComplete.run();
            }
            return result;
        }
    }
    
    /**
     * Execute a vision task asynchronously.
     */
    private void executeVisionTaskAsync(final ProgramTask task, final Runnable onComplete) {
        visionExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    TaskResult result = task.execute();
                    if (result.isSuccess()) {
                        log.info("Vision task completed: " + result.getMessage());
                    } else {
                        log.error("Vision task failed: " + result.getMessage());
                    }
                } catch (Exception e) {
                    log.error("Vision task threw exception: " + e.getMessage());
                } finally {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        });
    }
    
    /**
     * Execute a robot task synchronously.
     */
    private TaskResult executeRobotTaskSync(ProgramTask task) {
        try {
            TaskResult result = task.execute();
            if (result.isSuccess()) {
                log.info("Robot task completed: " + result.getMessage());
            } else {
                log.error("Robot task failed: " + result.getMessage());
            }
            return result;
        } catch (Exception e) {
            log.error("Robot task threw exception: " + e.getMessage());
            return TaskResult.failure("Task execution exception", e);
        }
    }
    
    /**
     * Shutdown the dispatcher and its executor service.
     */
    public void shutdown() {
        log.info("Shutting down program dispatcher");
        visionExecutor.shutdown();
    }
}
