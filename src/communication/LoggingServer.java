package communication;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import common.ILogger;



/**
 * Background task that runs a ServerSocket to stream robot logs to remote Telnet clients.
 * Implements a broadcast server where multiple clients can connect and receive log messages.
 * 
 * Java 1.7 compatible - no lambdas, no diamond operators
 * Uses background thread pattern from Sunrise OS
 */
public class LoggingServer implements Runnable {
    
    private ServerSocket serverSocket;
    private List<ClientConnection> clients;
    private AtomicBoolean running;
    private ILogger logger;
    private int port;
    
    /**
     * Represents a connected client
     */
    private class ClientConnection {
        Socket socket;
        PrintWriter writer;
        
        ClientConnection(Socket socket) throws IOException {
            this.socket = socket;
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        }
        
        void sendMessage(String message) {
            if (writer != null && !socket.isClosed()) {
                writer.println(message);
            }
        }
        
        void close() {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                // Ignore close errors
            }
        }
        
        boolean isConnected() {
            return socket != null && socket.isConnected() && !socket.isClosed();
        }
    }
    
    public LoggingServer(ILogger logger, int port) {
        this.logger = logger;
        this.port = port;
        this.clients = new ArrayList<ClientConnection>();
        this.running = new AtomicBoolean(false);
    }
    
    /**
     * Start the logging server
     * @throws IOException if server cannot be started
     */
    public void start() throws IOException {
        if (running.get()) {
            return;
        }
        
        serverSocket = new ServerSocket(port);
        running.set(true);
        
        Thread serverThread = new Thread(this);
        serverThread.setDaemon(true);
        serverThread.setName("LoggingServer-" + port);
        serverThread.start();
        
        logger.info("LoggingServer started on port " + port);
    }
    
    /**
     * Stop the logging server
     */
    public void stop() {
        running.set(false);
        
        // Close all client connections
        synchronized (clients) {
            for (ClientConnection client : clients) {
                client.close();
            }
            clients.clear();
        }
        
        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.warn("Error closing server socket: " + e.getMessage());
            }
        }
        
        logger.info("LoggingServer stopped");
    }
    
    /**
     * Broadcast a log message to all connected clients
     * @param message Log message to broadcast
     */
    public void broadcastLog(String message) {
        synchronized (clients) {
            // Remove disconnected clients
            List<ClientConnection> disconnected = new ArrayList<ClientConnection>();
            for (ClientConnection client : clients) {
                if (!client.isConnected()) {
                    disconnected.add(client);
                } else {
                    client.sendMessage(message);
                }
            }
            clients.removeAll(disconnected);
        }
    }
    
    /**
     * Background thread that accepts new client connections
     */
    public void run() {
        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientConnection client = new ClientConnection(clientSocket);
                
                synchronized (clients) {
                    clients.add(client);
                }
                
                logger.info("Logging client connected from " + clientSocket.getInetAddress());
                client.sendMessage("*** KUKA Robot Logging Server ***");
                
            } catch (IOException e) {
                if (running.get()) {
                    logger.warn("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Get number of connected clients
     * @return Number of active client connections
     */
    public int getClientCount() {
        synchronized (clients) {
            return clients.size();
        }
    }
    
    public boolean isRunning() {
        return running.get();
    }
}
