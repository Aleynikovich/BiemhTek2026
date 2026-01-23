package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPIBackgroundTask;
import com.kuka.generated.ioAccess.VisionInputsIOGroup;
import com.kuka.generated.ioAccess.VisionOutputsIOGroup;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.inject.Inject;

/**
 * Background Task for SmartPicking Vision System.
 * Handles PLC handshaking and synchronous socket communication.
 */
public class SmartPickingClient extends RoboticsAPIBackgroundTask {

    private static final Logger log = Logger.getLogger(SmartPickingClient.class);
    
    private static final String SERVER_IP = "172.31.1.69";
    private static final int PORT = 59002;
    
    // Response codes from camera
    private static final String CMD_SUCCESS = "0";
    private static final String CMD_FAILURE = "-1";

    private Socket _socket;
    private Scanner _in;
    private PrintWriter _out;
    private boolean _isConnected = false;
    private boolean _referenceLoaded = false;
    
    // Internal state to track if we've already notified the camera of the mode
    private int _currentCameraMode = 0; // 0: None, 101: Run, 102: Calib

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
                    // 1. One-time initialization for the reference
                    if (!_referenceLoaded) {
                        String resp = performTransaction("15;BIEMH26_105055");
                        if (CMD_SUCCESS.equals(resp)) {
                            _referenceLoaded = true;
                            log.info("Reference loaded successfully.");
                        } else {
                            log.error("Failed to load reference. Server returned: " + resp);
                        }
                    }

                    // 2. Mode Management (Run vs Calibration)
                    handleModeSelection();

                    // 3. Request Management
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
            String resp = performTransaction("101");
            if (CMD_SUCCESS.equals(resp)) {
                _currentCameraMode = 101;
                log.info("Camera switched to RUN mode (101)");
            }
        } else if (calReq && _currentCameraMode != 102) {
            String resp = performTransaction("102");
            if (CMD_SUCCESS.equals(resp)) {
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
        visionOutputs.setPickPositionReady(false);

        // Sequence of data requests
        boolean success = true;
        String[] sequence = {"2", "3", "4", "9"};
        
        for (int i = 0; i < sequence.length; i++) {
            String resp = performTransaction(sequence[i]);
            if (resp == null || CMD_FAILURE.equals(resp)) {
                log.error("Sequence failed at command [" + sequence[i] + "] with response: " + resp);
                success = false;
                break;
            }
        }

        if (success) {
            visionOutputs.setPickPositionReady(true);
            log.info("Run sequence complete. Pick position ready.");
            
            while (visionInputs.getDataRequest()) {
                try { Thread.sleep(50); } catch (InterruptedException e) {}
            }
            
            visionOutputs.setDataRequestSent(false);
            visionOutputs.setPickPositionReady(false);
        } else {
            visionOutputs.setDataRequestSent(false);
        }
    }

    private void executeCalibrationPlaceholder() {
        log.info("Calibration Request received. (Placeholder logic)");
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
            
            // High-level wrappers for cleaner code
            _in = new Scanner(_socket.getInputStream(), "US-ASCII");
            _out = new PrintWriter(new OutputStreamWriter(_socket.getOutputStream(), "US-ASCII"), true);
            
            _isConnected = true;
            log.info("Connected to Vision Server.");
        } catch (Exception e) {
            _isConnected = false;
        }
    }

    private String performTransaction(String message) {
        try {
            _out.print(message);// + "\r\n");
            _out.flush();

            if (_in.hasNext()) {
                return _in.next();
            }
        } catch (Exception e) {
            log.error("Transaction failed [" + message + "]: " + e.getMessage());
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