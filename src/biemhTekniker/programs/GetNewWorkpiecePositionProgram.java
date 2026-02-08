package biemhTekniker.programs;

import biemhTekniker.data.WorkpieceData;
import biemhTekniker.logger.Logger;
import biemhTekniker.vision.SmartPickingProtocol;
import biemhTekniker.vision.SmartPickingProtocol.Command;
import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;

/**
 * Program to get the position of a new workpiece from the vision system.
 * Sends sequence: 101, 2, 3, 4, 9 to camera.
 * Camera replies with: "0,0,-601.5,109.2,1193.7,-170.9,2.6,124.9,0,2"
 * Where positions are at indices: [2]=X, [3]=Y, [4]=Z, [5]=Rx, [6]=Ry, [7]=Rz
 */
public class GetNewWorkpiecePositionProgram extends RoboticsAPIApplication
{

    private static final Logger log      = Logger.getLogger(GetNewWorkpiecePositionProgram.class);
    private static final int    DELAY_MS = 200;

    private SmartPickingProtocol protocol;
    private WorkpieceData        workpieceData;

    /**
     * Sets the dependencies for this program.
     * Called from Main before run() is invoked.
     *
     * @param protocol      SmartPicking protocol connected to vision server
     * @param workpieceData Shared workpiece data object to store results
     */
    public void setDependencies(SmartPickingProtocol protocol, WorkpieceData workpieceData)
    {
        this.protocol      = protocol;
        this.workpieceData = workpieceData;
    }

    /**
     * Executes the sequence to get new workpiece position from camera.
     * Sequence: SET_AUTO_MODE(101) -> CAPTURE_DATA(2) -> LOCATE_CONTAINER(3) ->
     * LOCATE_PARTS(4) -> GET_PART_POS(9)
     */
    @Override
    public void run() throws Exception
    {
        log.info("Getting new workpiece position from camera...");

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

        workpieceData.set(x, y, z, rx, ry, rz, score);

        log.info("Workpiece position retrieved: " + workpieceData);
    }
}
