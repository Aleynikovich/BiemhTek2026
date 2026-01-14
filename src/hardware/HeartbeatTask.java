package hardware;

import java.util.concurrent.atomic.AtomicBoolean;

import com.kuka.generated.ioAccess.RobotStatusIOGroup;

import common.ILogger;

/**
 * Background task that toggles a Profinet heartbeat signal every 100ms.
 * Used to indicate robot is alive to the PLC (cell master).
 * Uses RobotStatusIOGroup ZRes1 signal as heartbeat by default.
 * 
 * Java 1.7 compatible - no lambdas, no diamond operators
 * Non-blocking - runs in background thread
 */
public class HeartbeatTask implements Runnable {
    
    private ILogger logger;
    private RobotStatusIOGroup robotStatusIO;
    private int intervalMs;
    
    private AtomicBoolean running;
    private AtomicBoolean currentState;
    private Thread backgroundThread;
    
    /**
     * Create a heartbeat task using ZRes1 signal
     * @param logger Task logger for debug output
     * @param robotStatusIO RobotStatus I/O group
     * @param intervalMs Toggle interval in milliseconds (default: 100ms)
     */
    public HeartbeatTask(ILogger logger, RobotStatusIOGroup robotStatusIO, int intervalMs) {
        this.logger = logger;
        this.robotStatusIO = robotStatusIO;
        this.intervalMs = intervalMs;
        
        this.running = new AtomicBoolean(false);
        this.currentState = new AtomicBoolean(false);
    }
    
    /**
     * Start the heartbeat task in background
     */
    public void start() {
        if (running.get()) {
            logger.warn("HeartbeatTask already running");
            return;
        }
        
        running.set(true);
        
        backgroundThread = new Thread(this);
        backgroundThread.setDaemon(true);
        backgroundThread.setName("HeartbeatTask-ZRes1");
        backgroundThread.start();
        
        logger.info("HeartbeatTask started (ZRes1, interval: " + intervalMs + "ms)");
    }
    
    /**
     * Stop the heartbeat task
     */
    public void stop() {
        running.set(false);
        
        if (backgroundThread != null) {
            try {
                backgroundThread.interrupt();
                backgroundThread.join(1000);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
        
        logger.info("HeartbeatTask stopped");
    }
    
    /**
     * Background thread that toggles the heartbeat signal
     */
    public void run() {
        while (running.get()) {
            try {
                // Toggle the state
                boolean newState = !currentState.get();
                currentState.set(newState);
                
                // Write to heartbeat signal (ZRes1)
                robotStatusIO.setZRes1(Boolean.valueOf(newState));
                
                // Sleep for the interval
                Thread.sleep(intervalMs);
                
            } catch (InterruptedException e) {
                if (running.get()) {
                    logger.warn("HeartbeatTask interrupted");
                }
                break;
            } catch (Exception e) {
                logger.error("HeartbeatTask error: " + e.getMessage());
                // Continue running even on error
            }
        }
        
        // Set to false on exit
        try {
            robotStatusIO.setZRes1(Boolean.valueOf(false));
        } catch (Exception e) {
            logger.error("Failed to reset heartbeat signal: " + e.getMessage());
        }
    }
    
    public boolean isRunning() {
        return running.get();
    }
    
    public boolean getCurrentState() {
        return currentState.get();
    }
}
