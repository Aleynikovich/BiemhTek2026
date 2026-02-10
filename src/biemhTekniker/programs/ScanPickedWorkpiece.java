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
 * Vision task to scan a picked workpiece and determine its orientation.
 * Program 110: Scans the workpiece to determine if it's regular (0) or inverted (1).
 * Returns reference+orientation (53x, 55x, 60x where x=0 or 1).
 * Sequence: 101 (Auto Mode) -> 2 (Capture) -> Send scan request -> Get orientation
 */
public class ScanPickedWorkpiece implements VisionTask
{
    private static final Logger log = Logger.getLogger(ScanPickedWorkpiece.class);
    private static final int DELAY_MS = 200;

    public void execute(VisionContext context) throws Exception
    {
        log.info("Starting workpiece orientation scan...");

        SmartPickingProtocol protocol = context.getProtocol();
        WorkpieceQueue queue = context.getWorkpieceQueue();
        
        // Get the workpiece that was just picked
        WorkpieceData workpieceData = queue.getPickedWorkpiece();
        if (workpieceData == null)
        {
            log.error("No picked workpiece found in queue");
            throw new Exception("No picked workpiece to scan");
        }

        int reference = workpieceData.getReferenceIndex();
        log.info("Scanning workpiece with reference index: " + reference);

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

        // Step 3: Send workpiece scan request based on reference
        // Reference index maps to reference numbers: 1->53, 2->55, 3->60
        int[] referenceNumbers = {53, 55, 60};
        
        if (reference < 1 || reference > referenceNumbers.length)
        {
            log.error("Unknown reference index: " + reference + " (expected 1, 2, or 3)");
            throw new Exception("Invalid workpiece reference: " + reference);
        }
        
        int referenceNumber = referenceNumbers[reference - 1];
        log.debug("Sending workpiece scan request for reference " + referenceNumber);
        
        VisionResult workpieceDataSendResult = protocol.sendWorkpieceScanRequest(1, referenceNumber);

        // Check if scan request succeeded
        if (workpieceDataSendResult == null || !workpieceDataSendResult.isSuccess())
        {
            log.error("Failed to send scan request for reference: " + reference);
            throw new Exception("Failed to send scan request");
        }

        ThreadUtil.milliSleep(DELAY_MS);

        // Step 4: Request workpiece orientation
        log.debug("Step 4: Requesting workpiece orientation");
        VisionResult orientationResult = protocol.requestWorkpieceOrientation(1);
        
        if (!orientationResult.isSuccess())
        {
            log.error("Failed to get workpiece orientation");
            throw new Exception("Failed to get workpiece orientation");
        }

        // Get orientation result (e.g., 530, 531, 550, 551, 600, 601)
        double refWithOrientation = orientationResult.getWorkpieceRefWithOrientation();
        int refWithOrientationInt = (int) refWithOrientation;
        
        // Extract orientation: last digit (0=regular, 1=inverted)
        int orientation = refWithOrientationInt % 10;
        
        log.info("Workpiece reference with orientation: " + refWithOrientationInt + 
                 " (reference=" + reference + ", orientation=" + orientation + 
                 (orientation == 0 ? " [REGULAR]" : " [INVERTED]") + ")");

        // Store orientation in workpiece data
        workpieceData.setOrientation(orientation);
        
        log.info("Workpiece orientation scan completed successfully");
    }
}
