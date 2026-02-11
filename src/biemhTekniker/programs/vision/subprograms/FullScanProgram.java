package biemhTekniker.programs.vision.subprograms;

import biemhTekniker.lib.config.ConfigManager;
import biemhTekniker.lib.data.WorkpieceData;
import biemhTekniker.lib.data.WorkpieceQueue;
import biemhTekniker.lib.logger.Logger;
import biemhTekniker.lib.vision.SmartPickingProtocol;
import biemhTekniker.lib.vision.SmartPickingProtocol.Command;
import biemhTekniker.lib.vision.SmartPickingProtocol.VisionResult;
import biemhTekniker.lib.vision.VisionProgram;
import biemhTekniker.programs.vision.VisionContext;
import com.kuka.common.ThreadUtil;

/**
 * Full scan sequence vision task.
 * Program 109: Composite scan that populates the full WorkpieceQueue.
 * Sequence: 101 (Auto Mode) -> 2 (Capture) -> 3 (Locate Container) -> 4 (Locate Parts for all refs) -> 9/11 loop
 */
public class FullScanProgram implements VisionProgram
{
    private static final Logger log = Logger.getLogger(FullScanProgram.class);
    private static final int DELAY_MS = 200;

    public void execute(VisionContext context) throws Exception
    {
        log.info("Starting full scan sequence...");

        SmartPickingProtocol protocol = context.getProtocol();
        final WorkpieceQueue queue = context.getWorkpieceQueue();
        ConfigManager config = ConfigManager.getInstance();

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
        // Use callback to add workpieces to queue progressively as they are found
        log.debug("Step 4: Locating parts across " + referenceCount + " references in zone " + zone);
        
        // Create callback to add each workpiece to queue immediately as it's found
        SmartPickingProtocol.WorkpieceCallback callback = new SmartPickingProtocol.WorkpieceCallback()
        {
            @Override
            public void onWorkpieceFound(WorkpieceData wp)
            {
                // Add or update workpiece in the queue (with position tracking)
                // This prevents creating duplicate workpieces on each scan
                queue.addOrUpdateWorkpiece(wp.getX(), wp.getY(), wp.getZ(), wp.getRx(), wp.getRy(), wp.getRz(), wp.getScore(), wp.getReferenceIndex());
                log.debug("Workpiece added to queue progressively: id=" + wp.getId() + ", ref=" + wp.getReferenceIndex() + ", score=" + wp.getScore());
            }
        };
        
        int totalFound = protocol.locateAllParts(referenceCount, zone, callback);

        log.info("Full scan complete - found " + totalFound + " workpieces");
        log.info("Queue status: " + queue.getAvailableCount() + " available, " + queue.getTotalCount() + " total");
    }
}
