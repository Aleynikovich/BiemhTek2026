package biemhTekniker.programs;

import biemhTekniker.config.ConfigManager;
import biemhTekniker.data.WorkpieceData;
import biemhTekniker.data.WorkpieceQueue;
import biemhTekniker.logger.Logger;
import biemhTekniker.vision.SmartPickingProtocol;
import biemhTekniker.vision.SmartPickingProtocol.Command;
import biemhTekniker.vision.SmartPickingProtocol.VisionResult;
import com.kuka.common.ThreadUtil;

import java.util.List;

/**
 * Full scan sequence vision task.
 * Program 109: Composite scan that populates the full WorkpieceQueue.
 * Sequence: 101 (Auto Mode) -> 2 (Capture) -> 3 (Locate Container) -> 4 (Locate Parts for all refs) -> 9/11 loop
 */
public class ScanPickedWorkpiece implements VisionTask
{
    private static final Logger log = Logger.getLogger(ScanPickedWorkpiece.class);
    private static final int DELAY_MS = 200;
    int reference;
    public void execute(VisionContext context) throws Exception
    {
        log.info("Starting workpiece scan sequence...");

        SmartPickingProtocol protocol = context.getProtocol();
        WorkpieceQueue queue = context.getWorkpieceQueue();
        ConfigManager config = ConfigManager.getInstance();
        WorkpieceData workpieceData = queue.getPickedWorkpiece();
        int zone = config.getInt("vision.zone", 1);

        // Step 1: Set AUTO mode (101)
        log.debug("Step 1: Setting AUTO mode");
        if (!protocol.setMode(Command.SET_AUTO_MODE))
        {
            log.error("Failed to set AUTO mode");
            throw new Exception("Failed to set AUTO mode");
        }
        ThreadUtil.milliSleep(DELAY_MS);

        // Step 2: Capture data (2)
        log.debug("Step 2: Capturing data");
        VisionResult captureResult = protocol.execute(Command.CAPTURE_DATA, true);
        if (!captureResult.isSuccess())
        {
            log.error("Failed to capture data");
            throw new Exception("Failed to capture data");
        }
        ThreadUtil.milliSleep(DELAY_MS);

        // Step 3: Send picked workpiece data ref
        // Initialize with a default failure or null to handle cases outside 1-3
        VisionResult workpieceDataSendResult = null;

        switch (reference)
        {
            case 1:
                log.debug("Sending WP 53");
                workpieceDataSendResult = protocol.execute(Command.SEND_WORKPIECE_SCAN_REQUEST_53, true);
                break;

            case 2:
                log.debug("Sending WP 55");
                workpieceDataSendResult = protocol.execute(Command.SEND_WORKPIECE_SCAN_REQUEST_55, true);
                break;

            case 3:
                log.debug("Sending WP 60");
                workpieceDataSendResult = protocol.execute(Command.SEND_WORKPIECE_SCAN_REQUEST_60, true);
                break;

            default:
                log.error("Unknown reference index: " + reference);
                throw new Exception("Invalid workpiece reference: " + reference);
        }

        // Add a safety check for null before checking success
        if (workpieceDataSendResult == null || !workpieceDataSendResult.isSuccess())
        {
            log.error("Failed to capture data for reference: " + reference);
            throw new Exception("Failed to capture data");
        }

        ThreadUtil.milliSleep(DELAY_MS);

        VisionResult workpieceOrientationResult = protocol.execute(Command.REQUEST_WORKPIECE_ORIENTATION, true);;
        String workpieceReferenceWithOrientation = String.valueOf(workpieceOrientationResult.getWorkpieceRefWithOrientation());
        log.info("Workpiece reference and orientation orientation: " + workpieceReferenceWithOrientation);
    }
}
