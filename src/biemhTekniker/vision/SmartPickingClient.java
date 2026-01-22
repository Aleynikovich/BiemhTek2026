package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPIBackgroundTask; // Changed import
import com.kuka.generated.ioAccess.VisionIOGroup;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import javax.inject.Inject;

// CHANGED: Extend standard BackgroundTask, NOT Cyclic
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
        log.info("SmartPickingClient (Threaded) initialized.");
    }

    @Override
    public void run() {
        // We create our own loop here. This thread is now independent of the cycle time.
        while (true) {
            try {
                if (!_isConnected) {
                    tryToConnect();
                } else {
                    // Logic Loop
                    if (vision.getTriggerRequest()) {
                        sendAndReceive();
                        // Prevent spamming: Wait until trigger goes low or add delay
                        Thread.sleep(500); 
                    }
                }
                
                // Vital: Sleep to prevent 100% CPU usage
                Thread.sleep(100); 
                
            } catch (Exception e) {
                log.error("Error in background loop: " + e.getMessage());
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void tryToConnect() {
        try {
            if (_socket != null && !_socket.isClosed()) return;

            // log.info("Connecting..."); 
            _socket = new Socket(SERVER_IP, PORT);
            _socket.setSoTimeout(1000); 
            
            _out = _socket.getOutputStream();
            _in = _socket.getInputStream();
            
            _isConnected = true;
            log.info("CONNECTED to Smart Picking Server.");

        } catch (Exception e) {
            _isConnected = false;
            // Silent fail to avoid log spam, retry in main loop
        }
    }

    private void sendAndReceive() {
        try {
            String payload = "15;BIEMH26_105055\r\n";
            
            // 1. Send
            _out.write(payload.getBytes("US-ASCII"));
            _out.flush();

            // 2. Wait
            Thread.sleep(100);

            // 3. Read
            byte[] buffer = new byte[256];
            int bytesRead = _in.read(buffer);

            if (bytesRead > 0) {
                String response = new String(buffer, 0, bytesRead, "US-ASCII");
                log.info("SERVER RESPONSE: " + response);
                
                // OPTIONAL: Reset the trigger from Java side if needed
                // vision.setTriggerRequest(false);
            } else {
                log.info("Connected, but no data received.");
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