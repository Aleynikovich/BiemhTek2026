package biemhTekniker.logger;

import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Centralized logging server that broadcasts log messages to all connected network clients.
 * Runs as a cyclic background task and listens on port 30002.
 * Implements LogHandler to receive messages from the central Logger singleton.
 * <p>
 * Architecture:
 * - CentralLogger sends messages to this server via a queue
 * - Server broadcasts to all connected network clients (Python log clients, etc.)
 * - Main.java RobotConsoleClient connects as a client to receive logs for robot console output
 * <p>
 * This design allows:
 * - All tasks (background and foreground) to log centrally
 * - Robot console to display all logs (via Main's println)
 * - Multiple network clients to receive logs simultaneously
 */
public class LoggingServer extends RoboticsAPICyclicBackgroundTask implements LogHandler
{
    private static final int LOG_PORT = 30002;
    private static final int QUEUE_CAPACITY = 10000; // Large queue to prevent message loss
    private final Map<String, LogClientConnection> connectedClients = new ConcurrentHashMap<String, LogClientConnection>();
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<String>(QUEUE_CAPACITY);
    private ServerSocket serverSocket;
    private Thread listenerThread;
    private volatile boolean isRunning = false;
    private int clientCounter = 0;

    @Override
    public void initialize()
    {
        initializeCyclic(0, 10, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);

        // Register this server as a log handler so CentralLogger sends messages here
        CentralLogger.getInstance().addHandler(this);

        // Start server listener in a separate thread
        listenerThread = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    serverSocket = new ServerSocket(LOG_PORT);
                    isRunning = true;
                    // Log via CentralLogger so it goes through the central logging system
                    CentralLogger.getInstance().debug("LOG_SRV", "Started on port " + LOG_PORT);

                    while (isRunning && !Thread.currentThread().isInterrupted())
                    {
                        try
                        {
                            Socket clientSocket = serverSocket.accept();
                            String clientIp = clientSocket.getInetAddress().getHostAddress();
                            String clientId = "LogClient-" + (++clientCounter);

                            // Log via CentralLogger
                            CentralLogger.getInstance().debug("LOG_SRV", "New client connected: " + clientId + " (" + clientIp + ")");

                            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                            LogClientConnection connection = new LogClientConnection(clientSocket, writer, clientId);
                            connectedClients.put(clientId, connection);

                            // Send welcome message
                            writer.println("*** KUKA Robot Logging Server ***");
                        } catch (IOException e)
                        {
                            if (isRunning)
                            {
                                // Log via CentralLogger
                                CentralLogger.getInstance().error("LOG_SRV", "Error accepting client: " + e.getMessage());
                            }
                        }
                    }
                } catch (IOException e)
                {
                    // Log via CentralLogger
                    CentralLogger.getInstance().error("LOG_SRV", "Error starting server: " + e.getMessage());
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();

        // Log via CentralLogger
        CentralLogger.getInstance().debug("LOG_SRV", "Initialized successfully");
    }

    @Override
    public void runCyclic()
    {
        try
        {
            // Process messages from queue and broadcast to all clients
            // 10ms timeout provides good responsiveness for real-time robot logging
            // while keeping CPU usage acceptable (matches hartu reference implementation)
            String message = messageQueue.poll(10, TimeUnit.MILLISECONDS);
            if (message != null && !connectedClients.isEmpty())
            {
                broadcastToClients(message);
            }
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        } catch (Exception e)
        {
            // Catch any unexpected errors to prevent the cyclic task from stopping
            CentralLogger.getInstance().error("LOG_SRV", "Error in runCyclic: " + e.getMessage());
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     * Removes disconnected clients automatically.
     */
    private void broadcastToClients(String message)
    {
        // Collect clients to remove to avoid ConcurrentModificationException
        java.util.List<String> clientsToRemove = new java.util.ArrayList<String>();
        
        for (Map.Entry<String, LogClientConnection> entry : connectedClients.entrySet())
        {
            String clientId = entry.getKey();
            LogClientConnection connection = entry.getValue();

            if (connection.isConnected())
            {
                try
                {
                    connection.getWriter().println(message);
                    connection.getWriter().flush();

                    // Check if write failed
                    if (connection.getWriter().checkError())
                    {
                        clientsToRemove.add(clientId);
                    }
                } catch (Exception e)
                {
                    clientsToRemove.add(clientId);
                }
            } else
            {
                clientsToRemove.add(clientId);
            }
        }
        
        // Remove disconnected clients
        for (String clientId : clientsToRemove)
        {
            LogClientConnection connection = connectedClients.get(clientId);
            if (connection != null)
            {
                try
                {
                    connection.close();
                    connectedClients.remove(clientId);
                    CentralLogger.getInstance().debug("LOG_SRV", "Client disconnected: " + clientId);
                } catch (IOException e)
                {
                    CentralLogger.getInstance().error("LOG_SRV", "Error closing connection for " + clientId + ": " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();

        isRunning = false;

        // Remove this handler from CentralLogger
        CentralLogger.getInstance().removeHandler(this);

        // Close all client connections
        for (Map.Entry<String, LogClientConnection> entry : connectedClients.entrySet())
        {
            LogClientConnection connection = entry.getValue();
            try
            {
                connection.close();
            } catch (IOException e)
            {
                // Can't log during dispose as we've removed ourselves from CentralLogger
            }
        }
        connectedClients.clear();

        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed())
        {
            try
            {
                serverSocket.close();
            } catch (IOException e)
            {
                // Can't log during dispose
            }
        }

        // Wait for listener thread to finish
        if (listenerThread != null && listenerThread.isAlive())
        {
            try
            {
                listenerThread.join(2000);
            } catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    // LogHandler implementation

    @Override
    public void sendMessage(String formattedMessage)
    {
        // Add message to queue for broadcasting
        // Non-blocking - if queue is full, drop oldest messages
        boolean offered = messageQueue.offer(formattedMessage);
        if (!offered)
        {
            // Queue full - try to make space by removing oldest message
            // then attempt to add the new message. If this still fails, message is lost.
            messageQueue.poll();
            // Try again - if this fails, the message is dropped (better than blocking)
            messageQueue.offer(formattedMessage);
        }
    }

    @Override
    public boolean isActive()
    {
        return isRunning;
    }

    @Override
    public void close()
    {
        // Called when removed from CentralLogger
        isRunning = false;
    }

    /**
     * Inner class to represent a log client connection
     */
    private static class LogClientConnection
    {
        private final Socket socket;
        private final PrintWriter writer;

        public LogClientConnection(Socket socket, PrintWriter writer, String clientId)
        {
            this.socket = socket;
            this.writer = writer;
        }

        public PrintWriter getWriter()
        {
            return writer;
        }

        public boolean isConnected()
        {
            return socket != null && socket.isConnected() && !socket.isClosed();
        }

        public void close() throws IOException
        {
            if (writer != null)
            {
                writer.close();
            }
            if (socket != null && !socket.isClosed())
            {
                socket.close();
            }
        }
    }
}
