package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;

import com.kuka.generated.ioAccess.VisionIOGroup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;


public class SmartPickingClient extends RoboticsAPICyclicBackgroundTask {

    private static final Logger log = Logger.getLogger(SmartPickingClient.class);
    private static final String SERVER_IP = "172.31.1.69";
    private static final int PORT = 59002;

    private Socket _socket;
    private PrintWriter _writer;
    private BufferedReader _reader;
    private boolean _isConnected = false;

    private VisionIOGroup vision;

    @Override
    public void initialize()
    {
        initializeCyclic(0, 1000, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
        log.info("SmartPickingClient initialized. Target: " + SERVER_IP + ":" + PORT);
        tryToConnect();
    }

    @Override
    public void runCyclic()
    {
        if (!_isConnected)
        {
           // tryToConnect();
        } else
        {
            if (vision.getTriggerRequest())
            {


            }
        }
    }

    private void tryToConnect()
    {
        try
        {
            log.debug("Attempting to connect to Smart Picking Server...");
            _socket = new Socket(SERVER_IP,PORT);
            _writer = new PrintWriter(_socket.getOutputStream(), true);

            _writer.println("15;BIEMH26_105055"); //Send ref
   


        } catch (Exception e)
        {
            log.debug("Connection failed: " + e.getMessage());
        }
    }

    @Override
    public void dispose() {
        try {
            if (_socket != null) _socket.close();
        } catch (Exception e) {
            log.error("Error closing vision socket: " + e.getMessage());
        }
        super.dispose();
    }
}