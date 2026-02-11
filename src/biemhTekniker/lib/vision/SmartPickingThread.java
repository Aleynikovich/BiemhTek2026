package biemhTekniker.lib.vision;

import biemhTekniker.lib.config.ConfigManager;
import biemhTekniker.lib.logger.Logger;
import com.kuka.common.ThreadUtil;

/**
 * Thread-based SmartPicking client for vision system communication.
 * Maintains persistent connection to vision server.
 * Thread monitors connection and handles reconnection automatically.
 * Thread-safe using volatile variables for inter-thread communication.
 */
public class SmartPickingThread extends Thread
{

    private static final Logger log = Logger.getLogger(SmartPickingThread.class);

    // Configurable connection parameters (loaded from application.properties)
    private final int initialRetryDelayMs;
    private final int maxRetryDelayMs;
    private final double backoffMultiplier;
    private final int connectionCheckIntervalMs;
    private final int maxConsecutiveErrors;

    private final String visionServerIP;
    private final int visionServerPort;
    private VisionSocketClient socketClient;
    private SmartPickingProtocol protocol;
    private volatile boolean running = true;
    // Note: currentRetryDelay is thread-confined (only accessed by this thread's run() method)
    // volatile ensures visibility if needed for debugging/monitoring but not required for correctness
    private volatile int currentRetryDelay;

    /**
     * Creates a SmartPicking thread.
     *
     * @param visionServerIP   Vision server IP address
     * @param visionServerPort Vision server port
     */
    public SmartPickingThread(String visionServerIP, int visionServerPort)
    {
        super("SmartPickingThread");
        this.visionServerIP = visionServerIP;
        this.visionServerPort = visionServerPort;
        setDaemon(true); // Thread will not prevent JVM shutdown
        
        // Load configuration
        ConfigManager config = ConfigManager.getInstance();
        this.initialRetryDelayMs = config.getInt("vision.retry.initial.delay.ms", 1000);
        this.maxRetryDelayMs = config.getInt("vision.retry.max.delay.ms", 60000);
        this.backoffMultiplier = config.getDouble("vision.retry.backoff.multiplier", 2.0);
        this.connectionCheckIntervalMs = config.getInt("vision.connection.check.interval.ms", 5000);
        this.maxConsecutiveErrors = config.getInt("vision.max.consecutive.errors", 10);
        this.currentRetryDelay = initialRetryDelayMs;
        
        log.debug("SmartPickingThread configuration: initialRetryDelay=" + initialRetryDelayMs + 
                  "ms, maxRetryDelay=" + maxRetryDelayMs + "ms, backoffMultiplier=" + backoffMultiplier +
                  ", checkInterval=" + connectionCheckIntervalMs + "ms, maxErrors=" + maxConsecutiveErrors);
    }

    /**
     * Initializes the vision socket client and protocol.
     */
    public void initialize()
    {
        log.info("SmartPickingThread initializing...");
        socketClient = new VisionSocketClient(visionServerIP, visionServerPort);
        protocol = new SmartPickingProtocol(socketClient);
        log.info("SmartPickingThread initialized.");
    }

    @Override
    public void run()
    {
        log.info("SmartPickingThread started.");

        // Verify initialization completed
        if (socketClient == null || protocol == null)
        {
            log.error("SmartPickingThread not properly initialized. Call initialize() first.");
            return;
        }

        int consecutiveErrors = 0;

        // Main thread loop - monitors connection and maintains it
        while (running && !Thread.currentThread().isInterrupted())
        {
            try
            {
                if (!socketClient.isConnected() || !socketClient.testConnection())
                {
                    if (socketClient.isConnected())
                    {
                        log.info("Connection health check failed, reconnecting...");
                        socketClient.close();
                    } else
                    {
                        log.info("Connection lost, attempting to reconnect in " + currentRetryDelay + "ms...");
                    }

                    // Wait before attempting reconnection (exponential backoff)
                    ThreadUtil.milliSleep(currentRetryDelay);

                    socketClient.connect();
                    if (socketClient.isConnected())
                    {
                        log.info("Reconnected to vision server");
                        currentRetryDelay = initialRetryDelayMs; // Reset backoff on success
                        consecutiveErrors = 0; // Reset on success
                    } else
                    {
                        // Increase delay for next attempt (exponential backoff)
                        currentRetryDelay = (int) Math.min(currentRetryDelay * backoffMultiplier, maxRetryDelayMs);
                    }
                } else
                {
                    // Connection is healthy, reset error counter and backoff
                    consecutiveErrors = 0;
                    currentRetryDelay = initialRetryDelayMs;
                    // Check less frequently when connected
                    ThreadUtil.milliSleep(connectionCheckIntervalMs);
                }
            } catch (Exception e)
            {
                consecutiveErrors++;
                log.error("Connection monitor error (" + consecutiveErrors + "/" + maxConsecutiveErrors + "): " + e.getMessage());

                if (consecutiveErrors >= maxConsecutiveErrors)
                {
                    log.error("Maximum consecutive errors reached. Thread will exit.");
                    break; // Exit the while loop to properly shut down
                }

                // Increase delay for next attempt (exponential backoff)
                currentRetryDelay = (int) Math.min(currentRetryDelay * backoffMultiplier, maxRetryDelayMs);
                ThreadUtil.milliSleep(currentRetryDelay);
            }
        }

        cleanup();
        log.info("SmartPickingThread stopped.");
    }

    /**
     * Cleans up resources.
     */
    private void cleanup()
    {
        if (socketClient != null)
        {
            socketClient.close();
        }
    }

    /**
     * Stops the thread gracefully.
     */
    public void shutdown()
    {
        log.info("SmartPickingThread shutdown requested.");
        running = false;
    }

    /**
     * Checks if the thread is still running.
     *
     * @return true if running, false otherwise
     */
    public boolean isRunning()
    {
        return running && isAlive();
    }

    /**
     * Gets the current protocol instance for program subroutines.
     *
     * @return SmartPickingProtocol instance, or null if not initialized
     */
    public SmartPickingProtocol getProtocol()
    {
        return protocol;
    }

    /**
     * Checks if connected to vision server.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected()
    {
        return socketClient != null && socketClient.isConnected();
    }
}
