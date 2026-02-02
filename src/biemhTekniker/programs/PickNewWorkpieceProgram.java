package biemhTekniker.programs;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

import javax.inject.Inject;
import javax.inject.Named;

import biemhTekniker.data.WorkpieceData;
import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;

/**
 * Program to pick a new workpiece using position from GetNewWorkpiecePositionProgram.
 */
public class PickNewWorkpieceProgram {
    
    private static final Logger log = Logger.getLogger(PickNewWorkpieceProgram.class);
    
    private final RoboticsAPIApplication application;
    private final LBR iiwa;
    private final WorkpieceData workpieceData;
    private Tool gripper;
    
    public PickNewWorkpieceProgram(RoboticsAPIApplication application, LBR robot, 
                                   WorkpieceData workpieceData,Tool gripper) {
        this.application = application;
        this.iiwa = robot;
        this.workpieceData = workpieceData;
	    this.gripper = gripper;    
    }
    
    /**
     * Executes the pick operation for a new workpiece.
     * @return true if pick succeeded, false otherwise
     */
    public boolean execute() {
        log.info("Picking new workpiece...");
        
        if (!workpieceData.isValid()) {
            log.error("Cannot pick workpiece - no valid position data available");
            return false;
        }
        
        log.info("Using workpiece position: " + workpieceData.toString());
        
        // TODO: Implement robot motion to pick workpiece
        // 1. Move to approach position above workpiece
        // 2. Move down to pick position using workpieceData coordinates
        // 3. Close gripper
        // 4. Move back to safe position
        ObjectFrame tcpA = gripper.getFrame("TCPA");
        
        
        tcpA.move(ptp(new Frame(workpieceData.getX(),
        		workpieceData.getY(),
        		workpieceData.getZ() + 200,
        		Math.toRadians(workpieceData.getRz()),
        		Math.toRadians(workpieceData.getRy()),
        		Math.toRadians(workpieceData.getRx()))).setJointVelocityRel(0.2));
        
        log.warn("PickNewWorkpieceProgram: Motion not yet implemented");
        return true;
    }
}
