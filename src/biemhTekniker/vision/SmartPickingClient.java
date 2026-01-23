package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPIBackgroundTask;
import com.kuka.generated.ioAccess.VisionInputsIOGroup;
import com.kuka.generated.ioAccess.VisionOutputsIOGroup;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
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
    
    private static final String CMD_SUCCESS = "0";
    private static final String CMD_FAILURE = "-1";

    private Socket _socket;
    private Scanner _in;
    private PrintWriter _out;
    private boolean _isConnected = false;
    private boolean _referenceLoaded = false;
    private volatile boolean _running = true;
    
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
        while (_running) {
            try {
                if (!_isConnected || _socket == null || _socket.isClosed() || !_socket.isConnected()) {
                    _referenceLoaded = false;
                    _currentCameraMode = 0;
                    tryToConnect();
                } else {
                    if (!_referenceLoaded) {
                        log.info("Attempting to load reference...");
                        // Executing the sequence defined in your working version
                        performTransaction("15;BIEMH26_105055");
                        performTransaction("19");
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

            } catch (InterruptedException e) {
                log.info("SmartPickingClient thread interrupted.");
                _running = false;
            } catch (Exception e) {
                if (_running) {
                    log.error("Main Loop Error: " + e.getMessage());
                    _isConnected = false;
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                }
            }
        }
        closeConnection();
        log.info("SmartPickingClient thread finished.");
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
            if (!_running) return;
            String resp = performTransaction(sequence[i]);
            if (resp == null || CMD_FAILURE.equals(resp)) {
                log.error("Sequence failed at step " + sequence[i] + ". Resp: " + resp);
                success = false;
                break;
            }
        }

        if (success) {
            visionOutputs.setPickPositionReady(true);
            while (visionInputs.getDataRequest() && _running) {
                try { Thread.sleep(50); } catch (InterruptedException e) { _running = false; }
            }
        }
        
        visionOutputs.setDataRequestSent(false);
        visionOutputs.setPickPositionReady(false);
    }

    private void executeCalibrationPlaceholder() {
        visionOutputs.setCalibrationComplete(true);
        while (visionInputs.getCalibrationRequest() && _running) {
            try { Thread.sleep(50); } catch (InterruptedException e) { _running = false; }
        }
        visionOutputs.setCalibrationComplete(false);
    }

    private void tryToConnect() {
        try {
            closeConnection();
            if (!_running) return;

            _socket = new Socket();
            _socket.setReuseAddress(true);
            _socket.connect(new InetSocketAddress(SERVER_IP, PORT), 5000);
            _socket.setSoTimeout(10000); 
            
            _in = new Scanner(_socket.getInputStream(), "US-ASCII");
            // Set delimiter to accept parentheses and commas as token separators if needed, 
            // but we will handle cleaning manually for better control.
            
            _out = new PrintWriter(new OutputStreamWriter(_socket.getOutputStream(), "US-ASCII"), true);
            
            _isConnected = true;
            log.info("Connected to Vision Server.");
        } catch (Exception e) {
            _isConnected = false;
        }
    }

    private String performTransaction(String message) {
        if (!_running || !_isConnected) return null;
        try {
            _out.print(message);
            _out.flush();
            log.info("Sent: [" + message + "]");

            // Scanner.next() blocks until a token is available
            if (_in.hasNext()) {
                String raw = _in.next();
                log.debug("Received Raw: [" + raw + "]");
                
                String cleaned = raw.replace("(", "").replace(")", "").trim();
                if (cleaned.contains(",")) {
                    cleaned = cleaned.split(",")[0].trim();
                }
                
                log.info("Result: [" + cleaned + "]");
                return cleaned;
            } else {
                log.warn("No data available from camera.");
                _isConnected = false;
            }
        } catch (Exception e) {
            if (_running) {
                log.error("Comm error on [" + message + "]: " + e.getMessage());
                _isConnected = false;
            }
        }
        return null;
    }

    private void resetOutputs() {
        visionOutputs.setDataRequestSent(false);
        visionOutputs.setPickPositionReady(false);
        visionOutputs.setCalibrationComplete(false);
    }

    private void closeConnection() {
        _isConnected = false;
        if (_in != null) _in.close();
        if (_out != null) _out.close();
        try { if (_socket != null) _socket.close(); } catch (Exception e) {}
        _in = null;
        _out = null;
        _socket = null;
    }

    @Override
    public void dispose() {
        _running = false;
        closeConnection();
        super.dispose();
    }
}