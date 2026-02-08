package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.deviceModel.LBR;

/**
 * Program to pick a measured workpiece from a predefined location.
 */
public class PickMeasuredWorkpieceProgram implements RobotProgram
{

    private static final Logger log = Logger.getLogger(PickMeasuredWorkpieceProgram.class);

    /**
     * Executes the pick operation for a measured workpiece.
     */
    public void execute(RobotContext context) throws Exception
    {
        log.info("Picking measured workpiece...");

        // Get dependencies from context
        LBR robot = context.getRobot();

        // TODO: Implement robot motion to pick measured workpiece
        // 1. Move to measured workpiece location
        // 2. Close gripper
        // 3. Move to safe position

        log.warn("PickMeasuredWorkpieceProgram: Motion not yet implemented");
    }
}
