package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import biemhTekniker.toolcontrol.ToolControl;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

/**
 * Program to pick a measured workpiece from a predefined location.
 */
public class PickMeasuredWorkpieceProgram
{

    private static final Logger log = Logger.getLogger(PickMeasuredWorkpieceProgram.class);

    private final RoboticsAPIApplication application;
    private final LBR                    robot;
    private final Tool                   gripper;
    private final ToolControl            toolControl;

    public PickMeasuredWorkpieceProgram(RoboticsAPIApplication application, LBR robot, Tool gripper, ToolControl toolControl)
    {
        this.application = application;
        this.robot       = robot;
        this.gripper     = gripper;
        this.toolControl = toolControl;
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
        // 2. Close gripper using toolControl.closeGripper1() or toolControl.closeGripper2()
        // 3. Move to safe position
        // Example:
        // gripper.move(ptp(application.getApplicationData().getFrame("/MeasuredPosition")));
        // toolControl.closeGripper1();
        // gripper.move(ptp(application.getApplicationData().getFrame("/BiemhHome")));

        log.warn("PickMeasuredWorkpieceProgram: Motion not yet implemented");
        return true;
    }
}
