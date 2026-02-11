#!/usr/bin/env python3
"""
KUKA Robot Control GUI - Streamlit Version
Modern, responsive web-based interface for controlling the KUKA LBR iiwa robot.

Run with: streamlit run robot_control_gui.py
"""

import streamlit as st
import json
import time
import socket
import threading
from datetime import datetime

# Page configuration
st.set_page_config(
    page_title="KUKA Robot Control",
    page_icon="🤖",
    layout="wide",
    initial_sidebar_state="expanded"
)

# Custom CSS for better styling and responsive design
st.markdown("""
<style>
    .stButton>button {
        width: 100%;
    }
    .status-connected {
        color: green;
        font-weight: bold;
    }
    .status-disconnected {
        color: red;
        font-weight: bold;
    }
    .program-label {
        color: blue;
        font-weight: bold;
        font-size: 1.2em;
    }
    div[data-testid="stExpander"] {
        background-color: #f0f2f6;
        border-radius: 5px;
        padding: 10px;
        margin-bottom: 10px;
    }
    /* Make workpiece list scrollable */
    .workpiece-container {
        max-height: 600px;
        overflow-y: auto;
    }
</style>
""", unsafe_allow_html=True)

# Initialize session state
if 'connected' not in st.session_state:
    st.session_state.connected = False
if 'robot_ip' not in st.session_state:
    st.session_state.robot_ip = "172.31.1.147"
if 'robot_port' not in st.session_state:
    st.session_state.robot_port = 30001
if 'current_program' not in st.session_state:
    st.session_state.current_program = 0
if 'vision_connected' not in st.session_state:
    st.session_state.vision_connected = False
if 'workpiece_position' not in st.session_state:
    st.session_state.workpiece_position = "Not retrieved"
if 'console_log' not in st.session_state:
    st.session_state.console_log = []
if 'client_socket' not in st.session_state:
    st.session_state.client_socket = None
if 'workpieces' not in st.session_state:
    st.session_state.workpieces = []
if 'gripper_states' not in st.session_state:
    st.session_state.gripper_states = {'g1': False, 'g2': False, 'g3': False}

# Helper functions
def log_message(message, level="INFO"):
    """Add message to console log"""
    timestamp = datetime.now().strftime("%H:%M:%S")
    st.session_state.console_log.append(f"[{timestamp}] {level}: {message}")
    # Keep only last 100 messages
    if len(st.session_state.console_log) > 100:
        st.session_state.console_log = st.session_state.console_log[-100:]

def send_command(command):
    """Send JSON command to robot"""
    if not st.session_state.connected or st.session_state.client_socket is None:
        log_message("Not connected to robot", "ERROR")
        st.error("Not connected to robot")
        return False
    
    try:
        message = json.dumps(command) + "\n"
        st.session_state.client_socket.sendall(message.encode('utf-8'))
        log_message(f"Sent: {command['type']}", "DEBUG")
        return True
    except Exception as e:
        log_message(f"Send error: {str(e)}", "ERROR")
        st.error(f"Communication error: {str(e)}")
        return False

def connect_to_robot():
    """Establish connection to robot"""
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.settimeout(5)
        sock.connect((st.session_state.robot_ip, st.session_state.robot_port))
        st.session_state.client_socket = sock
        st.session_state.connected = True
        log_message(f"Connected to {st.session_state.robot_ip}:{st.session_state.robot_port}", "SUCCESS")
        st.success("Connected to robot!")
        # Start listener thread
        threading.Thread(target=message_listener, daemon=True).start()
        return True
    except Exception as e:
        log_message(f"Connection failed: {str(e)}", "ERROR")
        st.error(f"Connection failed: {str(e)}")
        return False

def disconnect_from_robot():
    """Disconnect from robot"""
    if st.session_state.client_socket:
        try:
            st.session_state.client_socket.close()
        except:
            pass
        st.session_state.client_socket = None
    st.session_state.connected = False
    log_message("Disconnected from robot", "INFO")

def message_listener():
    """Background thread to receive messages"""
    while st.session_state.connected and st.session_state.client_socket:
        try:
            data = st.session_state.client_socket.recv(4096)
            if not data:
                break
            messages = data.decode('utf-8').strip().split('\n')
            for msg in messages:
                if msg:
                    handle_response(msg)
        except:
            break
    st.session_state.connected = False

def handle_response(response):
    """Process response from robot"""
    try:
        data = json.loads(response)
        msg_type = data.get('type')
        
        if msg_type == 'status':
            st.session_state.current_program = data.get('program', 0)
            st.session_state.vision_connected = data.get('vision_connected', False)
            st.session_state.workpiece_position = data.get('workpiece_position', 'Not retrieved')
            st.session_state.gripper_states = {
                'g1': data.get('gripper1_closed', False),
                'g2': data.get('gripper2_closed', False),
                'g3': data.get('gripper3_closed', False)
            }
        elif msg_type == 'workpieces':
            workpieces_json = data.get('workpieces', '[]')
            st.session_state.workpieces = json.loads(workpieces_json)
        elif msg_type == 'log':
            log_message(data.get('message', ''), data.get('level', 'INFO').upper())
        elif msg_type == 'response':
            log_message(data.get('message', ''), "INFO")
    except json.JSONDecodeError:
        log_message(f"[ROBOT] {response}", "INFO")

# Header
st.title("🤖 KUKA LBR iiwa Robot Control")
st.caption("Modern Streamlit Interface - BiemhTek2026")

# Sidebar - Connection and Status
with st.sidebar:
    st.header("Connection")
    
    col1, col2 = st.columns([3, 1])
    with col1:
        st.session_state.robot_ip = st.text_input("Robot IP", value=st.session_state.robot_ip)
    with col2:
        st.session_state.robot_port = st.number_input("Port", value=st.session_state.robot_port, step=1, min_value=1, max_value=65535)
    
    col1, col2 = st.columns(2)
    with col1:
        if st.button("Connect", disabled=st.session_state.connected, use_container_width=True):
            connect_to_robot()
    with col2:
        if st.button("Disconnect", disabled=not st.session_state.connected, use_container_width=True):
            disconnect_from_robot()
    
    status_text = "● Connected" if st.session_state.connected else "● Disconnected"
    status_class = "status-connected" if st.session_state.connected else "status-disconnected"
    st.markdown(f'<p class="{status_class}">{status_text}</p>', unsafe_allow_html=True)
    
    st.divider()
    
    st.header("Robot Status")
    program_names = {
        0: "Idle", 1: "Pick New", 2: "Place New",
        3: "Pick Measured", 4: "Place Measured",
        5: "Calibration", 6: "Test Calib"
    }
    prog_name = program_names.get(st.session_state.current_program, "Unknown")
    st.markdown(f'<p class="program-label">Program: {st.session_state.current_program} - {prog_name}</p>', unsafe_allow_html=True)
    
    vision_status = "● Connected" if st.session_state.vision_connected else "● Disconnected"
    vision_class = "status-connected" if st.session_state.vision_connected else "status-disconnected"
    st.markdown(f'Vision Server: <span class="{vision_class}">{vision_status}</span>', unsafe_allow_html=True)
    
    st.text(f"Position: {st.session_state.workpiece_position[:30]}...")
    
    # Gripper states
    st.subheader("Gripper States")
    col1, col2, col3 = st.columns(3)
    with col1:
        g1_icon = "🔴" if st.session_state.gripper_states['g1'] else "⚪"
        st.text(f"{g1_icon} G1")
    with col2:
        g2_icon = "🔴" if st.session_state.gripper_states['g2'] else "⚪"
        st.text(f"{g2_icon} G2")
    with col3:
        g3_icon = "🔴" if st.session_state.gripper_states['g3'] else "⚪"
        st.text(f"{g3_icon} G3")
    
    if st.button("Get Status", use_container_width=True):
        send_command({'type': 'get_status'})

# Main content area with tabs
tab1, tab2, tab3, tab4 = st.tabs(["🤖 Robot Programs", "👁️ Vision Commands", "📦 Workpieces", "💬 Console"])

# Tab 1: Robot Programs
with tab1:
    st.subheader("Robot Motion Programs (1-99)")
    
    programs = [
        (0, "Idle"), (1, "Pick New Workpiece"), (2, "Place New Workpiece"),
        (3, "Pick Measured Workpiece"), (4, "Place Measured Workpiece"),
        (5, "Calibration"), (6, "Test Calibration"),
    ]
    
    col1, col2, col3 = st.columns(3)
    for i, (num, name) in enumerate(programs):
        col = [col1, col2, col3][i % 3]
        with col:
            if st.button(f"{num}: {name}", key=f"prog_{num}", use_container_width=True):
                send_command({'type': 'set_program', 'program': num})
                log_message(f"Setting program to {num}", "INFO")
    
    st.divider()
    st.subheader("Quick Actions")
    col1, col2, col3 = st.columns(3)
    with col1:
        if st.button("🛑 Emergency Stop (Program 0)", use_container_width=True):
            send_command({'type': 'set_program', 'program': 0})
    with col2:
        if st.button("🏠 Cancel & Return Home", use_container_width=True):
            send_command({'type': 'cancel_program'})
    with col3:
        if st.button("🔄 Refresh Status", use_container_width=True):
            send_command({'type': 'get_status'})

# Tab 2: Vision Commands
with tab2:
    st.subheader("Vision System Commands (100-199)")
    
    cmds = [
        (100, "Load References"), (101, "Set Auto Mode"), (102, "Set Calibration Mode"),
        (103, "Capture Data"), (104, "Locate Container"), (105, "Get Container Position"),
        (106, "Locate Parts"), (107, "Get Part Position"), (108, "Get Next Part Position"),
        (109, "Full Scan Sequence"), (111, "Get New Workpiece Position (Legacy)"),
    ]
    
    col1, col2, col3 = st.columns(3)
    for i, (num, name) in enumerate(cmds):
        col = [col1, col2, col3][i % 3]
        with col:
            if st.button(f"{num}: {name}", key=f"vision_{num}", use_container_width=True):
                send_command({'type': 'set_program', 'program': num})
                log_message(f"Setting vision program to {num}", "INFO")

# Tab 3: Workpieces
with tab3:
    st.subheader("Workpiece Database & Visualization")
    
    # Action buttons
    col1, col2, col3, col4, col5 = st.columns(5)
    with col1:
        if st.button("🔄 Refresh Workpieces", use_container_width=True):
            send_command({'type': 'get_workpieces'})
    with col2:
        if st.button("📊 Get Queue Status", use_container_width=True):
            send_command({'type': 'get_queue_status'})
    with col3:
        if st.button("🗑️ Clear Queue", use_container_width=True):
            send_command({'type': 'clear_queue'})
    with col4:
        if st.button("❌ Delete Selected", use_container_width=True, disabled=True):
            st.info("Select a workpiece first")
    with col5:
        if st.button("🤏 Pick Selected", use_container_width=True, disabled=True):
            st.info("Select a workpiece first")
    
    st.divider()
    
    # Workpieces display in columns for better space usage
    col1, col2 = st.columns([1, 1])
    
    with col1:
        st.markdown("#### 📋 Workpiece List")
        if st.session_state.workpieces:
            # Scrollable container
            for wp in st.session_state.workpieces:
                with st.expander(f"ID: {str(wp.get('id', 'N/A'))[-8:]} | Ref: {wp.get('reference', 'N/A')} | State: {wp.get('state', 'N/A')}"):
                    st.text(f"Gripper: {wp.get('gripper', 'N/A')}")
                    st.text(f"Position: ({wp.get('x', 0):.1f}, {wp.get('y', 0):.1f}, {wp.get('z', 0):.1f}) mm")
                    st.text(f"Rotation: ({wp.get('rx', 0):.1f}, {wp.get('ry', 0):.1f}, {wp.get('rz', 0):.1f}) deg")
                    st.text(f"Score: {wp.get('score', 0):.2f}")
        else:
            st.info("No workpieces in queue. Click 'Refresh Workpieces' to load.")
    
    with col2:
        st.markdown("#### 🎯 Working Plane Visualization")
        st.info("Canvas visualization - placeholder for workpiece positions")
        st.markdown("**Note:** Gripper states are shown in the sidebar →")
        st.markdown("---")
        st.markdown("##### Workpiece Distribution")
        if st.session_state.workpieces:
            # Simple stats
            total = len(st.session_state.workpieces)
            st.metric("Total Workpieces", total)
            # Could add more visualization here with plotly
        else:
            st.info("No data to visualize")
    
    st.divider()
    
    # Motion Overrides - Collapsible section (saves vertical space!)
    with st.expander("⚙️ Motion Overrides (Advanced)", expanded=False):
        st.markdown("**Test one specific motion configuration** (not fallback lists)")
        
        override_enabled = st.checkbox("✅ Override Motions (test one specific configuration)")
        
        if override_enabled:
            st.markdown("##### 📍 Pick Parameters")
            col1, col2 = st.columns(2)
            with col1:
                redundancy_options = [
                    "None (no E1 offset)", "E1 = -100°", "E1 = -80°", "E1 = -60°",
                    "E1 = -40°", "E1 = 0°", "E1 = 40°", "E1 = 60°", "E1 = 80°", "E1 = 100°"
                ]
                pick_redundancy = st.selectbox("Redundancy", redundancy_options, index=2)
            with col2:
                pick_alternate = st.radio("Orientation", ["Regular", "Alternate (180°)"], horizontal=True)
            
            st.markdown("##### 🎯 Place Parameters")
            col1, col2 = st.columns(2)
            with col1:
                place_redundancy = st.selectbox("Redundancy ", redundancy_options, index=7, key="place_red")
            with col2:
                place_zrot = st.number_input("Z-Rotation (degrees)", value=45.0, step=1.0)
            
            if st.button("✓ Apply Motion Override", use_container_width=True):
                # Parse redundancy values
                def parse_redundancy(val):
                    if val.startswith("None"):
                        return None
                    import re
                    match = re.search(r'=\s*(-?\d+)', val)
                    return float(match.group(1)) if match else None
                
                pick_red = parse_redundancy(pick_redundancy)
                place_red = parse_redundancy(place_redundancy)
                
                cmd = {
                    'type': 'set_motion_override',
                    'pick_alternate': pick_alternate == "Alternate (180°)",
                    'place_zrot': place_zrot
                }
                if pick_red is not None:
                    cmd['pick_redundancy'] = pick_red
                if place_red is not None:
                    cmd['place_redundancy'] = place_red
                
                send_command(cmd)
                st.success("Motion override applied!")
        else:
            # Send clear command when disabled
            if st.button("Clear Motion Override", use_container_width=True):
                send_command({'type': 'clear_motion_override'})
                st.success("Motion override cleared!")

# Tab 4: Console
with tab4:
    st.subheader("Console Output")
    
    col1, col2, col3 = st.columns([2, 1, 1])
    with col1:
        log_level = st.selectbox("Log Level", ["DEBUG", "INFO", "WARN", "ERROR"], index=1)
    with col2:
        if st.button("Set Log Level", use_container_width=True):
            send_command({'type': 'set_log_level', 'level': log_level})
    with col3:
        if st.button("Clear Console", use_container_width=True):
            st.session_state.console_log = []
            st.rerun()
    
    # Console output - scrollable
    st.markdown("---")
    console_container = st.container()
    with console_container:
        if st.session_state.console_log:
            # Show last 50 messages
            for msg in st.session_state.console_log[-50:]:
                if "ERROR" in msg:
                    st.markdown(f'<span style="color:red">{msg}</span>', unsafe_allow_html=True)
                elif "WARN" in msg:
                    st.markdown(f'<span style="color:orange">{msg}</span>', unsafe_allow_html=True)
                elif "SUCCESS" in msg:
                    st.markdown(f'<span style="color:green">{msg}</span>', unsafe_allow_html=True)
                else:
                    st.text(msg)
        else:
            st.info("No console messages yet. Connect to robot to see logs.")

# Footer
st.markdown("---")
st.caption("KUKA LBR iiwa Robot Control - Streamlit Version | BiemhTek2026 | [Old tkinter GUI available as robot_control_gui_tkinter.py]")
