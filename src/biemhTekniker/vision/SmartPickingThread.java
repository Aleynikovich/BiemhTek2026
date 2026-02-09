package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
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

    private final String visionServerIP;
    private final int visionServerPort;
    private VisionSocketClient socketClient;
    private SmartPickingProtocol protocol;
    private volatile boolean running = true;

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
        final int MAX_CONSECUTIVE_ERRORS = 10;

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
                        log.info("Connection lost, attempting to reconnect...");
                    }

                    socketClient.connect();
                    if (socketClient.isConnected())
                    {
                        log.info("Reconnected to vision server");
                    }
                }
                consecutiveErrors = 0; // Reset on success
                ThreadUtil.milliSleep(1000); // Check connection every second
            } catch (Exception e)
            {
                consecutiveErrors++;
                log.error("Connection monitor error (" + consecutiveErrors + "/" + MAX_CONSECUTIVE_ERRORS + "): " + e.getMessage());

                if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS)
                {
                    log.error("Maximum consecutive errors reached. Shutting down SmartPickingThread.");
                    running = false;
                }
                ThreadUtil.milliSleep(2000); // Longer delay on error
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
