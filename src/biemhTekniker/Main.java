package biemhTekniker;

import biemhTekniker.IO.GripperController;
import biemhTekniker.IO.HmiButtonHandler;
import biemhTekniker.IO.MeasurementGripperController;
import biemhTekniker.IO.MeasurementGripperController.PlcRequestListener;
import biemhTekniker.logger.CentralLogger;
import biemhTekniker.logger.LoggingServer;
import biemhTekniker.vision.VisionClient;
import com.kuka.generated.ioAccess.Gripper1IOGroup;
import com.kuka.generated.ioAccess.Gripper2IOGroup;
import com.kuka.generated.ioAccess.PlcRequestsGrippersIOGroup;
import com.kuka.generated.ioAccess.RobotStatusIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.uiModel.userKeys.IUserKeyBar;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptpHome;

/**
 * Main robot application demonstrating core architecture layers.
 * Follows KUKA Sunrise OS best practices for HMI button handling and background task management.
 * <p>
 * Logging Architecture:
 * - Only this foreground task can write to robot console via println
 * - Background tasks use CentralLogger singleton
 * - LoggingServer broadcasts logs to network clients
 * - RobotConsoleClient (inner class) connects to LoggingServer and prints to console
 */
@SuppressWarnings("unused")
public class Main extends RoboticsAPIApplication
{
    @Inject
    private LBR iiwa;
    @Inject
    private Controller controller;
    @Inject
    private RobotStatusIOGroup robotStatusIO;
    @Inject
    private Gripper1IOGroup gripper1IO;
    @Inject
    private Gripper2IOGroup gripper2IO;
    @Inject
    private PlcRequestsGrippersIOGroup plcRequestsIO;
    @Inject
    private LoggingServer logServer;
    @Inject
    private VisionClient visionClient;
    @Inject
    private MeasurementGripperController mmGripperController;

    private ConfigManager config;
    private GripperController gripperController;
    private HmiButtonHandler hmiButtonHandler;
    private IUserKeyBar hmiKeyBar;
    private RobotConsoleClient consoleClient;
    private Thread consoleClientThread;

    @Override
    public void initialize()
    {
        // Start robot console client first to capture all logs
        startRobotConsoleClient();

        CentralLogger.getInstance().info("MAIN", "Initializing core architecture...");

        config = ConfigManager.getInstance();
        config.setConfigBasePath("C:/KRC/Projects/BiemhTek2026/configs/");

        try
        {
            config.loadRobotConfig();
            config.loadPlcConfig();
            CentralLogger.getInstance().info("MAIN", "Configuration loaded");
        } catch (IOException e)
        {
            CentralLogger.getInstance().warn("MAIN", "Config files not found, using defaults: " + e.getMessage());
        }

        // Initialize gripper controller with IO dependencies
        gripperController = new GripperController(gripper1IO, gripper2IO);
        CentralLogger.getInstance().info("MAIN", "Gripper controller initialized");

        // Initialize HMI buttons using correct Sunrise API
        initializeHmiButtons();

        // Set up PLC request listener for MeasurementGripperController
        // Note: mmGripperController is injected and will be auto-started by framework
        mmGripperController.setListener(new PlcRequestListener()
        {
            public void onGripper1OpenRequest()
            {
                gripperController.openGripper1();
            }

            public void onGripper1CloseRequest()
            {
                gripperController.closeGripper1();
            }

            public void onGripper2OpenRequest()
            {
                gripperController.openGripper2();
            }

            public void onGripper2CloseRequest()
            {
                gripperController.closeGripper2();
            }
        });

        // Configure and connect vision client
        // Note: visionClient is injected and will be auto-started by framework
        String visionIp = config.getRobotProperty("vision.ip", "172.31.1.69");
        int visionPort = config.getRobotPropertyInt("vision.port", 59002);
        String delimiter = config.getRobotProperty("vision.delimiter", ",");
        visionClient.setConnectionParams(visionIp, visionPort, delimiter);
        try
        {
            visionClient.connect();
            CentralLogger.getInstance().info("MAIN", "Vision client connected");
        } catch (IOException e)
        {
            CentralLogger.getInstance().error("MAIN", "Failed to connect to vision: " + e.getMessage());
        }

        CentralLogger.getInstance().info("MAIN", "Core architecture initialized");
    }

    /**
     * Starts the robot console client that connects to LoggingServer
     * and broadcasts all logs to robot console via println.
     * Only foreground tasks (like Main) can use println to write to robot console.
     */
    private void startRobotConsoleClient()
    {
        consoleClient = new RobotConsoleClient();
        consoleClientThread = new Thread(consoleClient);
        consoleClientThread.setDaemon(true);
        consoleClientThread.start();
    }

    /**
     * Initializes the HMI programmable buttons on the SmartPad.
     * Uses the correct Sunrise API: getApplicationUI().createUserKeyBar()
     */
    private void initializeHmiButtons()
    {
        try
        {
            CentralLogger.getInstance().info("MAIN", "Initializing HMI programmable buttons...");

            // Create user key bar using correct Sunrise API
            this.hmiKeyBar = getApplicationUI().createUserKeyBar("BiemhTek_HMI");

            // Create button handler and register keys
            this.hmiButtonHandler = new HmiButtonHandler(gripperController);
            this.hmiButtonHandler.registerUserKeys(this.hmiKeyBar);

            CentralLogger.getInstance().info("MAIN", "HMI programmable buttons initialized successfully");
        } catch (Exception e)
        {
            CentralLogger.getInstance().error("MAIN", "Failed to initialize HMI buttons: " + e.getMessage());
        }
    }

    @Override
    public void run()
    {
        iiwa.move(ptpHome());
        CentralLogger.getInstance().info("MAIN", "Main application ready");
    }

    @Override
    public void dispose()
    {
        CentralLogger.getInstance().info("MAIN", "Disposing core architecture...");

        if (visionClient != null)
        {
            visionClient.disconnect();
        }

        // Stop robot console client
        if (consoleClient != null)
        {
            consoleClient.stop();
        }
        if (consoleClientThread != null && consoleClientThread.isAlive())
        {
            try
            {
                // Interrupt the thread to break out of blocking operations
                consoleClientThread.interrupt();
                // Wait briefly for graceful shutdown
                consoleClientThread.join(2000);
            } catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }

        super.dispose();
    }

    /**
     * Inner class that connects to LoggingServer as a client
     * and broadcasts received log messages to robot console via println.
     * <p>
     * This is necessary because only foreground tasks (like Main)
     * can use println to write to the robot's SmartPad console.
     * Background tasks cannot directly write to console.
     */
    private static class RobotConsoleClient implements Runnable
    {
        private static final String LOG_SERVER_HOST = "localhost";
        private static final int LOG_SERVER_PORT = 30002;
        private static final int RECONNECT_DELAY_MS = 5000;

        private volatile boolean running = true;
        private Socket socket;
        private BufferedReader reader;

        @Override
        public void run()
        {
            System.out.println("[RobotConsoleClient] Starting console client to receive logs from LoggingServer...");

            while (running)
            {
                try
                {
                    // Connect to logging server
                    socket = new Socket(LOG_SERVER_HOST, LOG_SERVER_PORT);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    System.out.println("[RobotConsoleClient] Connected to LoggingServer on port " + LOG_SERVER_PORT);

                    // Read and broadcast log messages
                    String line;
                    while (running && (line = reader.readLine()) != null)
                    {
                        // Broadcast to robot console using println
                        // Only foreground tasks can do this
                        System.out.println(line);
                    }

                } catch (IOException e)
                {
                    if (running)
                    {
                        System.out.println("[RobotConsoleClient] Connection error: " + e.getMessage());
                        System.out.println("[RobotConsoleClient] Will retry in " + RECONNECT_DELAY_MS + "ms...");

                        // Wait before reconnecting
                        try
                        {
                            Thread.sleep(RECONNECT_DELAY_MS);
                        } catch (InterruptedException ie)
                        {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                } finally
                {
                    // Clean up connection
                    closeConnection();
                }
            }

            System.out.println("[RobotConsoleClient] Console client stopped.");
        }

        public void stop()
        {
            running = false;
            closeConnection();
        }

        private void closeConnection()
        {
            // Close socket first to interrupt blocking read
            try
            {
                if (socket != null && !socket.isClosed())
                {
                    socket.close();
                }
            } catch (IOException e)
            {
                // Ignore
            }

            // Then close reader (this should be non-blocking now)
            try
            {
                if (reader != null)
                {
                    reader.close();
                }
            } catch (IOException e)
            {
                // Ignore
            }
        }
    }
}