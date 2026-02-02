package biemhTekniker.programs;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.lin;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

import javax.inject.Inject;
import javax.inject.Named;

import biemhTekniker.data.WorkpieceData;
import biemhTekniker.logger.Logger;

import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.geometricModel.math.Transformation;

/**
 * Program to pick a new workpiece using position from GetNewWorkpiecePositionProgram.
 */
public class PickNewWorkpieceProgram {
    
    private static final Logger log = Logger.getLogger(PickNewWorkpieceProgram.class);
    
    private final RoboticsAPIApplication application;
    private final LBR iiwa;
    private final WorkpieceData workpieceData;
    private Tool gripper;
	private MediaFlangeIOGroup gripperIO;
    
    public PickNewWorkpieceProgram
    (	
		RoboticsAPIApplication application,
		LBR robot, 
		WorkpieceData workpieceData,
		Tool gripper,
	    MediaFlangeIOGroup gripperIO
	) 
    {
        this.application = application;
        this.iiwa = robot;
        this.workpieceData = workpieceData;
	    this.gripper = gripper;  
	    this.gripperIO = gripperIO;
    }
    
    /**
     * Executes the pick operation for a new workpiece.
     * @return true if pick succeeded, false otherwise
     */
    public boolean execute()
    {
        log.info("Picking new workpiece...");
        
        if (!workpieceData.isValid()) 
        {
            log.error("Cannot pick workpiece - no valid position data available");
            return false;
        }
        
        log.debug("Using workpiece position: " + workpieceData.toString());
        
        ObjectFrame tcp = gripper.getFrame("TCPB");
        Frame pickPosition = workpieceData.getWorkPiecePickFrame();
        //Frame prePickPosition = workpieceData.getWorkPiecePickFrame();
        //prePickPosition.setZ(prePickPosition.getZ() + 100);

        tcp.move(ptp(pickPosition.transform(Transformation.ofDeg(0, 0, 100, 0, 0, 0))).setJointVelocityRel(0.5));
        tcp.move(lin(pickPosition).setJointVelocityRel(0.25));
        ThreadUtil.milliSleep(500);
        tcp.move(lin(pickPosition.transform(Transformation.ofDeg(0, 0, 20, 0, 0, 0))).setJointVelocityRel(0.5));
        tcp.move(lin(pickPosition.transform(Transformation.ofDeg(0, 0, 20, 15, 0, 0))).setJointVelocityRel(0.5));
        tcp.move(lin(pickPosition.transform(Transformation.ofDeg(0, 0, 1, 15, 0, 0))).setJointVelocityRel(0.5));
    	gripper.move(ptp(application.getApplicationData().getFrame("/BiemhHome")));
        
        return true;
    }
}
