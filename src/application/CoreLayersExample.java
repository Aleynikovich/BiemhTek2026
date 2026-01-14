package application;

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

import communication.LoggingServer;
import communication.VisionClient;
import communication.VisionFrame;
import common.ILogger;
import config.ConfigManager;
import hardware.GripperController;
import hardware.HeartbeatTask;
import hardware.MeasurementGripperController;

/**
 * Example application demonstrating the core architecture layers.
 * Shows integration of ConfigManager, VisionClient, LoggingServer,
 * HeartbeatTask, and GripperController.
 * 
 * Java 1.7 compatible - no lambdas, no diamond operators
 */
public class CoreLayersExample extends RoboticsAPIApplication {
    
    @Inject
    private LBR lbr;
    
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
    
    // Core components
    private ConfigManager config;
    private LoggingServer logServer;
    private VisionClient visionClient;
    private HeartbeatTask heartbeat;
    private GripperController grippers;
    private MeasurementGripperController mmGripper;
    
    // Logger adapter for core components
    private ILogger coreLogger;
    
    @Override
    public void initialize() {
        getLogger().info("Initializing Core Architecture Example...");
        
        // Create logger adapter
        coreLogger = new ILogger() {
            public void info(String message) {
                getLogger().info(message);
            }
            
            public void warn(String message) {
                getLogger().warn(message);
            }
            
            public void error(String message) {
                getLogger().error(message);
            }
        };
        
        // 1. Load Configuration
        config = ConfigManager.getInstance();
        config.setConfigBasePath("/home/KRC/configs/");
        try {
            config.loadRobotConfig();
            config.loadPlcConfig();
            getLogger().info("Configuration loaded successfully");
        } catch (IOException e) {
            getLogger().error("Failed to load configuration: " + e.getMessage());
            getLogger().info("Using default values");
        }
        
        // 2. Start Logging Server
        int logPort = config.getRobotPropertyInt("logging.port", 5001);
        logServer = new LoggingServer(coreLogger, logPort);
        try {
            logServer.start();
            getLogger().info("Logging server started on port " + logPort);
        } catch (IOException e) {
            getLogger().error("Failed to start logging server: " + e.getMessage());
        }
        
        // 3. Start Heartbeat
        int heartbeatInterval = config.getPlcPropertyInt("plc.heartbeat.interval", 100);
        heartbeat = new HeartbeatTask(coreLogger, robotStatusIO, heartbeatInterval);
        heartbeat.start();
        getLogger().info("Heartbeat started (ZRes1, " + heartbeatInterval + "ms)");
        
        // 4. Initialize Grippers
        grippers = new GripperController(gripper1IO, gripper2IO, coreLogger);
        getLogger().info("Gripper controller initialized");
        
        // 5. Initialize Measurement Machine Gripper Controller
        mmGripper = new MeasurementGripperController(plcRequestsIO, coreLogger);
        mmGripper.setListener(new MeasurementGripperController.PlcRequestListener() {
            public void onGripper1OpenRequest() {
                grippers.openGripper1();
            }
            
            public void onGripper1CloseRequest() {
                grippers.closeGripper1();
            }
            
            public void onGripper2OpenRequest() {
                grippers.openGripper2();
            }
            
            public void onGripper2CloseRequest() {
                grippers.closeGripper2();
            }
        });
        mmGripper.startMonitoring();
        getLogger().info("Measurement gripper controller monitoring PLC requests");
        
        // 6. Connect to Vision System
        String visionIp = config.getRobotProperty("vision.ip", "192.168.1.100");
        int visionPort = config.getRobotPropertyInt("vision.port", 5000);
        String delimiter = config.getRobotProperty("vision.delimiter", ",");
        
        visionClient = new VisionClient(coreLogger, visionIp, visionPort, delimiter);
        try {
            visionClient.connect();
            getLogger().info("Connected to vision system at " + visionIp + ":" + visionPort);
        } catch (IOException e) {
            getLogger().error("Failed to connect to vision system: " + e.getMessage());
        }
        
        getLogger().info("Core Architecture initialized successfully");
    }
    
    @Override
    public void run() {
        getLogger().info("Starting Core Layers Example Application...");
        
        // Broadcast to logging clients
        if (logServer != null && logServer.isRunning()) {
            logServer.broadcastLog("=== Core Layers Example Started ===");
        }
        
        // Move to home position
        getLogger().info("Moving to home position...");
        lbr.move(ptpHome());
        
        // Example: Test gripper operations
        getLogger().info("Testing Gripper 1...");
        grippers.openGripper1();
        if (grippers.waitForGripper1Open(3000)) {
            getLogger().info("Gripper 1 opened successfully");
            logServer.broadcastLog("Gripper 1: OPEN");
        } else {
            getLogger().warn("Gripper 1 open timeout");
        }
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        grippers.closeGripper1();
        if (grippers.waitForGripper1Close(3000)) {
            getLogger().info("Gripper 1 closed successfully");
            logServer.broadcastLog("Gripper 1: CLOSED");
        } else {
            getLogger().warn("Gripper 1 close timeout");
        }
        
        // Example: Send trigger to vision system
        if (visionClient != null && visionClient.isConnected()) {
            getLogger().info("Triggering vision system...");
            try {
                visionClient.sendData("TRIGGER");
                
                // Wait for vision response
                if (visionClient.waitForData(5000)) {
                    VisionFrame frame = visionClient.getLatestFrame();
                    if (frame != null && frame.isValid()) {
                        getLogger().info("Vision frame received: " + frame.toString());
                        logServer.broadcastLog("Vision: " + frame.toString());
                        
                        // Get base frame from application
                        Frame baseFrame = new Frame(getFrame("/BinPicking"));
                        Frame targetFrame = frame.toKukaFrame(baseFrame);
                        getLogger().info("Target frame: " + targetFrame.toString());
                    } else {
                        getLogger().warn("Invalid vision frame received");
                    }
                } else {
                    getLogger().warn("Vision data timeout");
                }
            } catch (IOException e) {
                getLogger().error("Vision communication error: " + e.getMessage());
            }
        }
        
        // Return to home
        getLogger().info("Returning to home position...");
        lbr.move(ptpHome());
        
        if (logServer != null && logServer.isRunning()) {
            logServer.broadcastLog("=== Core Layers Example Completed ===");
            getLogger().info("Logging server has " + logServer.getClientCount() + " connected clients");
        }
        
        getLogger().info("Example completed successfully");
    }
    
    @Override
    public void dispose() {
        getLogger().info("Shutting down Core Architecture...");
        
        // Stop all background tasks
        if (heartbeat != null) {
            heartbeat.stop();
            getLogger().info("Heartbeat stopped");
        }
        
        if (mmGripper != null) {
            mmGripper.stopMonitoring();
            getLogger().info("Measurement gripper monitoring stopped");
        }
        
        if (visionClient != null && visionClient.isConnected()) {
            visionClient.disconnect();
            getLogger().info("Vision client disconnected");
        }
        
        if (logServer != null) {
            logServer.broadcastLog("=== Robot shutting down ===");
            try {
                Thread.sleep(500); // Allow final message to be sent
            } catch (InterruptedException e) {
                // Ignore
            }
            logServer.stop();
            getLogger().info("Logging server stopped");
        }
        
        getLogger().info("Core Architecture shutdown complete");
        super.dispose();
    }
}
