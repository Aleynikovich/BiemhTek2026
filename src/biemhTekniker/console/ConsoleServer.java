package biemhTekniker.console;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Background task that manages the console command server.
 * Receives requests from external clients (GUI) and handles them via ConsoleServerInterface.
 */
public class ConsoleServer extends RoboticsAPICyclicBackgroundTask
{
    private static final Logger log = Logger.getLogger(ConsoleServer.class);
    private ServerSocket serverSocket;
    private final int PORT = 30001;
    private ConsoleServerInterface serverInterface;
    private List<ConsoleCommandHandler> handlers;
    
    public ConsoleServer(ConsoleServerInterface serverInterface) {
        this.serverInterface = serverInterface;
        this.handlers = new ArrayList<ConsoleCommandHandler>();
    }
    
    @Override
    public void initialize()
    {
        initializeCyclic(0, 1000, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
        try
        {
            serverSocket = new ServerSocket(PORT);
            serverSocket.setSoTimeout(100);
            log.info("Console server listening on port " + PORT);
        }
        catch (IOException e)
        {
            log.error("Failed to start console server: " + e.getMessage());
        }
    }

    @Override
    public void runCyclic()
    {
        if (serverSocket == null) return;
        
        try
        {
            Socket clientSocket = serverSocket.accept();
            log.info("New client connection accepted");
            
            // Create handler for this client
            ConsoleCommandHandler handler = new ConsoleCommandHandler(clientSocket, serverInterface);
            handlers.add(handler);
            
            // Start handler in new thread
            Thread handlerThread = new Thread(handler);
            handlerThread.setDaemon(true);
            handlerThread.start();
        }
        catch (java.net.SocketTimeoutException e)
        {
            // Normal timeout, just continue
        }
        catch (IOException e)
        {
            log.error("Error accepting client: " + e.getMessage());
        }
    }

    @Override
    public void dispose()
    {
        // Shutdown all handlers
        for (ConsoleCommandHandler handler : handlers) {
            handler.shutdown();
        }
        handlers.clear();
        
        // Close server socket
        if (serverSocket != null) {
            try
            {
                serverSocket.close();
                log.info("Console server closed on port " + PORT);
            } catch (IOException e)
            {
                log.error("Error closing console server: " + e.getMessage());
            }
        }
        super.dispose();
    }
}
