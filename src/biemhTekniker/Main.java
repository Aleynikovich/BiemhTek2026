package biemhTekniker;

import biemhTekniker.IO.GripperController;
import biemhTekniker.IO.HmiButtonHandler;
import biemhTekniker.IO.MeasurementGripperController;
import biemhTekniker.IO.MeasurementGripperController.PlcRequestListener;
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
import java.io.IOException;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptpHome;

/**
 * Main robot application demonstrating core architecture layers.
 * Follows KUKA Sunrise OS best practices for HMI button handling and background task management.
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

    @Override
    public void initialize()
    {
        getLogger().info("Initializing core architecture...");

        config = ConfigManager.getInstance();
        config.setConfigBasePath("/home/KRC/configs/");

        try
        {
            config.loadRobotConfig();
            config.loadPlcConfig();
            getLogger().info("Configuration loaded");
        } catch (IOException e)
        {
            getLogger().warn("Config files not found, using defaults: " + e.getMessage());
        }

        // Initialize gripper controller with IO dependencies
        gripperController = new GripperController(gripper1IO, gripper2IO);
        getLogger().info("Gripper controller initialized");

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

        // Configure and start logging server
        // Note: logServer is injected and will be auto-started by framework
        int logPort = config.getRobotPropertyInt("logging.port", 9000);
        logServer.setPort(logPort);
        try
        {
            logServer.startServer();
        } catch (IOException e)
        {
            getLogger().error("Failed to start logging server: " + e.getMessage());
        }

        // Configure and connect vision client
        // Note: visionClient is injected and will be auto-started by framework
        String visionIp = config.getRobotProperty("vision.ip", "192.168.1.100");
        int visionPort = config.getRobotPropertyInt("vision.port", 5000);
        String delimiter = config.getRobotProperty("vision.delimiter", ",");
        visionClient.setConnectionParams(visionIp, visionPort, delimiter);
        try
        {
            visionClient.connect();
            getLogger().info("Vision client connected");
        } catch (IOException e)
        {
            getLogger().error("Failed to connect to vision: " + e.getMessage());
        }

        getLogger().info("Core architecture initialized");
    }

    /**
     * Initializes the HMI programmable buttons on the SmartPad.
     * Uses the correct Sunrise API: getApplicationUI().createUserKeyBar()
     */
    private void initializeHmiButtons()
    {
        try
        {
            getLogger().info("Initializing HMI programmable buttons...");

            // Create user key bar using correct Sunrise API
            this.hmiKeyBar = getApplicationUI().createUserKeyBar("BiemhTek_HMI");

            // Create button handler and register keys
            this.hmiButtonHandler = new HmiButtonHandler(gripperController);
            this.hmiButtonHandler.registerUserKeys(this.hmiKeyBar);

            getLogger().info("HMI programmable buttons initialized successfully");
        } catch (Exception e)
        {
            getLogger().error("Failed to initialize HMI buttons: " + e.getMessage());
        }
    }

    @Override
    public void run()
    {
        iiwa.move(ptpHome());
        getLogger().info("Main application ready");
    }

    @Override
    public void dispose()
    {
        getLogger().info("Disposing core architecture...");

        if (visionClient != null)
        {
            visionClient.disconnect();
        }
        if (logServer != null)
        {
            logServer.stopServer();
        }

        super.dispose();
    }
}