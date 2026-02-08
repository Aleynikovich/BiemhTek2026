package biemhTekniker.console;

import biemhTekniker.logger.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Console command server for GUI integration.
 * Runs in its own thread and accepts TCP connections from GUI clients.
 * Handles JSON commands to control the robot.
 */
public class ConsoleServer implements Runnable
{
    private static final Logger                      log     = Logger.getLogger(ConsoleServer.class);
    private final        int                         port;
    private final        ConsoleServerInterface      serverInterface;
    private final        List<ConsoleCommandHandler> handlers;
    private              ServerSocket                serverSocket;
    private volatile     boolean                     running = false;
    private              Thread                      serverThread;

    public ConsoleServer(ConsoleServerInterface serverInterface, int port)
    {
        this.serverInterface = serverInterface;
        this.port            = port;
        this.handlers        = new ArrayList<ConsoleCommandHandler>();
        log.info("ConsoleServer instance created");
    }

    /**
     * Initialize and start the console server.
     */
    public void initialize()
    {
        log.info("ConsoleServer initializing...");

        try
        {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000); // 1 second timeout for accept
            log.info("Console server socket bound to port " + port);

            // Start server thread
            running      = true;
            serverThread = new Thread(this, "ConsoleServerThread");
            serverThread.setDaemon(true);
            serverThread.start();

            log.info("Console server thread started and listening on port " + PORT);
        }
        catch (IOException e)
        {
            log.error("Failed to start console server on port " + port + ": " + e.getMessage(), e);
        }
    }

    @Override public void run()
    {
        log.info("ConsoleServer thread running");

        while (running)
        {
            try
            {
                // Accept client connections
                Socket clientSocket = serverSocket.accept();
                log.info("New client connection accepted from: " + clientSocket.getInetAddress());

                // Create handler for this client
                ConsoleCommandHandler handler = new ConsoleCommandHandler(clientSocket, serverInterface);
                synchronized (handlers)
                {
                    handlers.add(handler);
                }

                // Start handler in new thread
                Thread handlerThread = new Thread(handler, "ClientHandler-" + clientSocket.getInetAddress());
                handlerThread.setDaemon(true);
                handlerThread.start();

                log.info("Client handler thread started");
            }
            catch (java.net.SocketTimeoutException e)
            {
                // Normal timeout, just continue
            }
            catch (IOException e)
            {
                if (running)
                {
                    log.error("Error accepting client: " + e.getMessage());
                }
            }
            catch (Exception e)
            {
                log.error("Unexpected error in ConsoleServer: " + e.getMessage(), e);
            }
        }

        log.info("ConsoleServer thread stopped");
    }

    /**
     * Stop the console server and close all connections.
     */
    public void dispose()
    {
        log.info("ConsoleServer disposing...");
        running = false;

        // Shutdown all handlers
        synchronized (handlers)
        {
            for (ConsoleCommandHandler handler : handlers)
            {
                try
                {
                    handler.shutdown();
                }
                catch (Exception e)
                {
                    log.error("Error shutting down handler: " + e.getMessage());
                }
            }
            handlers.clear();
        }

        // Close server socket
        if (serverSocket != null)
        {
            try
            {
                serverSocket.close();
                log.info("Console server socket closed on port " + PORT);
            }
            catch (IOException e)
            {
                log.error("Error closing console server socket: " + e.getMessage());
            }
        }

        // Wait for server thread to finish
        if (serverThread != null)
        {
            try
            {
                serverThread.join(2000); // Wait up to 2 seconds
                log.info("ConsoleServer thread joined");
            }
            catch (InterruptedException e)
            {
                log.warn("Interrupted while waiting for server thread");
                Thread.currentThread().interrupt();
            }
        }

        log.info("ConsoleServer disposed");
    }

    /**
     * Check if the console server is running.
     *
     * @return true if running, false otherwise
     */
    public boolean isRunning()
    {
        return running && serverThread != null && serverThread.isAlive();
    }
}

