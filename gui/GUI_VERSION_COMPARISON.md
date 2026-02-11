# GUI Version Comparison

## Streamlit GUI (New - Recommended)

**File:** `robot_control_gui.py`  
**Framework:** Streamlit (Web-based)  
**Run command:** `streamlit run robot_control_gui.py`

### Advantages:
✅ **Responsive Design** - Automatically adapts to screen size  
✅ **Better Space Usage** - Collapsible sections (expanders) save vertical space  
✅ **Modern UI** - Clean, professional interface with icons  
✅ **Auto-scrolling** - Lists and content areas scroll automatically  
✅ **Web-based** - Access from any device with a browser  
✅ **Live Reload** - Automatic updates during development  
✅ **Mobile Friendly** - Works on tablets and phones  

### Installation:
```bash
pip install -r requirements_streamlit.txt
# or
pip install streamlit
```

### Running:
```bash
cd gui
streamlit run robot_control_gui.py
```
The GUI will open in your browser at http://localhost:8501

### Key Features:
- Sidebar for connection and status (always visible)
- Tabbed interface for different sections
- **Collapsible Motion Overrides** - Saves ~200px of vertical space
- **Scrollable Workpiece List** - No vertical space constraints
- Color-coded console output
- Real-time status updates

---

## Tkinter GUI (Backup)

**File:** `robot_control_gui_tkinter.py`  
**Framework:** Tkinter (Desktop)  
**Run command:** `python3 robot_control_gui_tkinter.py`

### Characteristics:
- Fixed window size (900x1000)
- Desktop application
- All sections visible at once (causes vertical space issues)
- Requires X11/display server

### When to use:
- No internet access needed
- Prefer native desktop app
- Browser not available

### Running:
```bash
cd gui
python3 robot_control_gui_tkinter.py
```

---

## Migration Notes

Both GUIs use the same communication protocol and connect to the same robot console server.  
All backend modules (`modules/`) work with both interfaces.

The Streamlit GUI solves the vertical space problem mentioned in the PR comments by:
1. Using collapsible sections (expanders) for Motion Overrides
2. Making the workpiece list scrollable
3. Using a responsive layout that adapts to window size
4. Placing status information in a sidebar

To switch back to tkinter:
```bash
mv robot_control_gui.py robot_control_gui_streamlit.py
mv robot_control_gui_tkinter.py robot_control_gui.py
```
