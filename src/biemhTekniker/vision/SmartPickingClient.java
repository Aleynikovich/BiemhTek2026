package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPIBackgroundTask;
import com.kuka.generated.ioAccess.VisionIOGroup;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import javax.inject.Inject;

public class SmartPickingClient extends RoboticsAPIBackgroundTask {

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
        log.info("SmartPickingClient initialized.");
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!_isConnected) {
                    tryToConnect();
                } else {
                    if (vision.getTriggerRequest()) {
                        sendAndReceive();
                        // Wait until PLC turns off the trigger to avoid double sending
                        while (vision.getTriggerRequest()) {
                             Thread.sleep(100);
                        }
                    }
                }
                Thread.sleep(100); // Prevent CPU overload
            } catch (Exception e) {
                // If loop crashes, wait 1s and restart
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void tryToConnect() {
        try {
            if (_socket != null && !_socket.isClosed()) return;

            _socket = new Socket(SERVER_IP, PORT);
            _socket.setSoTimeout(1000); 
            
            _out = _socket.getOutputStream();
            _in = _socket.getInputStream();
            
            _isConnected = true;
            log.info("CONNECTED to Smart Picking Server.");

        } catch (Exception e) {
            _isConnected = false;
        }
    }

    private void sendAndReceive() {
        try {
            String payload = "15;BIEMH26_105055\r\n";
            
            // COMPATIBILITY FIX: Use "US-ASCII" string literal
            _out.write(payload.getBytes("US-ASCII"));
            _out.flush();

            Thread.sleep(100);

            byte[] buffer = new byte[256];
            int bytesRead = _in.read(buffer);

            if (bytesRead > 0) {
                // COMPATIBILITY FIX: Use "US-ASCII" string literal
                String response = new String(buffer, 0, bytesRead, "US-ASCII");
                log.info("SERVER RESPONSE: " + response);
            } else {
                log.info("No data received.");
            }

        } catch (Exception e) {
            log.error("Comms Error: " + e.getMessage());
            _isConnected = false;
            try { _socket.close(); } catch (Exception ignored) {}
        }
    }
    
    @Override
    public void dispose() {
        try { if (_socket != null) _socket.close(); } catch (Exception e) {}
        super.dispose();
    }
}