package biemhTekniker.console;


import biemhTekniker.logger.LogManager;
import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Background task that manages the console command server.
 * Receives requests from external clients and handles them locally
**/
public class ConsoleServer extends RoboticsAPICyclicBackgroundTask
{
    private static final Logger log = Logger.getLogger(ConsoleServer.class);
    private ServerSocket serverSocket;
    private final int PORT = 30001;
    @Override
    public void initialize()
    {
        initializeCyclic(0, 1000, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
        try
        {
            serverSocket = new ServerSocket(PORT);
            serverSocket.setSoTimeout(10);
            log.info("Server listening on port " + PORT);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void runCyclic()
    {
        try
        {

            Socket ConsoleClient = serverSocket.accept();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose()
    {
        try
        {
            serverSocket.close();
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        super.dispose();
    }
}

