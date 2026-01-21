package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class SmartPickingClient extends RoboticsAPICyclicBackgroundTask {

    private static final Logger log = Logger.getLogger(SmartPickingClient.class);
    private static final String SERVER_IP = "172.31.1.69";
    private static final int PORT = 59002;

    private Socket _socket;
    private PrintWriter _writer;
    private BufferedReader _reader;
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
            handleCommunication();
        }
    }

    private void tryToConnect() {
        try {
            log.debug("Attempting to connect to Smart Picking Server...");
            _socket = new Socket();
            _socket.connect(new InetSocketAddress(SERVER_IP, PORT), 500);

            _writer = new PrintWriter(_socket.getOutputStream(), true);
            _reader = new BufferedReader(new InputStreamReader(_socket.getInputStream()));

            _isConnected = true;
            log.info("Successfully connected to Smart Picking Server.");

        } catch (Exception e) {
            _isConnected = false;
            // We use debug here so we don't litter the SmartPAD with "failed"
            // messages every second if the camera is simply turned off.
            log.debug("Connection failed: " + e.getMessage());
        }
    }

    private void handleCommunication()
    {
        try
        {
            // 1. Check if MAIN sent something to the OUTBOX
            String toServer = VisionGateway.pollOutbox();
            if (toServer != null)
            {
                _writer.println(toServer);
            }

            // 2. Check if SERVER sent something to the INBOX
            if (_reader.ready())
            { // Don't wait if there is no data
                String fromServer = _reader.readLine();
                if (fromServer != null)
                {
                    // Put it in the Gateway so Main can find it
                    VisionGateway.depositResponse(fromServer);
                }
            }
        } catch (IOException e)
        {
            log.error("Communication error: " + e.getMessage());
            cleanup();
        }
    }

    private void maintainConnection() {
        // Check if the socket is still actually alive
        if (_socket.isClosed() || !_socket.isConnected()) {
            _isConnected = false;
            log.warn("Vision Server connection lost.");
            return;
        }

        log.debug("Connection healthy. Waiting for vision data...");
    }

    private void cleanup() {
        _isConnected = false;
        try {
            if (_writer != null) _writer.close();
            if (_socket != null) _socket.close();
        } catch (Exception e) {
            // Silent catch for cleanup
        }
        _writer = null;
        _socket = null;
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