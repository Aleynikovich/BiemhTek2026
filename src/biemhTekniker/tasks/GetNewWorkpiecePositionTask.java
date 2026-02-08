package biemhTekniker.tasks;

import biemhTekniker.data.WorkpieceData;
import biemhTekniker.logger.Logger;
import biemhTekniker.util.SimpleHttpClient;
import biemhTekniker.util.SimpleJson;
import biemhTekniker.vision.SmartPickingProtocol;
import com.kuka.common.ThreadUtil;

/**
 * VisionTask implementation that gets new workpiece position from vision system.
 * This is a refactored version of GetNewWorkpiecePositionProgram that:
 * 1. Runs asynchronously (does not block robot motion)
 * 2. Does NOT perform any robot motions
 * 3. Posts workpiece positions to the config service
 */
public class GetNewWorkpiecePositionTask extends VisionTask
{
    private static final Logger log      = Logger.getLogger(GetNewWorkpiecePositionTask.class);
    private static final int    DELAY_MS = 200;

    private final SmartPickingProtocol protocol;
    private final WorkpieceData        workpieceData;
    private final String               configServiceUrl;

    public GetNewWorkpiecePositionTask(SmartPickingProtocol protocol,
                                       WorkpieceData workpieceData,
                                       String configServiceUrl)
    {
        super(1, "GetNewWorkpiecePosition");
        this.protocol          = protocol;
        this.workpieceData     = workpieceData;
        this.configServiceUrl  = configServiceUrl;
    }

    @Override public TaskResult execute() throws Exception
    {
        log.info("Getting new workpiece position from camera (async)...");

        try
        {
            // Step 1: Set AUTO mode (101)
            log.debug("Step 1: Setting AUTO mode");
            if (!protocol.setMode(SmartPickingProtocol.Command.SET_AUTO_MODE))
            {
                log.error("Failed to set AUTO mode");
                return TaskResult.failure("Failed to set AUTO mode");
            }
            ThreadUtil.milliSleep(DELAY_MS);

            // Step 2: Capture data (2)
            log.debug("Step 2: Capturing data");
            SmartPickingProtocol.VisionResult captureResult = protocol.execute(SmartPickingProtocol.Command.CAPTURE_DATA, true);
            if (!captureResult.isSuccess())
            {
                log.error("Failed to capture data");
                return TaskResult.failure("Failed to capture data");
            }
            ThreadUtil.milliSleep(DELAY_MS);

            // Step 3: Locate container (3)
            log.debug("Step 3: Locating container");
            SmartPickingProtocol.VisionResult containerResult = protocol.execute(SmartPickingProtocol.Command.LOCATE_CONTAINER, true);
            if (!containerResult.isSuccess())
            {
                log.error("Failed to locate container");
                return TaskResult.failure("Failed to locate container");
            }
            ThreadUtil.milliSleep(DELAY_MS);

            // Step 4: Locate parts (4)
            log.debug("Step 4: Locating parts");
            SmartPickingProtocol.VisionResult partsResult = protocol.execute(SmartPickingProtocol.Command.LOCATE_PARTS, true);
            if (!partsResult.isSuccess())
            {
                log.error("Failed to locate parts");
                return TaskResult.failure("Failed to locate parts");
            }
            ThreadUtil.milliSleep(DELAY_MS);

            // Step 5: Get part position (9)
            log.debug("Step 5: Getting part position");
            SmartPickingProtocol.VisionResult posResult = protocol.execute(SmartPickingProtocol.Command.GET_PART_POS, true);
            if (!posResult.isSuccess())
            {
                log.error("Failed to get part position");
                return TaskResult.failure("Failed to get part position");
            }

            // Parse workpiece position
            double x     = posResult.getX();
            double y     = posResult.getY();
            double z     = posResult.getZ();
            double rx    = posResult.getRx();
            double ry    = posResult.getRy();
            double rz    = posResult.getRz();
            double score = posResult.getScore();

            // Update shared workpiece data
            workpieceData.set(x, y, z, rx, ry, rz, score);
            log.info("Workpiece position retrieved: " + workpieceData);

            // Post workpiece position to config service
            postWorkpieceToConfigService(x, y, z, rx, ry, rz, score);

            return TaskResult.success("Workpiece position retrieved successfully");
        }
        catch (Exception e)
        {
            log.error("GetNewWorkpiecePosition task failed: " + e.getMessage());
            return TaskResult.failure("Task execution failed: " + e.getMessage());
        }
    }

    /**
     * Post workpiece position to config service via REST API.
     */
    private void postWorkpieceToConfigService(double x, double y, double z,
                                              double rx, double ry, double rz,
                                              double score)
    {
        if (configServiceUrl == null || configServiceUrl.isEmpty())
        {
            log.warn("Config service URL not configured, skipping workpiece POST");
            return;
        }

        try
        {
            String url      = configServiceUrl + "/api/workpieces";
            String jsonBody = SimpleJson.buildWorkpieceJson(x, y, z, rx, ry, rz, score, "GetNewWorkpiecePosition");

            log.debug("Posting workpiece to config service: " + url);
            String response = SimpleHttpClient.post(url, jsonBody);

            if (response != null)
            {
                log.info("Workpiece position posted to config service successfully");
            }
            else
            {
                log.warn("Failed to post workpiece position to config service");
            }
        }
        catch (Exception e)
        {
            log.error("Error posting workpiece to config service: " + e.getMessage());
        }
    }
}
