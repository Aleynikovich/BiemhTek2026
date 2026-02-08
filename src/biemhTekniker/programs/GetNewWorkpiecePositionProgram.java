package biemhTekniker.programs;

import biemhTekniker.data.WorkpieceData;
import biemhTekniker.data.WorkpieceQueue;
import biemhTekniker.logger.Logger;
import biemhTekniker.vision.SmartPickingProtocol;
import biemhTekniker.vision.SmartPickingProtocol.Command;
import com.kuka.common.ThreadUtil;

/**
 * Vision task to get the position of a new workpiece from the vision system.
 * This is the legacy 5-step sequence maintained for backward compatibility.
 * Sends sequence: 101, 2, 3, 4, 9 to camera.
 * 
 * NOTE: New code should use FullScanTask (Program 109) instead, which handles
 * the complete scan sequence and queue management automatically.
 */
public class GetNewWorkpiecePositionProgram implements VisionTask
{

    private static final Logger log      = Logger.getLogger(GetNewWorkpiecePositionProgram.class);
    private static final int    DELAY_MS = 200;

    /**
     * Executes the legacy 5-step sequence to get new workpiece position from camera.
     * Sequence: SET_AUTO_MODE(101) -> CAPTURE_DATA(2) -> LOCATE_CONTAINER(3) ->
     * LOCATE_PARTS(4) -> GET_PART_POS(9)
     * 
     * Results are added to the workpiece queue.
     */
    public void execute(VisionContext context) throws Exception
    {
        log.info("Getting new workpiece position from camera (legacy mode)...");

        // Get dependencies from context
        SmartPickingProtocol protocol = context.getProtocol();
        WorkpieceQueue queue = context.getWorkpieceQueue();

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
        SmartPickingProtocol.VisionResult captureResult = protocol.execute(Command.CAPTURE_DATA, true);
        if (!captureResult.isSuccess())
        {
            log.error("Failed to capture data");
            throw new Exception("Failed to capture data");
        }
        ThreadUtil.milliSleep(DELAY_MS);

        // Step 3: Locate container (3)
        log.debug("Step 3: Locating container");
        SmartPickingProtocol.VisionResult containerResult = protocol.execute(Command.LOCATE_CONTAINER, true);
        if (!containerResult.isSuccess())
        {
            log.error("Failed to locate container");
            throw new Exception("Failed to locate container");
        }
        ThreadUtil.milliSleep(DELAY_MS);

        // Step 4: Locate parts (4)
        log.debug("Step 4: Locating parts");
        SmartPickingProtocol.VisionResult partsResult = protocol.execute(Command.LOCATE_PARTS, true);
        if (!partsResult.isSuccess())
        {
            log.error("Failed to locate parts");
            throw new Exception("Failed to locate parts");
        }
        ThreadUtil.milliSleep(DELAY_MS);

        // Step 5: Get part position (9)
        log.debug("Step 5: Getting part position");
        SmartPickingProtocol.VisionResult posResult = protocol.execute(Command.GET_PART_POS, true);
        if (!posResult.isSuccess())
        {
            log.error("Failed to get part position");
            throw new Exception("Failed to get part position");
        }

        // Parse and store workpiece position
        // Response format: "0,0,-601.5,109.2,1193.7,-170.9,2.6,124.9,0,2"
        // Indices: [0]=success, [1]=?, [2]=X, [3]=Y, [4]=Z, [5]=Rx, [6]=Ry, [7]=Rz, [8]=?, [9]=score
        double x     = posResult.getX();
        double y     = posResult.getY();
        double z     = posResult.getZ();
        double rx    = posResult.getRx();
        double ry    = posResult.getRy();
        double rz    = posResult.getRz();
        double score = posResult.getScore();

        // Create workpiece data and add to queue
        WorkpieceData workpiece = new WorkpieceData();
        workpiece.set(x, y, z, rx, ry, rz, score);
        queue.addWorkpiece(workpiece);

        log.info("Workpiece position retrieved and added to queue: " + workpiece);
    }
}
