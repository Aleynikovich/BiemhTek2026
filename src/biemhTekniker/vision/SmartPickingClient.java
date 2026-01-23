package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPIBackgroundTask;
import com.kuka.generated.ioAccess.VisionInputsIOGroup;
import com.kuka.generated.ioAccess.VisionOutputsIOGroup;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import javax.inject.Inject;

/**
 * Background Task for SmartPicking Vision System.
 * Handles PLC handshaking and synchronous socket communication.
 */
public class SmartPickingClient extends RoboticsAPIBackgroundTask {

    private static final Logger log = Logger.getLogger(SmartPickingClient.class);
    
    private static final String SERVER_IP = "172.31.1.69";
    private static final int PORT = 59002;
    
    private static final String CMD_SUCCESS = "0";
    private static final String CMD_FAILURE = "-1";

    private Socket _socket;
    private InputStream _in;
    private OutputStream _out;
    private boolean _isConnected = false;
    private boolean _referenceLoaded = false;
    
    private int _currentCameraMode = 0; 

    @Inject
    private VisionInputsIOGroup visionInputs;
    @Inject
    private VisionOutputsIOGroup visionOutputs;
    
    @Override
    public void initialize() {
        log.info("SmartPickingClient initialized.");
        resetOutputs();
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!_isConnected || _socket == null || _socket.isClosed()) {
                    _referenceLoaded = false;
                    _currentCameraMode = 0;
                    tryToConnect();
                } else {
                    if (!_referenceLoaded) {
                        log.info("Attempting to load reference...");
                        String resp = performTransaction("15;BIEMH26_105055");
                        if (CMD_SUCCESS.equals(resp)) {
                            _referenceLoaded = true;
                            log.info("Reference loaded successfully.");
                        } else {
                            log.error("Reference load failed. Response: [" + resp + "]");
                        }
                    }

                    handleModeSelection();

                    if (visionInputs.getRunMode() && _currentCameraMode == 101) {
                        if (visionInputs.getDataRequest()) {
                            executeRunSequence();
                        }
                    } else if (visionInputs.getCalibrationMode() && _currentCameraMode == 102) {
                        if (visionInputs.getCalibrationRequest()) {
                            executeCalibrationPlaceholder();
                        }
                    }
                }
                
                Thread.sleep(100); 

            } catch (Exception e) {
                log.error("Main Loop Error: " + e.getMessage());
                _isConnected = false;
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void handleModeSelection() {
        boolean runReq = visionInputs.getRunMode();
        boolean calReq = visionInputs.getCalibrationMode();

        if (runReq && _currentCameraMode != 101) {
            if (CMD_SUCCESS.equals(performTransaction("101"))) {
                _currentCameraMode = 101;
                log.info("Camera switched to RUN mode (101)");
            }
        } else if (calReq && _currentCameraMode != 102) {
            if (CMD_SUCCESS.equals(performTransaction("102"))) {
                _currentCameraMode = 102;
                log.info("Camera switched to CALIBRATION mode (102)");
            }
        } else if (!runReq && !calReq) {
            _currentCameraMode = 0;
        }
    }

    private void executeRunSequence() {
        log.info("Data Request received. Executing sequence 2,3,4,9...");
        visionOutputs.setDataRequestSent(true);

        boolean success = true;
        String[] sequence = {"2", "3", "4", "9"};
        
        for (int i = 0; i < sequence.length; i++) {
            String resp = performTransaction(sequence[i]);
            if (resp == null || CMD_FAILURE.equals(resp)) {
                log.error("Sequence failed at step " + sequence[i] + ". Resp: " + resp);
                success = success && false; // Keep track of failure
                success = false;
                break;
            }
        }

        if (success) {
            visionOutputs.setPickPositionReady(true);
            while (visionInputs.getDataRequest()) {
                try { Thread.sleep(50); } catch (InterruptedException e) {}
            }
        }
        
        visionOutputs.setDataRequestSent(false);
        visionOutputs.setPickPositionReady(false);
    }

    private void executeCalibrationPlaceholder() {
        visionOutputs.setCalibrationComplete(true);
        while (visionInputs.getCalibrationRequest()) {
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
        visionOutputs.setCalibrationComplete(false);
    }

    private void tryToConnect() {
        try {
            if (_socket != null) { try { _socket.close(); } catch(Exception e){} }
            _socket = new Socket(SERVER_IP, PORT);
            _socket.setSoTimeout(25000); 
            
            _in = _socket.getInputStream();
            _out = _socket.getOutputStream();
            
            _isConnected = true;
            log.info("Connected to Vision Server.");
        } catch (Exception e) {
            _isConnected = false;
        }
    }

    private String performTransaction(String message) {
        try {
            _out.write(message.getBytes("US-ASCII"));
            _out.flush();
            log.info("Sent: [" + message + "]");

            byte[] buffer = new byte[1024];
            int bytesRead = _in.read(buffer);

            if (bytesRead > 0) {
                // Remove everything except digits and the minus sign
                // This cleans up null bytes, \r, \n, or spaces that trim() might miss
                String raw = new String(buffer, 0, bytesRead, "US-ASCII");
                String cleaned = raw.replaceAll("[^0-9\\-]", "");
                
                log.info("Received Raw: [" + raw + "] -> Cleaned: [" + cleaned + "]");
                return cleaned;
            } else {
                log.warn("No bytes read from camera.");
            }
        } catch (Exception e) {
            log.error("Comm error on [" + message + "]: " + e.getMessage());
            _isConnected = false;
        }
        return null;
    }

    private void resetOutputs() {
        visionOutputs.setDataRequestSent(false);
        visionOutputs.setPickPositionReady(false);
        visionOutputs.setCalibrationComplete(false);
    }

    @Override
    public void dispose() {
        try { if (_socket != null) _socket.close(); } catch (Exception e) {}
        super.dispose();
    }
}