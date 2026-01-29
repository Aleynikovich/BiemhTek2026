package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPIBackgroundTask;
import com.kuka.generated.ioAccess.VisionInputsIOGroup;
import com.kuka.generated.ioAccess.VisionOutputsIOGroup;
import biemhTekniker.vision.SmartPickingProtocol.Command;
import biemhTekniker.vision.SmartPickingProtocol.VisionResult;
import javax.inject.Inject;

/**
 * @deprecated This class has been replaced by {@link SmartPickingThread}.
 * Use SmartPickingThread for better integration with the main application.
 * This file is kept for reference only.
 */
@Deprecated
public class SmartPickingClient extends RoboticsAPIBackgroundTask {

    private static final Logger log = Logger.getLogger(SmartPickingClient.class);

    private enum Mode {
        NONE,
        AUTO,
        CALIBRATION
    }

    @Inject
    private VisionInputsIOGroup visionInputs;
    @Inject
    private VisionOutputsIOGroup visionOutputs;

    private VisionSocketClient _socketClient;
    private SmartPickingProtocol _protocol;

    private boolean _referenceLoaded = false;
    private Mode _currentMode = Mode.NONE;
    private volatile boolean _running = true;
    private String reference = "BIEMH26_105055";

    @Override
    public void initialize() {
        log.info("SmartPickingClient initialized.");
        _socketClient = new VisionSocketClient("172.31.1.69", 59002);
        _protocol = new SmartPickingProtocol(_socketClient);
        resetOutputs();
    }

    @Override
    public void run() {
        while (_running) {
            try {
                if (!_socketClient.isConnected()) {
                    handleReconnection();
                } else {
                    processWorkCycle();
                }
                ThreadUtil.milliSleep(100);
            } catch (Exception e) {
                log.error("Loop Error: " + e.getMessage());
            }
        }
        _socketClient.close();
    }

    private void handleReconnection() {
        _referenceLoaded = false;
        _currentMode = Mode.NONE;
        _socketClient.connect();
    }

    private void processWorkCycle() {
        if (!_referenceLoaded) {
            _referenceLoaded = _protocol.loadReference(reference);
            if (_referenceLoaded) {
                log.debug("Loaded reference: " + reference);
            }
            return;
        }

        handleModeSelection();

        if (_currentMode == Mode.AUTO) {
            if (visionInputs.getDataRequest()) {
                //executeRunSequence();
            }
        } else if (_currentMode == Mode.CALIBRATION) {
            if (visionInputs.getCalibrationRequest()) {
                executeCalibrationSequence();
            }
        }
    }

    private void handleModeSelection() {
        boolean runReq = visionInputs.getRunMode();
        boolean calReq = visionInputs.getCalibrationMode();

        Mode targetMode = runReq ? Mode.AUTO : (calReq ? Mode.CALIBRATION : Mode.NONE);

        if (targetMode != Mode.NONE && targetMode != _currentMode) {
            Command cmd = (targetMode == Mode.AUTO) ? Command.SET_AUTO_MODE : Command.SET_CALIB_MODE;
            if (_protocol.setMode(cmd)) {
                _currentMode = targetMode;
                log.info("Mode changed to: " + _currentMode);
            }
        } else if (targetMode == Mode.NONE) {
            _currentMode = Mode.NONE;
        }
    }

    private void executeCalibrationSequence() {
        visionOutputs.setCalibrationComplete(true);
        waitForInputLow(new InputCheck() {
            public boolean isHigh() { return visionInputs.getCalibrationRequest(); }
        });
        visionOutputs.setCalibrationComplete(false);
    }

    private void waitForInputLow(InputCheck check) {
        while (check.isHigh() && _running) {
            try { Thread.sleep(50); } catch (InterruptedException e) { _running = false; }
        }
    }

    private void resetOutputs() {
        visionOutputs.setDataRequestSent(false);
        visionOutputs.setPickPositionReady(false);
        visionOutputs.setCalibrationComplete(false);
    }

    @Override
    public void dispose() {
        _running = false;
        if (_socketClient != null) {
            _socketClient.close();
        }
        super.dispose();
    }

    private interface InputCheck {
        boolean isHigh();
    }
}