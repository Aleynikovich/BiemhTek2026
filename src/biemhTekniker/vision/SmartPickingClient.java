package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;
import com.kuka.generated.ioAccess.VisionIOGroup;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class SmartPickingClient extends RoboticsAPICyclicBackgroundTask {

    private static final Logger log = Logger.getLogger(SmartPickingClient.class);
    private static final String SERVER_IP = "172.31.1.69";
    private static final int PORT = 59002;

    private Socket _socket;
    private OutputStream _out;
    private InputStream _in;
    private boolean _isConnected = false;

    @Inject
    private VisionIOGroup vision;

    @Override
    public void initialize() {
        initializeCyclic(0, 1000, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
        log.info("SmartPickingClient initialized. Target: " + SERVER_IP + ":" + PORT);
        tryToConnect();
    }

    @Override
    public void runCyclic() {
        if (!_isConnected) {
            tryToConnect();
        } else {
            if (_socket == null || _socket.isClosed()) {
                _isConnected = false;
                return;
            }

            if (vision.getTriggerRequest()) {
                sendAndReceive();
            }
        }
    }

    private void tryToConnect() {
        try {
            if (_socket != null && !_socket.isClosed()) return;

            log.debug("Attempting to connect to Smart Picking Server...");
            _socket = new Socket(SERVER_IP, PORT);
            
            _socket.setSoTimeout(500); 
            
            _out = _socket.getOutputStream();
            _in = _socket.getInputStream();
            
            _isConnected = true;
            log.info("Connected successfully.");

        } catch (Exception e) {
            _isConnected = false;
        }
    }

    private void sendAndReceive() {
        try {
            String payload = "15;BIEMH26_105055\r\n";
            
            // FIX: Use string "US-ASCII" instead of StandardCharsets.US_ASCII
            _out.write(payload.getBytes("US-ASCII"));
            _out.flush();

            Thread.sleep(50);

            byte[] buffer = new byte[128];
            int bytesRead = _in.read(buffer);

            if (bytesRead > 0) {
                // FIX: Use string "US-ASCII" here as well
                String response = new String(buffer, 0, bytesRead, "US-ASCII");
                log.info("Server Response: " + response);
            } else {
                log.info("No data received.");
            }
            
            // vision.setTriggerRequest(false);

        } catch (Exception e) {
            log.error("Communication error: " + e.getMessage());
            _isConnected = false;
            try { _socket.close(); } catch (Exception ignored) {}
        }
    }

    @Override
    public void dispose() {
        try {
            if (_socket != null) _socket.close();
        } catch (Exception e) {
            log.error("Error closing vision socket: " + e.getMessage());
        }
        super.dispose();
    }
}