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
public class FullScanTask implements VisionTask
{
    private static final Logger log = Logger.getLogger(FullScanTask.class);
    private static final int DELAY_MS = 200;

    public void execute(VisionContext context) throws Exception
    {
        log.info("Starting full scan sequence...");

        SmartPickingProtocol protocol = context.getProtocol();
        WorkpieceQueue queue = context.getWorkpieceQueue();
        ConfigManager config = ConfigManager.getInstance();
        WorkpieceData workpieceData = queue.takeNextForPicking();

        int referenceCount = config.getInt("vision.reference.count", 3);
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

        // Step 3: Locate container (3)
        log.debug("Step 3: Locating container");
        VisionResult containerResult = protocol.execute(Command.LOCATE_CONTAINER, true);
        if (!containerResult.isSuccess())
        {
            log.error("Failed to locate container");
            throw new Exception("Failed to locate container");
        }
        ThreadUtil.milliSleep(DELAY_MS);

        // Step 4: Locate all parts across all references
        log.debug("Step 4: Locating parts across " + referenceCount + " references in zone " + zone);
        List<WorkpieceData> foundWorkpieces = protocol.locateAllParts(referenceCount, zone);

        // Step 5: Add or update workpieces in the queue (with position tracking)
        // This prevents creating duplicate workpieces on each scan
        // Note: O(n²) complexity - consider spatial indexing if workpiece count exceeds ~50
        int addedCount = 0;
        int updatedCount = 0;
        for (int i = 0; i < foundWorkpieces.size(); i++)
        {
            WorkpieceData wp = foundWorkpieces.get(i);
            WorkpieceData existing = queue.findAtPosition(wp.getX(), wp.getY(), wp.getZ(), wp.getReferenceIndex());

            if (existing != null && (existing.getState() == WorkpieceState.RETURNED || existing.getState() == WorkpieceState.AVAILABLE))
            {
                // Update existing workpiece
                existing.set(wp.getX(), wp.getY(), wp.getZ(), wp.getRx(), wp.getRy(), wp.getRz(), wp.getScore());
                existing.setState(WorkpieceState.AVAILABLE);
                updatedCount++;
            } else if (existing == null)
            {
                // Add new workpiece
                queue.addWorkpiece(wp);
                addedCount++;
            }
            // If existing workpiece is in use (PICKED, MEASURING, MEASURED), skip it
        }

        log.info("Full scan complete: Added " + addedCount + " new, updated " + updatedCount + " existing workpieces");
        log.info("Queue status: " + queue.getAvailableCount() + " available, " + queue.getTotalCount() + " total");
    }
}
