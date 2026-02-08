package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import biemhTekniker.programs.VisionContext;
import biemhTekniker.programs.VisionTask;
import com.kuka.common.ThreadUtil;

/**
 * Manages vision tasks execution on the vision thread.
 * Provides non-blocking vision task submission from the robot thread.
 */
public class VisionManager
{
    private static final Logger              log = Logger.getLogger(VisionManager.class);
    private final        SmartPickingThread  smartPickingThread;
    private final        VisionContext       visionContext;
    private volatile     VisionTask          pendingTask;
    private volatile     boolean             taskRunning;
    private              Thread              visionExecutorThread;
    private volatile     boolean             running;

    /**
     * Creates a vision manager.
     *
     * @param smartPickingThread SmartPicking thread managing camera connection
     * @param visionContext      Context for vision tasks
     */
    public VisionManager(SmartPickingThread smartPickingThread, VisionContext visionContext)
    {
        this.smartPickingThread = smartPickingThread;
        this.visionContext      = visionContext;
        this.taskRunning        = false;
        this.running            = true;
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
                if (pendingTask != null && !taskRunning)
                {
                    VisionTask task = pendingTask;
                    pendingTask = null;
                    taskRunning = true;

                    try
                    {
                        log.debug("Executing vision task: " + task.getClass().getSimpleName());
                        task.execute(visionContext);
                        log.debug("Vision task completed: " + task.getClass().getSimpleName());
                    }
                    catch (Exception e)
                    {
                        log.error("Vision task failed: " + task.getClass().getSimpleName() + " - " + e.getMessage(), e);
                    }
                    finally
                    {
                        taskRunning = false;
                    }
                }
                ThreadUtil.milliSleep(100);
            }
            catch (Exception e)
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
        if (taskRunning)
        {
            log.warn("Vision task already running, cannot submit new task: " + task.getClass().getSimpleName());
            return;
        }
        log.info("Submitting vision task: " + task.getClass().getSimpleName());
        pendingTask = task;
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
        return taskRunning || pendingTask != null;
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
            }
            catch (InterruptedException e)
            {
                log.warn("Interrupted while waiting for vision executor thread to stop");
                Thread.currentThread().interrupt();
            }
        }
    }
}
