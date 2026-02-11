# Quick Start - New Streamlit GUI

## Installation

```bash
cd gui
pip install streamlit
```

Or use the requirements file:
```bash
pip install -r requirements_streamlit.txt
```

## Running the GUI

```bash
streamlit run robot_control_gui.py
```

The GUI will automatically open in your default web browser at:
```
http://localhost:8501
```

## Features

### 🎯 Responsive Design
- Automatically adapts to your screen size
- Works on desktop, tablet, and mobile
- Sidebar collapses on small screens

### 📦 Better Space Management
- **Collapsible sections**: Motion Overrides fold away when not needed
- **Scrollable lists**: Workpiece list scrolls independently
- **Flexible layout**: No fixed window size constraints

### 🚀 Modern Interface
- Clean, professional design with icons
- Color-coded status indicators
- Real-time updates
- Tab-based navigation

## Usage

1. **Connect to Robot**
   - Enter IP and Port in the sidebar
   - Click "Connect"
   - Status will show "● Connected" in green

2. **Control Programs**
   - Use "Robot Programs" tab for motion programs
   - Use "Vision Commands" tab for vision system
   - Quick actions available on each tab

3. **Manage Workpieces**
   - Click "Refresh Workpieces" to load queue
   - Workpieces shown in collapsible cards (saves space!)
   - Motion Overrides in expandable section (click to open)

4. **Monitor Console**
   - View logs in "Console" tab
   - Set log level (DEBUG, INFO, WARN, ERROR)
   - Color-coded messages

## Advantages Over Tkinter GUI

| Feature | Streamlit | Tkinter |
|---------|-----------|---------|
| Vertical Space | Unlimited (scrollable) | Fixed 1000px |
| Layout | Responsive | Fixed |
| Mobile Support | Yes | No |
| Browser Access | Yes | No |
| Collapsible Sections | Yes | No |
| Auto-scroll Lists | Yes | No |

## Troubleshooting

### Port Already in Use
If you see "Address already in use", stop the existing Streamlit process:
```bash
pkill -f streamlit
streamlit run robot_control_gui.py
```

### Browser Doesn't Open
Manually navigate to: http://localhost:8501

### Connection Issues
- Verify robot IP address in sidebar
- Check robot console server is running
- Test with: `ping <robot_ip>`

## Reverting to Tkinter GUI

If you prefer the old interface:
```bash
python3 robot_control_gui_tkinter.py
```

Both GUIs connect to the same robot and use the same protocol.

## Notes

- The GUI state persists during the session
- Refresh the browser page to reset state
- Connection survives page refresh (background thread)
- All motion override features preserved from tkinter version
