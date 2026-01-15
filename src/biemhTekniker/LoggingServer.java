package biemhTekniker;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPIBackgroundTask;

/**
 * Background task that runs a ServerSocket to stream robot logs to remote Telnet clients.
 */
public class LoggingServer extends RoboticsAPIBackgroundTask {
    
    private ServerSocket serverSocket;
    private List<ClientConnection> clients;
    private AtomicBoolean running;
    private int port;
    
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
    
    public void setPort(int port) {
        this.port = port;
    }
    
    @Override
    public void initialize() {
        this.clients = new ArrayList<ClientConnection>();
        this.running = new AtomicBoolean(false);
        getLogger().info("LoggingServer initialized");
    }
    
    public void startServer() throws IOException {
        if (running.get()) {
            return;
        }
        
        serverSocket = new ServerSocket(port);
        running.set(true);
        getLogger().info("LoggingServer started on port " + port);
    }
    
    public void stopServer() {
        running.set(false);
        
        synchronized (clients) {
            for (ClientConnection client : clients) {
                client.close();
            }
            clients.clear();
        }
        
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                getLogger().warn("Error closing server socket: " + e.getMessage());
            }
        }
        
        getLogger().info("LoggingServer stopped");
    }
    
    public void broadcastLog(String message) {
        synchronized (clients) {
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
    
    @Override
    public void run() {
        getLogger().info("LoggingServer background task started");
        
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientConnection client = new ClientConnection(clientSocket);
                
                synchronized (clients) {
                    clients.add(client);
                }
                
                getLogger().info("Logging client connected from " + clientSocket.getInetAddress());
                client.sendMessage("*** KUKA Robot Logging Server ***");
                
            } catch (IOException e) {
                if (running.get()) {
                    getLogger().warn("Error accepting client connection: " + e.getMessage());
                }
            }
        }
        
        getLogger().info("LoggingServer background task stopped");
    }
    
    public int getClientCount() {
        synchronized (clients) {
            return clients.size();
        }
    }
    
    public boolean isRunning() {
        return running.get();
    }
    
    @Override
    public void dispose() {
        stopServer();
        super.dispose();
    }
}
