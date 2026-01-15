package biemhTekniker;

import java.io.IOException;

import javax.inject.Inject;

import com.kuka.generated.ioAccess.Gripper1IOGroup;
import com.kuka.generated.ioAccess.Gripper2IOGroup;
import com.kuka.generated.ioAccess.PlcRequestsGrippersIOGroup;
import com.kuka.generated.ioAccess.RobotStatusIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Frame;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;

import biemhTekniker.MeasurementGripperController.PlcRequestListener;

/**
 * Main robot application demonstrating core architecture layers.
 */
public class Main extends RoboticsAPIApplication {
	
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
	
	private ConfigManager config;
	private HeartbeatTask heartbeat;
	private LoggingServer logServer;
	private VisionClient visionClient;
	private GripperController gripperController;
	private MeasurementGripperController mmGripperController;

	@Override
	public void initialize() {
		getLogger().info("Initializing core architecture...");
		
		config = ConfigManager.getInstance();
		config.setConfigBasePath("/home/KRC/configs/");
		
		try {
			config.loadRobotConfig();
			config.loadPlcConfig();
			getLogger().info("Configuration loaded");
		} catch (IOException e) {
			getLogger().warn("Config files not found, using defaults: " + e.getMessage());
		}
		
		heartbeat = new HeartbeatTask();
		initAndStartTask(heartbeat);
		
		int logPort = config.getRobotPropertyInt("logging.port", 9000);
		logServer = new LoggingServer();
		logServer.setPort(logPort);
		initAndStartTask(logServer);
		try {
			logServer.startServer();
		} catch (IOException e) {
			getLogger().error("Failed to start logging server: " + e.getMessage());
		}
		
		gripperController = new GripperController();
		gripperController.initializeHMI();
		getLogger().info("Gripper HMI buttons created");
		
		mmGripperController = new MeasurementGripperController();
		mmGripperController.setListener(new PlcRequestListener() {
			public void onGripper1OpenRequest() {
				gripperController.openGripper1();
			}
			public void onGripper1CloseRequest() {
				gripperController.closeGripper1();
			}
			public void onGripper2OpenRequest() {
				gripperController.openGripper2();
			}
			public void onGripper2CloseRequest() {
				gripperController.closeGripper2();
			}
		});
		initAndStartTask(mmGripperController);
		
		String visionIp = config.getRobotProperty("vision.ip", "192.168.1.100");
		int visionPort = config.getRobotPropertyInt("vision.port", 5000);
		String delimiter = config.getRobotProperty("vision.delimiter", ",");
		
		visionClient = new VisionClient();
		visionClient.setConnectionParams(visionIp, visionPort, delimiter);
		initAndStartTask(visionClient);
		try {
			visionClient.connect();
			getLogger().info("Vision client connected");
		} catch (IOException e) {
			getLogger().error("Failed to connect to vision: " + e.getMessage());
		}
		
		getLogger().info("Core architecture initialized");
	}

	@Override
	public void run() {
		iiwa.move(ptpHome());
		getLogger().info("Main application ready");
	}
	
	@Override
	public void dispose() {
		getLogger().info("Disposing core architecture...");
		
		if (visionClient != null) {
			visionClient.disconnect();
		}
		if (logServer != null) {
			logServer.stopServer();
		}
		
		super.dispose();
	}
}