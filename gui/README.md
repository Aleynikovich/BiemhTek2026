# KUKA Robot Control GUI

Python-based GUI application for controlling the KUKA LBR iiwa robot remotely.

## Features

- **Connection Management**: Connect/disconnect to robot console server
- **Program Control**: Set program number (0-7) with dedicated buttons
- **Real-time Status**: Display current program, vision connection status, and workpiece position
- **Console Output**: View robot logs and command responses
- **Log Level Filtering**: Select minimum log level (DEBUG, INFO, WARN, ERROR) to filter console output and reduce network traffic
- **Quick Actions**: Emergency stop, status requests, console clearing

## Requirements

- Python 3.x
- tkinter (usually included with Python)
- Network access to robot (default: 172.31.1.69:30001)

## Installation

1. Ensure Python 3 is installed:
```bash
python3 --version
```

2. Tkinter is typically included with Python. If not, install it:
```bash
# Ubuntu/Debian
sudo apt-get install python3-tk

# macOS (via Homebrew)
brew install python-tk
```

## Usage

### Starting the GUI

```bash
python3 robot_control_gui.py
```

Or make it executable:
```bash
chmod +x robot_control_gui.py
./robot_control_gui.py
```

### Connecting to Robot

1. Enter robot IP address (default: 172.31.1.69)
2. Enter console port (default: 30001)
3. Click "Connect"

### Available Programs

- **Program 0**: Idle (Emergency Stop)
- **Program 1**: Get New Workpiece Position
- **Program 2**: Calibration
- **Program 3**: Test Calibration
- **Program 4**: Pick New Workpiece
- **Program 5**: Place New Workpiece
- **Program 6**: Pick Measured Workpiece
- **Program 7**: Place Measured Workpiece

### Quick Actions

- **Emergency Stop**: Immediately sets program to 0 (Idle)
- **Get Status**: Request current robot status
- **Clear Console**: Clear the console output window

### Log Level Filtering

The GUI includes a log level filter that allows you to control which messages are displayed:

- **DEBUG**: Shows all messages (most verbose)
- **INFO**: Shows informational messages, warnings, and errors
- **WARN**: Shows only warnings and errors
- **ERROR**: Shows only error messages (least verbose)

When connected to the robot, changing the log level also instructs the robot to filter messages at the source, reducing network traffic. Each client can set their own log level independently.

Log messages are color-coded:
- Debug messages appear in gray
- Info messages appear in black
- Warnings appear in orange
- Errors appear in red
- Success messages appear in green

## Architecture

### Communication Protocol

The GUI communicates with the robot using JSON messages over TCP:

**Set Program Command:**
```json
{
  "type": "set_program",
  "program": 2
}
```

**Get Status Command:**
```json
{
  "type": "get_status"
}
```

**Set Log Level Command:**
```json
{
  "type": "set_log_level",
  "level": "INFO"
}
```

**Get Log Level Command:**
```json
{
  "type": "get_log_level"
}
```

**Status Response:**
```json
{
  "type": "status",
  "program": 2,
  "vision_connected": true,
  "workpiece_position": "WorkpieceData{x=-601.5, y=109.2, z=1193.7, ...}"
}
```

**Log Level Response:**
```json
{
  "type": "log_level",
  "level": "DEBUG"
}
```

**Log Message:**
```json
{
  "type": "log",
  "level": "info",
  "message": "Operation completed successfully"
}
```

Or in NetworkListener format:
```
[13:45:23.456] ClassName | INFO: Operation completed successfully
```

### Robot-Side Components

- **ConsoleServer.java**: Accepts TCP connections and manages client handlers
- **ConsoleCommandHandler.java**: Processes JSON commands from GUI, manages NetworkListener for log forwarding
- **ConsoleServerInterface.java**: Interface for Main.java to provide robot state
- **SimpleJSON.java**: Minimal JSON implementation for Java 1.7 compatibility
- **NetworkListener.java**: Forwards log entries to connected clients with configurable filtering
- **LogManager.java**: Central logging system that broadcasts to all registered listeners

## Troubleshooting

### Cannot Connect
- Verify robot IP address and port
- Check network connectivity: `ping 172.31.1.69`
- Ensure robot console server is running
- Check firewall settings

### Connection Drops
- Network instability
- Robot controller restart
- Check robot logs for errors

### Program Not Changing
- Verify connection status (should show "Connected" in green)
- Check console output for error messages
- Try "Get Status" to verify communication

## Development

### Adding New Commands

1. Update `handleCommand()` in GUI to send new command type
2. Add handler in `ConsoleCommandHandler.java`
3. Update `ConsoleServerInterface.java` if new robot state access needed

### Customizing Interface

The GUI uses tkinter and follows a modular structure:
- Connection frame: Network settings and connection controls
- Status frame: Real-time robot state display
- Program control frame: Program selection buttons
- Quick actions frame: Common operations
- Console frame: Log output

Modify `create_*_frame()` methods in `RobotControlGUI` class to customize layout.

## License

Part of BiemhTek2026 KUKA Robot Project
