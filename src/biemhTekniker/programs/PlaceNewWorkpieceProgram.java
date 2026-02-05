package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;

/**
 * Program to place a new workpiece at a predefined location.
 */
public class PlaceNewWorkpieceProgram
{

    private static final Logger log = Logger.getLogger(PlaceNewWorkpieceProgram.class);

    private final RoboticsAPIApplication application;
    private final LBR                    robot;
    private final Tool                   gripper;

    public PlaceNewWorkpieceProgram(RoboticsAPIApplication application, LBR robot)
    {
        this.application = application;
        this.robot       = robot;
    }

    /**
     * Executes the place operation for a new workpiece.
     *
     * @return true if place succeeded, false otherwise
     */
    public boolean execute()
    {
        log.info("Placing new workpiece...");

        ObjectFrame tcpA = gripper.getFrame("TCPA");

        log.warn("PlaceNewWorkpieceProgram: Motion not yet implemented");
        return true;
    }
}
