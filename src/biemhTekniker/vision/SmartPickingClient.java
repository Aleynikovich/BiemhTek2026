package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPIBackgroundTask;
import com.kuka.generated.ioAccess.VisionInputsIOGroup;
import com.kuka.generated.ioAccess.VisionOutputsIOGroup;
import biemhTekniker.vision.SmartPickingProtocol.Command;
import biemhTekniker.vision.SmartPickingProtocol.VisionResult;
import javax.inject.Inject;

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
                Thread.sleep(100);
            } catch (InterruptedException e) {
                _running = false;
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
                executeRunSequence();
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

    private void executeRunSequence() {
        visionOutputs.setDataRequestSent(true);

        Command[] steps = {
                Command.CAPTURE_DATA,
                Command.LOCATE_CONTAINER,
                Command.LOCATE_PARTS,
                Command.GET_PART_POS
        };

        boolean success = true;

        for (int i = 0; i < steps.length; i++) {
            if (!_running) return;
            VisionResult res = _protocol.execute(steps[i]);

            if (!res.isSuccess()) {
                log.error("Step " + steps[i] + " failed.");
                success = false;
                break;
            }

            // --- BRIDGE UPDATE START ---
            // If we successfully got part positions, save them to the bridge
            if (steps[i] == Command.GET_PART_POS) {
                VisionDataBridge.get().update(
                        res.getX(), res.getY(), res.getZ(),
                        res.getRx(), res.getRy(), res.getRz()
                );
                log.info("Part found at X=" + res.getX() + ", Y=" + res.getY());
            }
            // --- BRIDGE UPDATE END ---
        }

        if (success) {
            visionOutputs.setPickPositionReady(true);
            waitForInputLow(new InputCheck() {
                public boolean isHigh() { return visionInputs.getDataRequest(); }
            });
        }

        visionOutputs.setDataRequestSent(false);
        visionOutputs.setPickPositionReady(false);
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