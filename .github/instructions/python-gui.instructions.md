---
applyTo: "gui/**/*.py"
---

# Instructions for Python GUI Code

The GUI provides remote monitoring and control of the robot via TCP/IP console server.

## Architecture
- **Console Client**: `modules/console_client.py` handles TCP connection to robot (port 30001)
- **Command Protocol**: JSON messages like `{"type": "set_program", "program": 1}`
- **Status Updates**: Poll status every 500ms to update UI with robot state
- **Workpiece Data**: Periodic refresh of workpiece queue for visualization

## Thread Safety in GUI
- **Main Thread**: All Tkinter/Streamlit UI operations must run on main thread
- **Network Thread**: Socket I/O runs in background thread via `threading.Thread`
- **Queue Communication**: Use `queue.Queue` to pass data between threads safely

## Common Patterns
```python
# 1. Sending commands
def send_command(cmd_dict):
    """Send JSON command to robot console"""
    if self.client and self.client.connected:
        self.client.send(json.dumps(cmd_dict))

# 2. Status polling loop (background thread)
def update_loop(self):
    while self.running:
        self.send_command({'type': 'get_status'})
        time.sleep(0.5)

# 3. UI update (main thread)
def update_ui_from_status(self, status_data):
    """Update UI with status data - must run on main thread"""
    self.status_label.config(text=status_data.get('state', 'Unknown'))
```

## Visualization
- **Canvas Coordinate System**: Origin at (50, 350), Y-axis points up
- **3D to 2D Projection**: Use full rotation matrix (Rz→Ry→Rx) for correct arrow orientation
- **Color Coding**: Reference 1 (blue), Reference 2 (green), Reference 3 (red)
- **State Colors**: Available (bright), Picked (medium), Measuring (light), Returned (faded)

## Configuration
- **Connection Settings**: TCP host and port for robot console
- **Refresh Rates**: Status (500ms), Workpieces (manual/periodic)
- **Display Options**: Canvas size, font sizes, color schemes

## Error Handling
- **Connection Lost**: Show clear status indicator, allow reconnection
- **Invalid Responses**: Log but don't crash, show error in UI
- **Timeout**: 5-second timeout on socket operations
