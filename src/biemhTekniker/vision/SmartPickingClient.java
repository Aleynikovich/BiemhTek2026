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
            _socket = new Socket();
            _socket.connect(new InetSocketAddress(SERVER_IP, PORT), 500);

            _writer = new PrintWriter(_socket.getOutputStream(), true);
            _reader = new BufferedReader(new InputStreamReader(_socket.getInputStream()));

            _isConnected = true;
            log.info("Successfully connected to Smart Picking Server.");

            _writer.println("15;BIEMH26_105055"+ "\r\n"); //Send ref
            
            _writer.flush();
            Thread.sleep(10000);
            String response = _reader.readLine();

            if ("0".equals(response))
            {
                _isConnected = true;
                log.info("Connected and reference loaded successfully.");
            }
            else
            {
                throw new Exception("Invalid server response: " + response);
            }

            _writer.println("101"); //Send auto mode
            response = _reader.readLine();

            if ("0".equals(response))
            {
                log.info("Smart Picking in automatic mode.");
            }
            else
            {
                throw new Exception("Invalid server response: " + response);
            }


        } catch (Exception e)
        {
            _isConnected = false;

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