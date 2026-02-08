package biemhTekniker.tasks;

import biemhTekniker.data.WorkpieceData;
import biemhTekniker.logger.Logger;
import biemhTekniker.model.ProgramDescriptor;
import biemhTekniker.registry.HttpClient;
import biemhTekniker.registry.SimpleJSON;
import biemhTekniker.vision.SmartPickingProtocol;
import com.kuka.common.ThreadUtil;

/**
 * Vision task that gets a new workpiece position from the camera system.
 * This task mirrors the logic of GetNewWorkpiecePositionProgram but:
 * 1. Does NOT perform any robot motion
 * 2. Posts the workpiece position to the config service
 * 3. Can be executed asynchronously
 * 
 * Java 7 compatible.
 */
public class GetNewWorkpiecePositionTask extends VisionTask {
    
    private static final Logger log = Logger.getLogger(GetNewWorkpiecePositionTask.class);
    private static final int DELAY_MS = 200;
    
    private final SmartPickingProtocol protocol;
    private final WorkpieceData workpieceData;
    private final String configServiceBaseUrl;
    
    /**
     * Create a new GetNewWorkpiecePositionTask.
     * 
     * @param descriptor Program descriptor
     * @param protocol SmartPicking protocol connected to vision server
     * @param workpieceData Shared workpiece data object to store results
     * @param configServiceBaseUrl Base URL of config service
     */
    public GetNewWorkpiecePositionTask(ProgramDescriptor descriptor,
                                       SmartPickingProtocol protocol,
                                       WorkpieceData workpieceData,
                                       String configServiceBaseUrl) {
        super(descriptor);
        this.protocol = protocol;
        this.workpieceData = workpieceData;
        this.configServiceBaseUrl = configServiceBaseUrl;
    }
    
    @Override
    public TaskResult execute() {
        log.info("Executing GetNewWorkpiecePosition vision task (async)");
        
        try {
            // Step 1: Set AUTO mode (101)
            log.debug("Step 1: Setting AUTO mode");
            if (!protocol.setMode(SmartPickingProtocol.Command.SET_AUTO_MODE)) {
                return TaskResult.failure("Failed to set AUTO mode");
            }
            ThreadUtil.milliSleep(DELAY_MS);
            
            // Step 2: Capture data (2)
            log.debug("Step 2: Capturing data");
            SmartPickingProtocol.VisionResult captureResult = protocol.execute(
                    SmartPickingProtocol.Command.CAPTURE_DATA, true);
            if (!captureResult.isSuccess()) {
                return TaskResult.failure("Failed to capture data");
            }
            ThreadUtil.milliSleep(DELAY_MS);
            
            // Step 3: Locate container (3)
            log.debug("Step 3: Locating container");
            SmartPickingProtocol.VisionResult containerResult = protocol.execute(
                    SmartPickingProtocol.Command.LOCATE_CONTAINER, true);
            if (!containerResult.isSuccess()) {
                return TaskResult.failure("Failed to locate container");
            }
            ThreadUtil.milliSleep(DELAY_MS);
            
            // Step 4: Locate parts (4)
            log.debug("Step 4: Locating parts");
            SmartPickingProtocol.VisionResult partsResult = protocol.execute(
                    SmartPickingProtocol.Command.LOCATE_PARTS, true);
            if (!partsResult.isSuccess()) {
                return TaskResult.failure("Failed to locate parts");
            }
            ThreadUtil.milliSleep(DELAY_MS);
            
            // Step 5: Get part position (9)
            log.debug("Step 5: Getting part position");
            SmartPickingProtocol.VisionResult posResult = protocol.execute(
                    SmartPickingProtocol.Command.GET_PART_POS, true);
            if (!posResult.isSuccess()) {
                return TaskResult.failure("Failed to get part position");
            }
            
            // Extract workpiece position
            double x = posResult.getX();
            double y = posResult.getY();
            double z = posResult.getZ();
            double rx = posResult.getRx();
            double ry = posResult.getRy();
            double rz = posResult.getRz();
            double score = posResult.getScore();
            
            // Update shared workpiece data
            workpieceData.set(x, y, z, rx, ry, rz, score);
            log.info("Workpiece position retrieved: " + workpieceData);
            
            // Post to config service
            if (configServiceBaseUrl != null && !configServiceBaseUrl.isEmpty()) {
                postWorkpieceToConfigService(x, y, z, rx, ry, rz, score);
            } else {
                log.warn("Config service URL not configured, skipping POST");
            }
            
            return TaskResult.success("Workpiece position retrieved successfully");
            
        } catch (Exception e) {
            log.error("GetNewWorkpiecePosition task failed: " + e.getMessage());
            return TaskResult.failure("Task execution failed", e);
        }
    }
    
    /**
     * Post workpiece position to config service.
     */
    private void postWorkpieceToConfigService(double x, double y, double z,
                                              double rx, double ry, double rz,
                                              double score) {
        try {
            String url = configServiceBaseUrl + "/api/workpieces";
            String json = SimpleJSON.buildWorkpieceJSON(x, y, z, rx, ry, rz, score, "GetNewWorkpiecePosition");
            
            log.debug("Posting workpiece position to config service: " + url);
            String response = HttpClient.post(url, json);
            
            if (response != null) {
                log.info("Successfully posted workpiece position to config service");
            } else {
                log.warn("Failed to post workpiece position to config service");
            }
        } catch (Exception e) {
            log.error("Error posting to config service: " + e.getMessage());
            // Don't fail the task if posting fails - this is optional
        }
    }
}
