# Auto Cycle Feature Documentation

## Overview
The Auto Cycle feature enables fully automatic operation of the robot through a repeating sequence of operations. This eliminates the need for manual intervention between each step.

## Automatic Sequence
When the auto cycle is started, the robot will continuously execute the following sequence:

1. **Home** - Move to home position
2. **Load Reference** (Program 100) - Initialize vision reference data
3. **Full Scan** (Program 109) - Scan workpiece container and populate queue
4. **Pick New Workpiece** (Program 1) - Pick the next available workpiece
5. **Home** - Return to home position
6. **Place New Workpiece** (Program 2) - Place workpiece in measuring machine
7. **Home** - Return to home position
8. **Repeat** - Continue from step 1

## Usage

### Starting Auto Cycle
**From Python GUI:**
1. Connect to the robot via the GUI
2. Navigate to the "Program Control" tab
3. Click the "START AUTO CYCLE" button
4. The status indicator will change to "Status: Running" (green)

**From Console Command:**
```json
{"type": "start_auto_cycle"}
```

### Stopping Auto Cycle
**From Python GUI:**
1. Click the "STOP AUTO CYCLE" button
2. The cycle will stop after completing the current program
3. The status indicator will change to "Status: Stopped" (gray)

**From Console Command:**
```json
{"type": "stop_auto_cycle"}
```

### Checking Auto Cycle Status
**From Console Command:**
```json
{"type": "get_auto_cycle_status"}
```

**Response:**
```json
{"type": "auto_cycle_status", "running": true}
```

## Status Auto-Refresh
The "Get Status" button now automatically enables periodic status refreshing:
- Vision server connection status is updated every 2 seconds
- Workpiece queue is refreshed automatically
- No console logging for status updates (silent polling)

## Safety Considerations

### PLC Handshake (Future Implementation)
The auto cycle includes a placeholder for Zeiss PLC handshake checking before the place operation:
- The robot should only place workpieces when the measuring machine signals it is ready
- Currently commented out in `AutoCycleManager.java` line ~212
- To enable: implement `checkZeissPLCReady()` method with appropriate IO checks

### Emergency Stop
The auto cycle can be stopped at any time:
- Use "STOP AUTO CYCLE" button in GUI
- Use "Emergency Stop (Program 0)" button for immediate halt
- Use "Cancel & Return Home" to abort current operation

### Error Handling
If any step in the cycle fails:
- The auto cycle will automatically stop
- An error message will be logged
- The robot will maintain its current position

## Architecture

### Java Backend Components

#### AutoCycleManager
- **Location:** `src/biemhTekniker/lib/managers/AutoCycleManager.java`
- **Purpose:** Manages the automatic cycle execution in a background thread
- **Key Methods:**
  - `startCycle()` - Starts the auto cycle
  - `stopCycle()` - Stops the auto cycle
  - `isRunning()` - Returns current status

#### ConsoleCommandHandler
- **Location:** `src/biemhTekniker/console/ConsoleCommandHandler.java`
- **Updates:** Added handlers for `start_auto_cycle`, `stop_auto_cycle`, and `get_auto_cycle_status` commands

#### AppController
- **Location:** `src/biemhTekniker/lib/managers/AppController.java`
- **Updates:** Integrated AutoCycleManager and implemented ConsoleServerInterface methods

### Python GUI Components

#### robot_control_gui.py / robot_control_gui_tkinter.py
- **Updates:**
  - Added auto cycle buttons and status indicator
  - Implemented auto-refresh for status polling
  - Added auto cycle status polling (1 second interval)

## Configuration
No additional configuration is required. The auto cycle uses existing program numbers:
- 100: Load Reference
- 109: Full Scan
- 1: Pick New Workpiece
- 2: Place New Workpiece

## Troubleshooting

### Auto Cycle Won't Start
- Check if robot is connected to GUI
- Verify robot is not currently executing a program
- Check console logs for error messages

### Auto Cycle Stops Unexpectedly
- Check for program execution errors in logs
- Verify vision server connection is stable
- Ensure workpiece queue has available workpieces

### Status Not Updating
- Verify GUI is connected to robot
- Check network connection stability
- Restart auto-refresh by clicking "Get Status" button
