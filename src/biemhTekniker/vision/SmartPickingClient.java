package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class SmartPickingClient extends RoboticsAPICyclicBackgroundTask {

    private static final Logger log = Logger.getLogger(SmartPickingClient.class);
    private static final String SERVER_IP = "172.31.1.69";
    private static final int PORT = 59002;

    private Socket _socket;
    private boolean _isConnected = false;

    @Override
    public void initialize() {
        initializeCyclic(0, 1000, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
        log.info("SmartPickingClient initialized. Target: " + SERVER_IP + ":" + PORT);
    }

    @Override
    public void runCyclic() {
        if (!_isConnected) {
            tryToConnect();
        } else {
            maintainConnection();
        }
    }

    private void tryToConnect() {
        try {
            log.debug("Attempting to connect to Vision Server...");
            _socket = new Socket();
            // Use a short timeout so we don't block the cyclic task too long
            _socket.connect(new InetSocketAddress(SERVER_IP, PORT), 500);

            _isConnected = true;
            log.info("Successfully connected to Vision Server.");

        } catch (Exception e) {
            _isConnected = false;
            // We use debug here so we don't litter the SmartPAD with "failed"
            // messages every second if the camera is simply turned off.
            log.debug("Connection failed: " + e.getMessage());
        }
    }

    private void maintainConnection() {
        // Check if the socket is still actually alive
        if (_socket.isClosed() || !_socket.isConnected()) {
            _isConnected = false;
            log.warn("Vision Server connection lost.");
            return;
        }

        // Here you will eventually read your Vision Data
        log.debug("Connection healthy. Waiting for vision data...");
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