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
import math
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import Rectangle, Circle
import numpy as np

# Page configuration
st.set_page_config(
    page_title="KUKA Robot Control",
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
if 'last_update' not in st.session_state:
    st.session_state.last_update = time.time()
if 'auto_refresh' not in st.session_state:
    st.session_state.auto_refresh = True

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

def create_workpiece_canvas(workpieces):
    """Create matplotlib figure showing workpiece positions on working plane (650x480mm)"""
    fig, ax = plt.subplots(figsize=(10, 7.5))
    
    # Canvas dimensions - 650x480mm working plane
    canvas_width = 650
    canvas_height = 480
    
    # Set up the plot
    ax.set_xlim(-50, canvas_width + 50)
    ax.set_ylim(-50, canvas_height + 50)
    ax.set_aspect('equal')
    ax.set_xlabel('X (mm)', fontsize=10)
    ax.set_ylabel('Y (mm)', fontsize=10)
    ax.set_title(f'Working Plane ({canvas_width}x{canvas_height}mm)', fontsize=12, fontweight='bold')
    ax.grid(True, alpha=0.3, linestyle='--', linewidth=0.5)
    
    # Draw border
    border = Rectangle((0, 0), canvas_width, canvas_height, 
                       fill=False, edgecolor='black', linewidth=2)
    ax.add_patch(border)
    
    # Workpiece dimensions and colors
    wp_length = 80
    wp_width = 40
    ref_colors = {1: '#FF0000', 2: '#800080', 3: '#0000FF'}
    state_colors = {'AVAILABLE': 'black', 'PICKED': 'orange', 'MEASURING': 'purple', 
                   'MEASURED': 'blue', 'RETURNED': 'gray', 'NEW': 'green', 'PROCESSING': 'orange'}
    
    # Draw each workpiece
    for wp in workpieces:
        x = float(wp.get('x', 0))
        y = float(wp.get('y', 0))
        rx_deg = float(wp.get('rx', 0))
        ry_deg = float(wp.get('ry', 0))
        rz_deg = float(wp.get('rz', 0))
        ref = wp.get('reference', 1)
        state = wp.get('state', 'AVAILABLE')
        gripper = wp.get('gripper', '')
        wp_id = str(wp.get('id', '?'))
        orientation = wp.get('orientation', 0)
        
        # Convert to canvas coordinates
        canvas_x = x + 150
        canvas_y = -y - 250 + canvas_height
        
        # Skip if out of bounds
        if canvas_x < -100 or canvas_x > canvas_width + 100 or canvas_y < -100 or canvas_y > canvas_height + 100:
            continue
        
        # Get colors
        fill_color = ref_colors.get(ref, '#CCCCCC')
        outline_color = state_colors.get(state, 'black')
        outline_width = 3 if state in ['PICKED', 'PROCESSING'] else 2
        
        # Calculate rotation
        az = math.radians(rz_deg)
        ay = math.radians(ry_deg)
        ax = math.radians(rx_deg)
        
        # Rotation matrices for projection
        ux_x = math.cos(az) * math.cos(ay)
        ux_y = math.sin(az) * math.cos(ay)
        uy_x = math.cos(az) * math.sin(ay) * math.sin(ax) - math.sin(az) * math.cos(ax)
        uy_y = math.sin(az) * math.sin(ay) * math.sin(ax) + math.cos(az) * math.cos(ax)
        
        # Calculate rectangle corners
        half_l, half_w = wp_length / 2, wp_width / 2
        corners_basis = [
            (-half_l, -half_w), (+half_l, -half_w), (+half_l, +half_w), (-half_l, +half_w)
        ]
        rotated_corners = []
        for cl, cw in corners_basis:
            px = cl * ux_x + cw * uy_x
            py = cl * ux_y + cw * uy_y
            rotated_corners.extend([canvas_x + px, canvas_y - py])
        
        # Draw workpiece polygon
        polygon = plt.Polygon(
            [(rotated_corners[i], rotated_corners[i+1]) for i in range(0, len(rotated_corners), 2)],
            facecolor=fill_color, edgecolor=outline_color, linewidth=outline_width
        )
        ax.add_patch(polygon)
        
        # Draw orientation arrow
        flip = -1 if orientation == 1 else 1
        arrow_length = wp_length / 2
        arrow_dx = arrow_length * ux_x * flip
        arrow_dy = arrow_length * ux_y * flip
        
        ax.arrow(canvas_x, canvas_y, arrow_dx, -arrow_dy,
                head_width=10, head_length=8, fc='yellow', ec='yellow', 
                linewidth=2, length_includes_head=True, zorder=5)
        
        # Draw rotation radius circle
        rev_rad = wp_length / 2 + wp_width / 4
        circle = Circle((canvas_x, canvas_y), rev_rad, 
                       fill=False, edgecolor='red', linestyle='--', linewidth=1, zorder=1)
        ax.add_patch(circle)
        
        # Add label
        label = f"ID:{wp_id[-4:]}"
        if gripper and str(gripper) != 'None':
            label += f"\nG:{gripper}"
        ax.text(canvas_x, canvas_y, label, 
               ha='center', va='center', fontsize=8, 
               color='white', fontweight='bold', zorder=10,
               bbox=dict(boxstyle='round,pad=0.3', facecolor='black', alpha=0.7))
    
    # Add legend
    legend_elements = [
        mpatches.Patch(facecolor='#FF0000', edgecolor='black', label='Ref 1'),
        mpatches.Patch(facecolor='#800080', edgecolor='black', label='Ref 2'),
        mpatches.Patch(facecolor='#0000FF', edgecolor='black', label='Ref 3'),
    ]
    ax.legend(handles=legend_elements, loc='upper left', fontsize=9)
    
    plt.tight_layout()
    return fig

# Header
st.title("KUKA LBR iiwa Robot Control")
st.caption("Streamlit Interface - BiemhTek2026")

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
        g1_status = "CLOSED" if st.session_state.gripper_states['g1'] else "OPEN"
        st.text(f"G1: {g1_status}")
    with col2:
        g2_status = "CLOSED" if st.session_state.gripper_states['g2'] else "OPEN"
        st.text(f"G2: {g2_status}")
    with col3:
        g3_status = "CLOSED" if st.session_state.gripper_states['g3'] else "OPEN"
        st.text(f"G3: {g3_status}")
    
    if st.button("Get Status", use_container_width=True):
        send_command({'type': 'get_status'})
    
    st.divider()
    
    # Auto-refresh toggle
    auto_refresh = st.checkbox("Auto-refresh (every 2s)", value=st.session_state.auto_refresh, key="auto_refresh_checkbox")
    if auto_refresh != st.session_state.auto_refresh:
        st.session_state.auto_refresh = auto_refresh
        st.session_state.last_update = 0  # Force immediate update

# Main content area with tabs
tab1, tab2, tab3, tab4 = st.tabs(["Robot Programs", "Vision Commands", "Workpieces", "Console"])

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
        if st.button("Emergency Stop (Program 0)", use_container_width=True):
            send_command({'type': 'set_program', 'program': 0})
    with col2:
        if st.button("Cancel & Return Home", use_container_width=True):
            send_command({'type': 'cancel_program'})
    with col3:
        if st.button("Refresh Status", use_container_width=True):
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
        if st.button("Refresh Workpieces", use_container_width=True):
            send_command({'type': 'get_workpieces'})
    with col2:
        if st.button("Get Queue Status", use_container_width=True):
            send_command({'type': 'get_queue_status'})
    with col3:
        if st.button("Clear Queue", use_container_width=True):
            send_command({'type': 'clear_queue'})
    with col4:
        if st.button("Delete Selected", use_container_width=True, disabled=True):
            st.info("Select a workpiece first")
    with col5:
        if st.button("Pick Selected", use_container_width=True, disabled=True):
            st.info("Select a workpiece first")
    
    st.divider()
    
    # Workpieces display in columns for better space usage
    col1, col2 = st.columns([1, 1])
    
    with col1:
        st.markdown("#### Workpiece List")
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
        st.markdown("#### Working Plane Visualization")
        
        if st.session_state.workpieces:
            # Create and display the canvas
            fig = create_workpiece_canvas(st.session_state.workpieces)
            st.pyplot(fig)
            plt.close(fig)  # Clean up
            
            # Stats
            st.markdown("---")
            st.markdown("##### Statistics")
            total = len(st.session_state.workpieces)
            col_a, col_b = st.columns(2)
            with col_a:
                st.metric("Total", total)
            with col_b:
                states = {}
                for wp in st.session_state.workpieces:
                    state = wp.get('state', 'UNKNOWN')
                    states[state] = states.get(state, 0) + 1
                if states:
                    most_common = max(states, key=states.get)
                    st.metric("Common State", most_common)
        else:
            st.info("No workpieces. Click 'Refresh Workpieces' to load.")
            # Show empty canvas
            fig, ax = plt.subplots(figsize=(10, 7.5))
            ax.set_xlim(-50, 700)
            ax.set_ylim(-50, 530)
            ax.set_aspect('equal')
            ax.set_xlabel('X (mm)', fontsize=10)
            ax.set_ylabel('Y (mm)', fontsize=10)
            ax.set_title('Working Plane (650x480mm) - No Data', fontsize=12)
            ax.grid(True, alpha=0.3, linestyle='--', linewidth=0.5)
            border = Rectangle((0, 0), 650, 480, fill=False, edgecolor='black', linewidth=2)
            ax.add_patch(border)
            ax.text(325, 240, 'No Workpieces\nClick "Refresh Workpieces"', 
                   ha='center', va='center', fontsize=14, color='gray')
            plt.tight_layout()
            st.pyplot(fig)
            plt.close(fig)
    
    st.divider()
    
    # Motion Overrides - Collapsible section (saves vertical space!)
    with st.expander("Motion Overrides (Advanced)", expanded=False):
        st.markdown("**Test one specific motion configuration** (not fallback lists)")
        
        override_enabled = st.checkbox("Override Motions (test one specific configuration)")
        
        if override_enabled:
            st.markdown("##### Pick Parameters")
            col1, col2 = st.columns(2)
            with col1:
                redundancy_options = [
                    "None (no E1 offset)", "E1 = -100°", "E1 = -80°", "E1 = -60°",
                    "E1 = -40°", "E1 = 0°", "E1 = 40°", "E1 = 60°", "E1 = 80°", "E1 = 100°"
                ]
                pick_redundancy = st.selectbox("Redundancy", redundancy_options, index=2)
            with col2:
                pick_alternate = st.radio("Orientation", ["Regular", "Alternate (180°)"], horizontal=True)
            
            st.markdown("##### Place Parameters")
            col1, col2 = st.columns(2)
            with col1:
                place_redundancy = st.selectbox("Redundancy ", redundancy_options, index=7, key="place_red")
            with col2:
                place_zrot = st.number_input("Z-Rotation (degrees)", value=45.0, step=1.0)
            
            if st.button("Apply Motion Override", use_container_width=True):
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
col1, col2 = st.columns([3, 1])
with col1:
    st.caption("KUKA LBR iiwa Robot Control - Streamlit Version | BiemhTek2026")
with col2:
    if st.session_state.connected:
        st.caption(f"Last update: {time.strftime('%H:%M:%S', time.localtime(st.session_state.last_update))}")

# Auto-refresh mechanism for live updates when connected
if st.session_state.connected and st.session_state.auto_refresh:
    current_time = time.time()
    # Only refresh every 2 seconds
    if current_time - st.session_state.last_update >= 2.0:
        st.session_state.last_update = current_time
        # Request status update to get latest data
        send_command({'type': 'get_status'})
        # Small delay to allow response to arrive
        time.sleep(0.2)
        # Trigger rerun to update UI
        st.rerun()
    elif current_time - st.session_state.last_update < 2.0:
        # Wait for next cycle
        remaining = 2.0 - (current_time - st.session_state.last_update)
        time.sleep(min(remaining, 0.5))
        st.rerun()
