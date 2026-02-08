package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.deviceModel.LBR;

/**
 * Program to place a measured workpiece back at its location.
 */
public class PlaceMeasuredWorkpieceProgram implements RobotProgram
{

    private static final Logger log = Logger.getLogger(PlaceMeasuredWorkpieceProgram.class);

    /**
     * Executes the place operation for a measured workpiece.
     */
    public void execute(RobotContext context) throws Exception
    {
        log.info("Placing measured workpiece...");

        // Get dependencies from context
        LBR robot = context.getRobot();

        // TODO: Implement robot motion to place measured workpiece
        // 1. Move to measured workpiece placement location
        // 2. Open gripper
        // 3. Move to safe position

        log.warn("PlaceMeasuredWorkpieceProgram: Motion not yet implemented");
    }
}
