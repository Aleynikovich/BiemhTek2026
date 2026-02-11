# GUI Backup Information

## Tkinter GUI Backup

The original tkinter-based GUI has been preserved for reference and fallback:

- **File**: `robot_control_gui_tkinter.py` (renamed from `robot_control_gui.py`)
- **Modules**: `modules/` directory contains the tkinter modules

## New Modern GUI

The new GUI uses **Streamlit**, a modern Python framework that provides:
- Responsive, web-based interface
- Better use of screen space with collapsible sections
- Automatic scrolling for content
- Mobile-friendly design
- Live reloading during development

## Running the GUIs

### Tkinter GUI (Backup)
```bash
python3 robot_control_gui_tkinter.py
```

### Streamlit GUI (New)
```bash
streamlit run robot_control_gui.py
```

The Streamlit GUI will open in your default web browser at http://localhost:8501

## Restoring Tkinter GUI

If you need to revert to the tkinter GUI:
```bash
mv robot_control_gui.py robot_control_gui_streamlit.py
mv robot_control_gui_tkinter.py robot_control_gui.py
```

## Installation

For the new Streamlit GUI, install additional dependencies:
```bash
pip install streamlit
```

Tkinter dependencies remain the same (included with Python).
