package biemhTekniker.managers;

import biemhTekniker.logger.LogCollector;
import biemhTekniker.logger.LogManager;
import biemhTekniker.logger.LogPublisher;
import biemhTekniker.logger.Logger;

/**
 * Manages logging initialization and lifecycle.
 * Centralizes logging setup to keep Main.java clean.
 */
public class LoggingManager
{

    private static final Logger log = Logger.getLogger(LoggingManager.class);
    private LogPublisher logPublisher;

    /**
     * Initializes the logging system with collector and publisher.
     *
     * @throws RuntimeException if logging initialization fails
     */
    public void initialize()
    {
        try
        {
            LogCollector logCollector = new LogCollector();
            LogManager.register(logCollector);

            logPublisher = new LogPublisher(logCollector);
            logPublisher.start();

            log.info("Logging initialized");
        } catch (Exception e)
        {
            throw new RuntimeException("Failed to initialize logging: " + e.getMessage(), e);
        }
    }

    /**
     * Stops the logging publisher if it's running.
     */
    public void shutdown()
    {
        if (logPublisher != null)
        {
            logPublisher.stop();
            log.info("Logging shutdown complete");
        }
    }
}
