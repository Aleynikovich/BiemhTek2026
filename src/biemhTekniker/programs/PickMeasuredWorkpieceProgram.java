package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;

import javax.inject.Inject;

/**
 * Program to pick a measured workpiece from a predefined location.
 */
public class PickMeasuredWorkpieceProgram extends RoboticsAPIApplication
{

    private static final Logger log = Logger.getLogger(PickMeasuredWorkpieceProgram.class);

    @Inject private LBR iiwa;

    /**
     * Executes the pick operation for a measured workpiece.
     */
    @Override public void run() throws Exception
    {
        log.info("Picking measured workpiece...");

        // TODO: Implement robot motion to pick measured workpiece
        // 1. Move to measured workpiece location
        // 2. Close gripper
        // 3. Move to safe position

        log.warn("PickMeasuredWorkpieceProgram: Motion not yet implemented");
    }
}
