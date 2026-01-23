package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPIBackgroundTask;
import com.kuka.generated.ioAccess.VisionIOGroup;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import javax.inject.Inject;

/**
 * Threaded Background Task - Mimics BinPicking_EKI logic
 * Compatible with Java 1.6 / 1.7 (Sunrise Workbench)
 */
public class SmartPickingClient extends RoboticsAPIBackgroundTask {

    private static final Logger log = Logger.getLogger(SmartPickingClient.class);
    
    // SERVER CONFIG
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
        // Main Loop (Replaces the Cyclic behavior)
        while (true) {
            try {
                // 1. Connection Management
                if (!_isConnected || _socket == null || _socket.isClosed()) {
                    tryToConnect();
                } 
                else {
                    // 2. Logic: Only send if PLC asks for it
                    if (vision.getTriggerRequest()) {
                        
                        log.info("Trigger received. Sending data...");
                        boolean success = performTransaction("15;BIEMH26_105055");
                        
                        if(success) {
                            // Wait for PLC to turn OFF trigger to avoid double sending
                            // Simulates the flow in BinPicking_EKI
                            while(vision.getTriggerRequest()) {
                                Thread.sleep(100);
                            }
                        }
                    }
                }
                
                // CRITICAL: Prevent CPU 100% usage
                Thread.sleep(100); 

            } catch (Exception e) {
                log.error("Error in Main Loop: " + e.getMessage());
                _isConnected = false;
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void tryToConnect() {
        try {
            if (_socket != null) { try { _socket.close(); } catch(Exception e){} }

            // log.info("Connecting to " + SERVER_IP + "...");
            _socket = new Socket(SERVER_IP, PORT);
            
            // TIMEOUT is crucial so read() doesn't hang forever if server dies
            _socket.setSoTimeout(20000); 
            
            _out = _socket.getOutputStream();
            _in = _socket.getInputStream();
            
            _isConnected = true;
            log.info("CONNECTED to Smart Picking Server.");

        } catch (Exception e) {
            _isConnected = false;
            // Silent fail to keep log clean until connected
        }
    }

    /**
     * Sends data and waits for response, mimicking get_message() from legacy code
     */
    private boolean performTransaction(String message) {
        try {
            // --- STEP 1: SEND ---
            // Append \r\n manually (Hercules style)
            String payload = message;// + "\r\n"; 
            
            // Java 1.6 safe way to get bytes
            byte[] data = payload.getBytes("US-ASCII"); 
            
            _out.write(data);
            _out.flush();

            // --- STEP 2: WAIT (Polling) ---
            // We give the server a moment to process
            Thread.sleep(5000);

            // --- STEP 3: READ ---
            byte[] buffer = new byte[1024];
            
            // This reads WHATEVER is in the buffer (doesn't wait for newline)
            int bytesRead = _in.read(buffer);

            if (bytesRead > 0) {
                // Java 1.6 safe String creation
                String response = new String(buffer, 0, bytesRead, "US-ASCII");
                log.info("SERVER RESPONSE: " + response);
                return true;
            } else {
                log.info("Server received data but sent empty response.");
                return false;
            }

        } catch (Exception e) {
            log.error("Transaction Failed: " + e.getMessage());
            _isConnected = false; // Force reconnect on next loop
            return false;
        }
    }
    
    @Override
    public void dispose() {
        try { if (_socket != null) _socket.close(); } catch (Exception e) {}
        super.dispose();
    }
}