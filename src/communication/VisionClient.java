package communication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import common.ILogger;

/**
 * Background TCP client task for vision system communication.
 * Connects to vision system and continuously reads incoming frames in the background.
 * Parses incoming strings into VisionFrame objects without blocking robot motion.
 * 
 * Java 1.7 compatible - no lambdas, no diamond operators
 * Based on legacy BinPicking_TCPClient pattern
 */
public class VisionClient implements Runnable {
    
    private Socket clientSocket;
    private BufferedReader reader;
    private DataOutputStream writer;
    private ILogger logger;
    
    private String serverIp;
    private int serverPort;
    private String delimiter;
    
    private AtomicBoolean connected;
    private AtomicBoolean running;
    private AtomicBoolean dataAvailable;
    
    private VisionFrame latestFrame;
    private String latestRawData;
    
    private Thread backgroundThread;
    
    public VisionClient(ILogger logger, String serverIp, int serverPort, String delimiter) {
        this.logger = logger;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.delimiter = delimiter;
        
        this.connected = new AtomicBoolean(false);
        this.running = new AtomicBoolean(false);
        this.dataAvailable = new AtomicBoolean(false);
        
        this.latestFrame = new VisionFrame();
    }
    
    /**
     * Connect to vision system and start background reading task
     * @throws IOException if connection fails
     */
    public void connect() throws IOException {
        if (connected.get()) {
            logger.warn("VisionClient already connected");
            return;
        }
        
        clientSocket = new Socket(serverIp, serverPort);
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        writer = new DataOutputStream(clientSocket.getOutputStream());
        
        connected.set(true);
        running.set(true);
        
        // Start background reading thread
        backgroundThread = new Thread(this);
        backgroundThread.setDaemon(true);
        backgroundThread.setName("VisionClient-" + serverIp + ":" + serverPort);
        backgroundThread.start();
        
        logger.info("VisionClient connected to " + serverIp + ":" + serverPort);
    }
    
    /**
     * Disconnect from vision system
     */
    public void disconnect() {
        running.set(false);
        connected.set(false);
        
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            logger.warn("Error closing connection: " + e.getMessage());
        }
        
        logger.info("VisionClient disconnected");
    }
    
    /**
     * Send data to vision system
     * @param data String data to send
     * @throws IOException if send fails
     */
    public void sendData(String data) throws IOException {
        if (!connected.get()) {
            throw new IOException("Not connected to vision system");
        }
        
        writer.writeBytes(data + "\n");
        writer.flush();
        logger.info("Sent to vision: " + data);
    }
    
    /**
     * Send integer data to vision system
     * @param value Integer value to send
     * @throws IOException if send fails
     */
    public void sendDataInt(int value) throws IOException {
        sendData(String.valueOf(value));
    }
    
    /**
     * Background thread that continuously reads from vision system
     */
    public void run() {
        while (running.get()) {
            try {
                if (reader.ready()) {
                    String line = reader.readLine();
                    
                    if (line != null && !line.isEmpty()) {
                        // Store raw data
                        latestRawData = line;
                        
                        // Parse into VisionFrame
                        VisionFrame frame = new VisionFrame();
                        if (frame.parseFromString(line, delimiter)) {
                            latestFrame = frame;
                            dataAvailable.set(true);
                            logger.info("Vision data received: " + frame.toString());
                        } else {
                            logger.warn("Failed to parse vision data: " + line);
                        }
                    }
                }
                
                // Small sleep to prevent CPU spinning
                Thread.sleep(10);
                
            } catch (IOException e) {
                if (running.get()) {
                    logger.error("Vision read error: " + e.getMessage());
                    connected.set(false);
                    running.set(false);
                }
            } catch (InterruptedException e) {
                // Thread interrupted, exit gracefully
                break;
            }
        }
    }
    
    /**
     * Get latest parsed vision frame (non-blocking)
     * @return Latest VisionFrame or null if none available
     */
    public VisionFrame getLatestFrame() {
        if (dataAvailable.get()) {
            return latestFrame;
        }
        return null;
    }
    
    /**
     * Get latest raw data string (non-blocking)
     * @return Latest raw data or null
     */
    public String getLatestRawData() {
        return latestRawData;
    }
    
    /**
     * Check if new data is available and clear the flag
     * @return true if new data available
     */
    public boolean isDataAvailable() {
        return dataAvailable.getAndSet(false);
    }
    
    /**
     * Wait for data to be received (blocking with timeout)
     * @param timeoutMs Timeout in milliseconds
     * @return true if data received, false if timeout
     */
    public boolean waitForData(long timeoutMs) {
        long startTime = System.currentTimeMillis();
        
        while (!dataAvailable.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return false;
            }
            
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                return false;
            }
            
            if (!connected.get()) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean isConnected() {
        return connected.get();
    }
    
    public boolean isRunning() {
        return running.get();
    }
}
