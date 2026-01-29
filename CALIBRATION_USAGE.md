# Calibration Routine Usage

## Overview
The calibration subroutine allows you to calibrate the SmartPicking vision system with the robot. The routine automatically moves the robot to 16 predefined calibration points and communicates with the vision system to establish the camera-to-robot transformation.

## Prerequisites

1. **Calibration Points**: Define 16 calibration points in `RoboticsAPI.data.xml`:
   - Located at: `/CalibrationPoints/P1` through `/CalibrationPoints/P16`
   - These points are already defined in the current project

2. **Vision Server**: The SmartPicking vision server must be running and accessible on the network
   - Default IP: `172.31.1.69`
   - Default Port: `59002`

## Usage Example

### Basic Usage in Main Application

```java
public void run() {
    // Execute calibration at the start of the application
    boolean calibrationSuccess = executeCalibration("172.31.1.69", 59002);
    
    if (!calibrationSuccess) {
        log.error("Calibration failed - aborting application");
        return;
    }
    
    log.info("Calibration completed - starting normal operation");
    
    // Continue with normal operation...
    while (true) {
        // Your application logic here
    }
}
```

### Standalone Calibration Application

You can also create a separate application class just for calibration:

```java
package biemhTekniker;

import biemhTekniker.calibration.CalibrationRoutine;
import biemhTekniker.logger.Logger;
import biemhTekniker.vision.SmartPickingProtocol;
import biemhTekniker.vision.VisionSocketClient;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import javax.inject.Inject;

public class CalibrationApp extends RoboticsAPIApplication {
    
    @Inject
    private LBR iiwa;
    
    private static final Logger log = Logger.getLogger(CalibrationApp.class);
    
    @Override
    public void initialize() {
        // Initialization code
    }
    
    @Override
    public void run() {
        log.info("Starting calibration application");
        
        // Connect to vision server
        VisionSocketClient visionClient = new VisionSocketClient("172.31.1.69", 59002);
        if (!visionClient.connect()) {
            log.error("Failed to connect to vision server");
            return;
        }
        
        SmartPickingProtocol protocol = new SmartPickingProtocol(visionClient);
        
        // Create calibration routine
        CalibrationRoutine calibration = new CalibrationRoutine(
            this,
            iiwa,
            protocol,
            iiwa.getFlange()
        );
        
        // Execute calibration
        boolean success = calibration.executeCalibration("/CalibrationPoints", null);
        
        // Clean up
        visionClient.close();
        
        if (success) {
            log.info("Calibration completed successfully");
        } else {
            log.error("Calibration failed");
        }
    }
}
```

## Calibration Process

The calibration routine performs the following steps:

1. **Set Calibration Mode**: Sends command "102" to put the vision system in calibration mode
2. **Visit Calibration Points**: For each of the 16 calibration points:
   - Moves to the point using LIN (linear) motion
   - Reads the current robot pose (position and orientation)
   - Sends the pose to the vision system (command "14")
   - Adds the calibration point (command "5")
3. **Execute Calibration**: Sends command "6" to compute the calibration transformation
4. **Test Calibration** (optional): Moves to a test point and verifies the calibration (command "7")

## Motion Parameters

- **Motion Type**: LIN (linear motion)
- **Velocity**: 25% of maximum joint velocity
- **Delays**: 500ms between major steps, 200ms between minor steps

## Pose Data Format

The robot pose is sent to the vision system in the following format:
```
X;Y;Z;Gamma;Beta;Alpha
```

Where:
- X, Y, Z: Position in tenths of millimeters (mm * 10)
- Gamma, Beta, Alpha: Orientation in millidegrees (degrees * 1000)
- Angles are converted from radians using: `angle_rad * 180 * 1000 / π`

## Error Handling

The calibration routine includes comprehensive error handling:
- Connection failures to the vision server
- Frame access errors
- Motion execution errors
- Vision system command failures

Each step is validated, and the routine returns `false` if any step fails.

## Logging

All calibration steps are logged using the custom Logger system:
- Info: Major steps (starting calibration, point added, calibration complete)
- Debug: Detailed information (pose data, command messages)
- Error: Failures and error conditions
- Warn: Non-critical issues (test calibration skipped)

## Test Calibration Frame

To enable test calibration, define a test frame in `RoboticsAPI.data.xml`:
```xml
<frame name="Test">
    <transformation a="..." b="..." c="..." x="..." y="..." z="..."/>
</frame>
```

Then update the Main.java to use it:
```java
boolean success = calibration.executeCalibration(
    "/CalibrationPoints",
    "/CalibrationPoints/Test"  // Enable test calibration
);
```

## Vision System Commands

The following commands are used during calibration:

| Command | Code | Description |
|---------|------|-------------|
| SET_CALIB_MODE | 102 | Switch vision system to calibration mode |
| SEND_ROBOT_POSE | 14 | Send current robot pose to vision system |
| ADD_CALIB_POINT | 5 | Add current point to calibration dataset |
| CALIBRATE | 6 | Compute calibration transformation |
| TEST_CALIB | 7 | Test the calibration accuracy |

## Troubleshooting

**Connection Issues**
- Verify vision server IP and port
- Check network connectivity
- Ensure vision server is running

**Frame Access Errors**
- Verify calibration points exist in RoboticsAPI.data.xml
- Check frame names match exactly (case-sensitive)

**Motion Errors**
- Ensure robot is not in a singularity
- Check that calibration points are reachable
- Verify no collisions along the path

**Vision System Errors**
- Check vision system logs
- Verify camera is properly connected
- Ensure lighting conditions are adequate
