# GUI Version Information

## Current GUI: Tkinter (Desktop Application)

**File:** `robot_control_gui.py`  
**Framework:** Tkinter (Desktop)  
**Run command:** `python3 robot_control_gui.py`

### Running the GUI:
```bash
cd gui
python3 robot_control_gui.py
```

### Features:
- Native desktop application
- Fixed window size (900x1000)
- All controls in one window
- Workpiece canvas visualization (650x480mm)
- Real-time status updates
- Motion override controls (single-configuration testing)

---

## Alternative: Streamlit GUI (Web-based)

**File:** `robot_control_gui_streamlit.py`  
**Framework:** Streamlit (Web)  
**Status:** Available as alternative, but may have refresh issues

### Running the Streamlit GUI:
```bash
pip install streamlit matplotlib numpy
streamlit run robot_control_gui_streamlit.py
```

**Note:** The Streamlit version has known issues with live data updates and is provided as an experimental alternative. The tkinter version is recommended for production use.

---

## Version History

- **Original**: Tkinter GUI with CSV-based motion overrides
- **Refactored**: Tkinter GUI with single-configuration motion override testing
- **Attempted Port**: Streamlit web-based GUI (reverted due to data update issues)
- **Current**: Tkinter GUI (stable, production-ready)

Both versions support the new single-configuration motion override protocol:
```json
{
  "type": "set_motion_override",
  "pick_redundancy": -80,
  "pick_alternate": false,
  "place_redundancy": 60,
  "place_zrot": 45.0
}
```

