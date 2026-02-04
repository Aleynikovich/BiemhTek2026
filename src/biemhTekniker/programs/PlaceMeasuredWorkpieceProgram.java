package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import biemhTekniker.toolcontrol.ToolControl;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

/**
 * Program to place a measured workpiece back at its location.
 */
public class PlaceMeasuredWorkpieceProgram
{

    private static final Logger log = Logger.getLogger(PlaceMeasuredWorkpieceProgram.class);

    private final RoboticsAPIApplication application;
    private final LBR                    robot;
    private final Tool                   gripper;
    private final ToolControl            toolControl;

    public PlaceMeasuredWorkpieceProgram(RoboticsAPIApplication application, LBR robot, Tool gripper, ToolControl toolControl)
    {
        this.application = application;
        this.robot       = robot;
        this.gripper     = gripper;
        this.toolControl = toolControl;
    }

    /**
     * Executes the place operation for a measured workpiece.
     *
     * @return true if place succeeded, false otherwise
     */
    public boolean execute()
    {
        log.info("Placing measured workpiece...");

        // TODO: Implement robot motion to place measured workpiece
        // 1. Move to measured workpiece placement location
        // 2. Open gripper using toolControl.openGripper1() or toolControl.openGripper2()
        // 3. Move to safe position
        // Example:
        // gripper.move(ptp(application.getApplicationData().getFrame("/MeasuredPlacePosition")));
        // toolControl.openGripper1();
        // gripper.move(ptp(application.getApplicationData().getFrame("/BiemhHome")));

        log.warn("PlaceMeasuredWorkpieceProgram: Motion not yet implemented");
        return true;
    }
}
