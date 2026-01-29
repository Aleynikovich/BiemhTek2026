package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.VisionInputsIOGroup;
import com.kuka.generated.ioAccess.VisionOutputsIOGroup;
import biemhTekniker.vision.SmartPickingProtocol.Command;

/**
 * Thread-based SmartPicking client for vision system communication.
 * Replaces the RoboticsAPIBackgroundTask implementation for better integration.
 * Thread-safe using volatile variables for inter-thread communication.
 */
public class SmartPickingThread extends Thread {

    private static final Logger log = Logger.getLogger(SmartPickingThread.class);

    /**
     * Operating mode of the vision system.
     */
    private enum Mode {
        NONE,
        AUTO,
        CALIBRATION
    }

    private final VisionInputsIOGroup visionInputs;
    private final VisionOutputsIOGroup visionOutputs;
    private final String visionServerIP;
    private final int visionServerPort;

    private VisionSocketClient socketClient;
    private SmartPickingProtocol protocol;

    private volatile boolean referenceLoaded = false;
    private volatile Mode currentMode = Mode.NONE;
    private volatile boolean running = true;
    
    /**
     * Reference identifier for the part being picked.
     * Format: PROJECT_PARTNUMBER (e.g., "BIEMH26_105055")
     * This should match a reference loaded in the vision system.
     */
    private final String reference = "BIEMH26_105055";

    /**
     * Creates a SmartPicking thread.
     * @param visionInputs Vision system input signals
     * @param visionOutputs Vision system output signals
     * @param visionServerIP Vision server IP address
     * @param visionServerPort Vision server port
     */
    public SmartPickingThread(VisionInputsIOGroup visionInputs, 
                             VisionOutputsIOGroup visionOutputs,
                             String visionServerIP,
                             int visionServerPort) {
        super("SmartPickingThread");
        this.visionInputs = visionInputs;
        this.visionOutputs = visionOutputs;
        this.visionServerIP = visionServerIP;
        this.visionServerPort = visionServerPort;
        setDaemon(true); // Thread will not prevent JVM shutdown
    }

    /**
     * Initializes the vision socket client and protocol.
     */
    public void initialize() {
        log.info("SmartPickingThread initializing...");
        socketClient = new VisionSocketClient(visionServerIP, visionServerPort);
        protocol = new SmartPickingProtocol(socketClient);
        resetOutputs();
        log.info("SmartPickingThread initialized.");
    }

    @Override
    public void run() {
        log.info("SmartPickingThread started.");
        
        // Verify initialization completed
        if (socketClient == null || protocol == null) {
            log.error("SmartPickingThread not properly initialized. Call initialize() first.");
            return;
        }
        
        int consecutiveErrors = 0;
        final int MAX_CONSECUTIVE_ERRORS = 10;
        
        while (running) {
            try {
                if (!socketClient.isConnected()) {
                    handleReconnection();
                } else {
                    processWorkCycle();
                }
                consecutiveErrors = 0; // Reset on success
                ThreadUtil.milliSleep(100);
            } catch (Exception e) {
                consecutiveErrors++;
                log.error("Loop Error (" + consecutiveErrors + "/" + MAX_CONSECUTIVE_ERRORS + "): " + e.getMessage());
                
                if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
                    log.error("Maximum consecutive errors reached. Shutting down SmartPickingThread.");
                    running = false;
                }
                ThreadUtil.milliSleep(500); // Longer delay on error
            }
        }
        
        cleanup();
        log.info("SmartPickingThread stopped.");
    }

    /**
     * Handles reconnection when connection is lost.
     */
    private void handleReconnection() {
        referenceLoaded = false;
        currentMode = Mode.NONE;
        socketClient.connect();
    }

    /**
     * Main work cycle - loads reference, handles mode, and processes requests.
     */
    private void processWorkCycle() {
        if (!referenceLoaded) {
            referenceLoaded = protocol.loadReference(reference);
            if (referenceLoaded) {
                log.debug("Loaded reference: " + reference);
            }
            return;
        }

        handleModeSelection();

        if (currentMode == Mode.AUTO) {
            if (visionInputs.getDataRequest()) {
                // executeRunSequence() - to be implemented
            }
        } else if (currentMode == Mode.CALIBRATION) {
            if (visionInputs.getCalibrationRequest()) {
                executeCalibrationSequence();
            }
        }
    }

    /**
     * Handles mode selection based on input signals.
     * If both run and calibration modes are requested, calibration takes priority.
     */
    private void handleModeSelection() {
        boolean runReq = visionInputs.getRunMode();
        boolean calReq = visionInputs.getCalibrationMode();

        // Calibration mode takes priority if both are requested
        Mode targetMode = Mode.NONE;
        if (calReq) {
            targetMode = Mode.CALIBRATION;
            if (runReq) {
                log.warn("Both RUN and CALIBRATION modes requested. Using CALIBRATION.");
            }
        } else if (runReq) {
            targetMode = Mode.AUTO;
        }

        if (targetMode != Mode.NONE && targetMode != currentMode) {
            Command cmd = (targetMode == Mode.AUTO) ? Command.SET_AUTO_MODE : Command.SET_CALIB_MODE;
            if (protocol.setMode(cmd)) {
                currentMode = targetMode;
                log.info("Mode changed to: " + currentMode);
            }
        } else if (targetMode == Mode.NONE) {
            currentMode = Mode.NONE;
        }
    }

    /**
     * Executes calibration sequence.
     * This acknowledges the calibration request signal from PLC.
     * Note: Actual calibration execution is handled separately by CalibrationManager.
     */
    private void executeCalibrationSequence() {
        visionOutputs.setCalibrationComplete(true);
        waitForInputLow(new InputCheck() {
            public boolean isHigh() { 
                return visionInputs.getCalibrationRequest(); 
            }
        });
        visionOutputs.setCalibrationComplete(false);
    }

    /**
     * Waits for an input signal to go low.
     * Thread will be interrupted on shutdown.
     */
    private void waitForInputLow(InputCheck check) {
        while (check.isHigh() && running) {
            try { 
                Thread.sleep(50); 
            } catch (InterruptedException e) {
                log.warn("Input wait interrupted - thread shutting down");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Resets all output signals to false.
     */
    private void resetOutputs() {
        visionOutputs.setDataRequestSent(false);
        visionOutputs.setPickPositionReady(false);
        visionOutputs.setCalibrationComplete(false);
    }

    /**
     * Stops the thread gracefully.
     */
    public void shutdown() {
        log.info("SmartPickingThread shutdown requested.");
        running = false;
    }

    /**
     * Cleans up resources.
     */
    private void cleanup() {
        if (socketClient != null) {
            socketClient.close();
        }
    }

    /**
     * Checks if the thread is still running.
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return running && isAlive();
    }

    /**
     * Gets the current protocol instance for external access.
     * @return SmartPickingProtocol instance, or null if not initialized
     */
    public SmartPickingProtocol getProtocol() {
        return protocol;
    }

    /**
     * Interface for checking input signal state.
     */
    private interface InputCheck {
        boolean isHigh();
    }
}
