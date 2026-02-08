package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;

import javax.inject.Inject;

/**
 * Program to place a measured workpiece back at its location.
 */
public class PlaceMeasuredWorkpieceProgram extends RoboticsAPIApplication
{

    private static final Logger log = Logger.getLogger(PlaceMeasuredWorkpieceProgram.class);

    @Inject
    private LBR iiwa;

    /**
     * Executes the place operation for a measured workpiece.
     */
    @Override
    public void run() throws Exception
    {
        log.info("Placing measured workpiece...");

        // TODO: Implement robot motion to place measured workpiece
        // 1. Move to measured workpiece placement location
        // 2. Open gripper
        // 3. Move to safe position

        log.warn("PlaceMeasuredWorkpieceProgram: Motion not yet implemented");
    }
}
