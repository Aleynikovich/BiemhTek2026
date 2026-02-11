package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import biemhTekniker.programs.VisionContext;
import biemhTekniker.programs.VisionTask;
import com.kuka.common.ThreadUtil;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages vision tasks execution on the vision thread.
 * Provides non-blocking vision task submission from the robot thread.
 */
public class VisionManager
{
    private static final Logger log = Logger.getLogger(VisionManager.class);
    private static final int TASK_POLL_INTERVAL_MS = 100;

    private final SmartPickingThread smartPickingThread;
    private final VisionContext visionContext;
    private final AtomicReference<VisionTask> pendingTask;
    private final AtomicBoolean taskRunning;
    private Thread visionExecutorThread;
    private volatile boolean running;

    /**
     * Creates a vision manager.
     *
     * @param smartPickingThread SmartPicking thread managing camera connection
     * @param visionContext      Context for vision tasks
     */
    public VisionManager(SmartPickingThread smartPickingThread, VisionContext visionContext)
    {
        this.smartPickingThread = smartPickingThread;
        this.visionContext = visionContext;
        this.pendingTask = new AtomicReference<VisionTask>(null);
        this.taskRunning = new AtomicBoolean(false);
        this.running = true;
    }

    /**
     * Initializes and starts the vision task executor thread.
     */
    public void initialize()
    {
        log.info("VisionManager initializing...");
        visionExecutorThread = new Thread(new Runnable()
        {
            public void run()
            {
                visionExecutorLoop();
            }
        }, "VisionExecutorThread");
        visionExecutorThread.setDaemon(true);
        visionExecutorThread.start();
        log.info("VisionManager initialized.");
    }

    /**
     * Vision executor thread loop.
     * Monitors for pending tasks and executes them.
     */
    private void visionExecutorLoop()
    {
        log.info("VisionExecutorThread started.");
        while (running && !Thread.currentThread().isInterrupted())
        {
            try
            {
                VisionTask task = pendingTask.get();
                if (task != null && !taskRunning.get())
                {
                    // Atomically acquire the task
                    if (pendingTask.compareAndSet(task, null) && taskRunning.compareAndSet(false, true))
                    {
                        try
                        {
                            log.debug("Executing vision task: " + task.getClass().getSimpleName());
                            task.execute(visionContext);
                            log.debug("Vision task completed: " + task.getClass().getSimpleName());
                        } catch (Exception e)
                        {
                            log.error("Vision task failed: " + task.getClass().getSimpleName() + " - " + e.getMessage(), e);
                        } finally
                        {
                            taskRunning.set(false);
                        }
                    }
                }
                ThreadUtil.milliSleep(TASK_POLL_INTERVAL_MS);
            } catch (Exception e)
            {
                log.error("VisionExecutorThread error: " + e.getMessage(), e);
            }
        }
        log.info("VisionExecutorThread stopped.");
    }

    /**
     * Submits a vision task for execution on the vision thread.
     * Non-blocking call - returns immediately.
     *
     * @param task Vision task to execute
     */
    public void submitVisionTask(VisionTask task)
    {
        if (taskRunning.get())
        {
            log.warn("Vision task already running, cannot submit new task: " + task.getClass().getSimpleName());
            return;
        }
        log.info("Submitting vision task: " + task.getClass().getSimpleName());
        pendingTask.set(task);
    }

    /**
     * Gets the SmartPicking protocol for coordinated programs (like calibration).
     *
     * @return SmartPicking protocol instance
     */
    public SmartPickingProtocol getProtocol()
    {
        return smartPickingThread.getProtocol();
    }

    /**
     * Checks if connected to vision server.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected()
    {
        return smartPickingThread.isConnected();
    }

    /**
     * Checks if a vision task is currently running.
     *
     * @return true if a task is running or pending
     */
    public boolean isTaskRunning()
    {
        return taskRunning.get() || pendingTask.get() != null;
    }

    /**
     * Shuts down the vision manager.
     */
    public void shutdown()
    {
        log.info("VisionManager shutdown requested.");
        running = false;
        if (visionExecutorThread != null && visionExecutorThread.isAlive())
        {
            try
            {
                visionExecutorThread.join(5000);
            } catch (InterruptedException e)
            {
                log.warn("Interrupted while waiting for vision executor thread to stop");
                Thread.currentThread().interrupt();
            }
        }
    }
}
