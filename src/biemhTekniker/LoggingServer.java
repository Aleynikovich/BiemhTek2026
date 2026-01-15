package biemhTekniker;

import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Cyclic background task that runs a ServerSocket to stream robot logs to remote Telnet clients.
 * Follows KUKA best practices for cyclic background task design.
 * Socket listener runs in a daemon thread; message broadcasting happens in runCyclic.
 */
public class LoggingServer extends RoboticsAPICyclicBackgroundTask
{

    private ServerSocket serverSocket;
    private List<ClientConnection> clients;
    private AtomicBoolean running;
    private int port;
    private Thread listenerThread;

    public void setPort(int port)
    {
        this.port = port;
    }

    @Override
    public void initialize()
    {
        this.clients = new ArrayList<ClientConnection>();
        this.running = new AtomicBoolean(false);

        // Initialize cyclic behavior: run every 1 second for periodic cleanup
        initializeCyclic(0, 1, TimeUnit.SECONDS, CycleBehavior.BestEffort);

        getLogger().info("LoggingServer initialized");
    }

    public void startServer() throws IOException
    {
        if (running.get())
        {
            return;
        }

        serverSocket = new ServerSocket(port);
        running.set(true);

        // Start listener thread in daemon mode
        listenerThread = new Thread(new Runnable()
        {
            public void run()
            {
                acceptClientConnections();
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();

        getLogger().info("LoggingServer started on port " + port);
    }

    /**
     * Accepts client connections in a background daemon thread.
     * This method runs continuously until the server is stopped.
     */
    private void acceptClientConnections()
    {
        getLogger().info("LoggingServer listener thread started");

        while (running.get() && !Thread.currentThread().isInterrupted())
        {
            try
            {
                Socket clientSocket = serverSocket.accept();
                ClientConnection client = new ClientConnection(clientSocket);

                synchronized (clients)
                {
                    clients.add(client);
                }

                getLogger().info("Logging client connected from " + clientSocket.getInetAddress());
                client.sendMessage("*** KUKA Robot Logging Server ***");

            } catch (IOException e)
            {
                if (running.get())
                {
                    getLogger().warn("Error accepting client connection: " + e.getMessage());
                }
            }
        }

        getLogger().info("LoggingServer listener thread stopped");
    }

    @Override
    public void runCyclic()
    {
        // Periodic cleanup of disconnected clients
        synchronized (clients)
        {
            List<ClientConnection> disconnected = new ArrayList<ClientConnection>();
            for (ClientConnection client : clients)
            {
                if (!client.isConnected())
                {
                    disconnected.add(client);
                }
            }
            for (ClientConnection client : disconnected)
            {
                client.close();
                clients.remove(client);
            }
        }
    }

    public void stopServer()
    {
        running.set(false);

        synchronized (clients)
        {
            for (ClientConnection client : clients)
            {
                client.close();
            }
            clients.clear();
        }

        if (serverSocket != null && !serverSocket.isClosed())
        {
            try
            {
                serverSocket.close();
            } catch (IOException e)
            {
                getLogger().warn("Error closing server socket: " + e.getMessage());
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

        getLogger().info("LoggingServer stopped");
    }

    public void broadcastLog(String message)
    {
        synchronized (clients)
        {
            for (ClientConnection client : clients)
            {
                if (client.isConnected())
                {
                    client.sendMessage(message);
                }
            }
        }
    }

    public int getClientCount()
    {
        synchronized (clients)
        {
            return clients.size();
        }
    }

    public boolean isRunning()
    {
        return running.get();
    }

    @Override
    public void dispose()
    {
        stopServer();
        super.dispose();
    }

    private class ClientConnection
    {
        Socket socket;
        PrintWriter writer;

        ClientConnection(Socket socket) throws IOException
        {
            this.socket = socket;
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        }

        void sendMessage(String message)
        {
            if (writer != null && !socket.isClosed())
            {
                writer.println(message);
            }
        }

        void close()
        {
            try
            {
                if (writer != null)
                {
                    writer.close();
                }
                if (socket != null && !socket.isClosed())
                {
                    socket.close();
                }
            } catch (IOException e)
            {
                // Ignore close errors
            }
        }

        boolean isConnected()
        {
            return socket != null && socket.isConnected() && !socket.isClosed();
        }
    }
}
