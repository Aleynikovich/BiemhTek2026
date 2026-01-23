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

    @Inject
    private VisionInputsIOGroup visionInputs;
    @Inject
    private VisionOutputsIOGroup visionOutputs;

    private VisionSocketClient _socketClient;
    private SmartPickingProtocol _protocol;

    private boolean _referenceLoaded = false;
    private Command _currentCameraMode = null;
    private volatile boolean _running = true;

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
        _currentCameraMode = null;
        if (_socketClient.connect()) {
            log.info("Reconnected to vision server.");
        }
    }

    private void processWorkCycle() {
        if (!_referenceLoaded) {
            _referenceLoaded = _protocol.loadReference("BIEMH26_105055");
            return;
        }

        handleModeSelection();

        // Check for Run Mode (101)
        if (visionInputs.getRunMode() && _currentCameraMode == Command.SET_AUTO_MODE) {
            if (visionInputs.getDataRequest()) {
                executeRunSequence();
            }
        }
        // Check for Calibration Mode (102)
        else if (visionInputs.getCalibrationMode() && _currentCameraMode == Command.SET_CALIB_MODE) {
            if (visionInputs.getCalibrationRequest()) {
                executeCalibrationSequence();
            }
        }
    }

    private void handleModeSelection() {
        boolean runReq = visionInputs.getRunMode();
        boolean calReq = visionInputs.getCalibrationMode();

        Command targetMode = runReq ? Command.SET_AUTO_MODE : (calReq ? Command.SET_CALIB_MODE : null);

        if (targetMode != null && targetMode != _currentCameraMode) {
            if (_protocol.setMode(targetMode)) {
                _currentCameraMode = targetMode;
                log.info("Mode changed to: " + targetMode.name());
            }
        } else if (targetMode == null) {
            _currentCameraMode = null;
        }
    }

    private void executeRunSequence() {
        visionOutputs.setDataRequestSent(true);

        // Defined steps using the Command enum for protocol compliance
        Command[] steps = {
                Command.CAPTURE_DATA,
                Command.LOCATE_CONTAINER,
                Command.LOCATE_PARTS,
                Command.GET_PART_POS
        };

        boolean success = true;

        for (Command cmd : steps) {
            if (!_running) return;

            VisionResult res = _protocol.execute(cmd);
            if (!res.isSuccess()) {
                log.error("Step " + cmd.name() + " failed.");
                success = false;
                break;
            }
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
        // Logic for specific calibration commands could be added here
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