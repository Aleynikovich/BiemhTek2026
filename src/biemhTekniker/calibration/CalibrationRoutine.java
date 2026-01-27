package biemhTekniker.calibration;

import biemhTekniker.logger.Logger;
import biemhTekniker.vision.SmartPickingProtocol;
import biemhTekniker.vision.SmartPickingProtocol.Command;
import biemhTekniker.vision.SmartPickingProtocol.VisionResult;
import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.motionModel.LIN;


import static com.kuka.roboticsAPI.motionModel.BasicMotions.lin;

/**
 * Calibration routine for the SmartPicking vision system.
 * Moves robot to predefined calibration points and communicates with vision system.
 */
public class CalibrationRoutine {

    private static final Logger log = Logger.getLogger(CalibrationRoutine.class);
    
    /**
     * Number of calibration points to visit.
     * Should match the number of points defined in RoboticsAPI.data.xml
     */
    private static final int NUM_CALIBRATION_POINTS = 16;
    
    /**
     * Joint velocity for calibration movements (0.25 = 25% of maximum joint velocity).
     * Lower speeds ensure precise positioning at calibration points.
     */
    private static final double JOINT_VELOCITY = 0.25;
    
    private static final int DELAY_MS = 500;

    private final RoboticsAPIApplication application;
    private final LBR robot;
    private final SmartPickingProtocol protocol;
    private final ObjectFrame flangeFrame;

    public CalibrationRoutine(RoboticsAPIApplication application, LBR robot, 
                             SmartPickingProtocol protocol, ObjectFrame flangeFrame) {
        this.application = application;
        this.robot = robot;
        this.protocol = protocol;
        this.flangeFrame = flangeFrame;
    }

    /**
     * Executes the complete calibration sequence.
     * @param calibrationPointsRoot Root frame containing calibration points (e.g., "/CalibrationPoints")
     * @param testCalibrationFrame Frame to use for calibration test (can be null to skip test)
     * @return true if calibration completed successfully, false otherwise
     */
    public boolean executeCalibration(String calibrationPointsRoot, String testCalibrationFrame) {
        log.info("Starting calibration routine...");
       

        // Set calibration mode
        if (!protocol.setMode(Command.SET_CALIB_MODE)) {
            log.error("Failed to set calibration mode");
            return false;
        }

        log.info("Calibration mode set successfully");
        ThreadUtil.milliSleep(DELAY_MS);

        // Visit all calibration points
        for (int i = 1; i <= NUM_CALIBRATION_POINTS - 1; i++) {
            if (!visitCalibrationPoint(calibrationPointsRoot, i)) {
                log.error("Calibration failed at point " + i);
                return false;
            }
        }

        // Execute calibration
        log.info("All calibration points collected. Executing calibration...");
        VisionResult calibResult = protocol.execute(Command.CALIBRATE, true);
        if (!calibResult.isSuccess()) {
            log.error("Calibration execution failed");
            return false;
        }

        log.info("Calibration executed successfully");
        ThreadUtil.milliSleep(DELAY_MS);

        // Test calibration if test frame is provided
        if (testCalibrationFrame != null && !testCalibrationFrame.isEmpty()) {
            if (!testCalibration(testCalibrationFrame)) {
                log.warn("Calibration test failed - calibration may still be valid");
            }
        } else {
            log.info("Skipping calibration test (no test frame provided)");
        }

        log.info("Calibration routine completed successfully");
        return true;
    }

    /**
     * Visits a single calibration point and sends pose data to vision system.
     */
    private boolean visitCalibrationPoint(String root, int pointNumber) {
        String frameName = root + "/P" + pointNumber;
        log.info("Moving to calibration point: " + frameName);

        // Get the frame from the data file
        ObjectFrame targetFrame;
        try {
            targetFrame = application.getFrame(frameName);
        } catch (Exception e) {
            log.error("Failed to get frame " + frameName + ": " + e.getMessage());
            return false;
        }

        // Move to calibration point using LIN motion
        try {
            LIN linMotion = lin(targetFrame);
            linMotion.setJointVelocityRel(JOINT_VELOCITY);
            robot.move(linMotion);
        } catch (Exception e) {
            log.error("Failed to move to " + frameName + ": " + e.getMessage());
            return false;
        }

        // Get current robot position
        Frame currentPose = robot.getCurrentCartesianPosition(flangeFrame);

        // Send robot pose to vision system
        String[] poseMessage = buildPoseMessage(currentPose);
        log.debug("Sending pose: " + poseMessage);
        protocol.execute(Command.SEND_ROBOT_POSE, false);
        
        // Send robot pose

            String[] poseParts = buildPoseMessage(currentPose);
            VisionResult poseResult = protocol.execute(Command.SEND_ROBOT_POSE,false);
            
            for (int i = 0; i < poseParts.length; i++)
    		{
            	protocol.sendCustomMessage(poseParts[i], false);
    		}
            ThreadUtil.milliSleep(DELAY_MS);
            // Add calibration point
            log.debug("Adding calibration point " + pointNumber);
            VisionResult addResult = protocol.execute(Command.ADD_CALIB_POINT, true);
            

            if (!addResult.isSuccess()) {
                log.error("Failed to add calibration point " + pointNumber);
                return false;
            }

            log.info("Calibration point " + pointNumber + " added successfully");
            ThreadUtil.milliSleep(200);
            return true;

       
    }

    /**
     * Tests the calibration by moving to a test frame and sending pose data.
     */
    private boolean testCalibration(String testFrameName) {
        log.info("Testing calibration at frame: " + testFrameName);

        // Move to test frame
        ObjectFrame testFrame;
        try {
            testFrame = application.getFrame(testFrameName);
        } catch (Exception e) {
            log.error("Failed to get test frame " + testFrameName + ": " + e.getMessage());
            return false;
        }

        try {
            LIN linMotion = lin(testFrame);
            linMotion.setJointVelocityRel(JOINT_VELOCITY);
            robot.move(linMotion);
        } catch (Exception e) {
            log.error("Failed to move to test frame: " + e.getMessage());
            return false;
        }

        ThreadUtil.milliSleep(DELAY_MS);

        // Get current robot position
        Frame currentPose = robot.getCurrentCartesianPosition(flangeFrame);

        // Send robot pose
        String[] poseParts = buildPoseMessage(currentPose);
        VisionResult poseResult = protocol.execute(Command.SEND_ROBOT_POSE, false);
        
        for (int i = 0; i < 6; i++)
		{
        	protocol.sendCustomMessage(poseParts[i], false);
		}
        
        if (!poseResult.isSuccess()) {
            log.error("Failed to send test pose");
            return false;
        }

        ThreadUtil.milliSleep(DELAY_MS);

        // Execute test calibration
        VisionResult testResult = protocol.execute(Command.TEST_CALIB, true);

        if (!testResult.isSuccess()) {
            log.error("Test calibration failed");
            return false;
        }

        log.info("Calibration test passed");
        ThreadUtil.milliSleep(200);

        return true;
    }

    /**
     * Builds the pose message string in the format expected by the vision system.
     * Format: X;Y;Z;Gamma;Beta;Alpha
     * - Positions are multiplied by 10 (mm * 10 = tenths of mm)
     * - Angles are converted from radians to millidegrees (rad * 180/PI * 1000)
     */
    private String[] buildPoseMessage(Frame pose) {
        // Convert positions: mm to tenths of mm
    	String[] poses = new String[6];
        poses[0] = String.format("%.0f", pose.getX()*10);
        poses[1] = String.format("%.0f", pose.getY()*10);
        poses[2] = String.format("%.0f", pose.getZ()*10);
        // Convert angles: radians to millidegrees
        poses[3] = String.format("%.0f", Math.toDegrees(pose.getAlphaRad())*1000);
        poses[4] = String.format("%.0f", Math.toDegrees(pose.getBetaRad())*1000);
        poses[5] = String.format("%.0f", Math.toDegrees(pose.getGammaRad())*1000);

        // Build message string
        return poses;
    }
}
