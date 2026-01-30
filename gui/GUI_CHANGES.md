# GUI Changes - Log Level Filtering

## Overview
Added minimum log level filtering functionality to the KUKA Robot Control GUI. Users can now select which log levels they want to see in the console and control what the robot sends over the network.

## New Features

### 1. Log Level Selector
- **Location**: Console Output frame, above the scrolled text area
- **Control Type**: Read-only combobox with 4 options:
  - DEBUG (shows all logs)
  - INFO (shows INFO, WARN, ERROR)
  - WARN (shows WARN, ERROR)
  - ERROR (shows ERROR only)
- **Default**: DEBUG (shows all logs)

### 2. Client-Side Filtering
The GUI filters displayed messages based on the selected log level. Messages with a level lower than the selected minimum are not displayed in the console.

### 3. Server-Side Filtering
When connected to the robot, changing the log level sends a command to the robot to update its NetworkListener filter. This reduces network traffic by having the robot only send logs at or above the selected level.

## Visual Layout

```
┌─ Console Output ────────────────────────────────────────────────────────────┐
│                                                                              │
│  Minimum Log Level: [DEBUG ▼]  (filters logs displayed in console and       │
│                                  sent from robot)                            │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │ [01:18:30] Connected to robot at 172.31.1.69:30001                     │ │
│  │ [01:18:30] Response: Log level set to DEBUG                            │ │
│  │ [01:18:31] [ROBOT] ConsoleCommandHandler: Network listener registered  │ │
│  │ [01:18:31] [ROBOT] Main: Main application running                      │ │
│  │ [01:18:32] [ROBOT] SmartPickingThread: Connected to vision server      │ │
│  │ [01:18:35] Setting program to 1                                        │ │
│  │ [01:18:36] [ROBOT] Main: Program number set to: 1 via console          │ │
│  │                                                                          │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────────────┘
```

## Color Coding

Log messages are color-coded based on their level:
- **DEBUG**: Gray text (less prominent)
- **INFO**: Black text (standard)
- **WARN**: Orange text (caution)
- **ERROR**: Red text (critical)
- **SUCCESS**: Green text (positive feedback)

## Usage

1. **During Connection**: When the GUI connects to the robot, it automatically sends the current log level setting to the robot.

2. **Changing Log Level**: 
   - Select a new level from the dropdown
   - GUI immediately filters displayed messages
   - If connected, sends `set_log_level` command to robot
   - Robot's NetworkListener updates its filter
   - Future logs are filtered at the source

3. **Viewing Different Levels**:
   - Set to **DEBUG** to see all application activity (useful for troubleshooting)
   - Set to **INFO** to see normal operational messages
   - Set to **WARN** to see only warnings and errors
   - Set to **ERROR** to see only critical errors

## Technical Details

### Communication Protocol

**Set Log Level Command:**
```json
{
  "type": "set_log_level",
  "level": "INFO"
}
```

**Response:**
```json
{
  "type": "response",
  "message": "Log level set to INFO",
  "success": true
}
```

**Get Log Level Command:**
```json
{
  "type": "get_log_level"
}
```

**Log Level Response:**
```json
{
  "type": "log_level",
  "level": "INFO"
}
```

### Log Entry Format

The GUI can parse two log formats:

1. **JSON Format** (for command responses):
```json
{
  "type": "log",
  "level": "info",
  "message": "Operation completed"
}
```

2. **NetworkListener Format** (from LogEntry.toString()):
```
[HH:MM:SS.mmm] SourceClass | LEVEL: message text
```

Example:
```
[13:45:23.456] Main | INFO: Application started
```

## Benefits

1. **Reduced Clutter**: Hide debug messages during normal operation
2. **Better Focus**: See only relevant messages for current task
3. **Network Efficiency**: Robot sends fewer messages over network
4. **Troubleshooting**: Easily switch to DEBUG when investigating issues
5. **User Control**: Each client can set their own log level independently
