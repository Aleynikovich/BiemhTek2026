package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;

/**
 * Program to pick a measured workpiece from a predefined location.
 */
public class PickMeasuredWorkpieceProgram
{

    private static final Logger log = Logger.getLogger(PickMeasuredWorkpieceProgram.class);

    private final RoboticsAPIApplication application;
    private final LBR                    robot;

    public PickMeasuredWorkpieceProgram(RoboticsAPIApplication application, LBR robot)
    {
        this.application = application;
        this.robot       = robot;
    }

    /**
     * Executes the pick operation for a measured workpiece.
     *
     * @return true if pick succeeded, false otherwise
     */
    public boolean execute()
    {
        log.info("Picking measured workpiece...");

        // TODO: Implement robot motion to pick measured workpiece
        // 1. Move to measured workpiece location
        // 2. Close gripper
        // 3. Move to safe position

        log.warn("PickMeasuredWorkpieceProgram: Motion not yet implemented");
        return true;
    }
}
