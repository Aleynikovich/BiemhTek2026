#!/usr/bin/env python3
"""
KUKA Robot Control GUI
Connects to the robot's console server and allows controlling program execution.
"""

import json
import socket
import threading
import time
import tkinter as tk
from datetime import datetime
from tkinter import ttk, scrolledtext, messagebox


class RobotControlGUI:
    def __init__(self, root):
        self.root = root
        self.root.title("KUKA Robot Control - BiemhTek2026")
        self.root.geometry("900x1000")
        
        # Connection settings
        self.robot_ip = tk.StringVar(value="172.31.1.147")
        self.robot_port = tk.IntVar(value=30001)
        self.connected = False
        self.socket = None
        
        # Reconnection with exponential backoff
        self.INITIAL_RETRY_DELAY = 1.0
        self.MAX_RETRY_DELAY = 60.0
        self.BACKOFF_MULTIPLIER = 2.0
        self.reconnect_thread = None
        self.current_retry_delay = self.INITIAL_RETRY_DELAY
        self.reconnecting = False
        
        # Program state
        self.current_program = tk.IntVar(value=0)
        self.vision_connected = tk.BooleanVar(value=False)
        self.workpiece_position = tk.StringVar(value="Not retrieved")
        
        # Log level filtering
        self.log_level = tk.StringVar(value="INFO")
        self.log_levels = ["DEBUG", "INFO", "WARN", "ERROR"]
        self.log_level_ordinal = {"DEBUG": 0, "INFO": 1, "WARN": 2, "ERROR": 3}
        
        # Auto-refresh settings (disabled by default, starts after connection)
        self.auto_refresh_enabled = False
        self.auto_refresh_interval = 2000  # 2 seconds
        self.auto_refresh_timer = None
        
        self.create_widgets()
        #self.update_connection_status()
        
    def create_widgets(self):
        # Style configuration
        style = ttk.Style()
        style.configure('Title.TLabel', font=('Helvetica', 14, 'bold'))
        style.configure('Header.TLabel', font=('Helvetica', 10, 'bold'))
        style.configure('Status.TLabel', font=('Helvetica', 9))
        
        # Main container
        main_frame = ttk.Frame(self.root, padding="5")
        main_frame.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        
        # Configure grid weights
        self.root.columnconfigure(0, weight=1)
        self.root.rowconfigure(0, weight=1)
        main_frame.columnconfigure(0, weight=1)
        main_frame.rowconfigure(3, weight=1)
        
        # Title
        title_label = ttk.Label(main_frame, text="KUKA LBR iiwa Robot Control", 
                               style='Title.TLabel')
        title_label.grid(row=0, column=0, pady=(0, 10), sticky=(tk.W, tk.E))
        
        # Connection Frame
        self.create_connection_frame(main_frame)
        
        # Status Frame
        self.create_status_frame(main_frame)
        
        # Create tabbed interface for better organization
        self.create_tabbed_interface(main_frame)
        
    def create_connection_frame(self, parent):
        frame = ttk.LabelFrame(parent, text="Connection", padding="5")
        frame.grid(row=1, column=0, sticky=(tk.W, tk.E), pady=(0, 5))
        
        # IP Address
        ttk.Label(frame, text="Robot IP:").grid(row=0, column=0, sticky=tk.W, padx=(0, 5))
        ip_entry = ttk.Entry(frame, textvariable=self.robot_ip, width=20)
        ip_entry.grid(row=0, column=1, sticky=tk.W, padx=(0, 20))
        
        # Port
        ttk.Label(frame, text="Port:").grid(row=0, column=2, sticky=tk.W, padx=(0, 5))
        port_entry = ttk.Entry(frame, textvariable=self.robot_port, width=10)
        port_entry.grid(row=0, column=3, sticky=tk.W, padx=(0, 20))
        
        # Connect/Disconnect buttons
        self.connect_btn = ttk.Button(frame, text="Connect", command=self.connect)
        self.connect_btn.grid(row=0, column=4, padx=5)
        
        self.disconnect_btn = ttk.Button(frame, text="Disconnect", command=self.disconnect, 
                                        state=tk.DISABLED)
        self.disconnect_btn.grid(row=0, column=5, padx=5)
        
        # Status indicator
        self.status_label = ttk.Label(frame, text="● Disconnected", foreground="red")
        self.status_label.grid(row=0, column=6, padx=20)
        
    def create_status_frame(self, parent):
        frame = ttk.LabelFrame(parent, text="Robot Status", padding="5")
        frame.grid(row=2, column=0, sticky=(tk.W, tk.E), pady=(0, 5))
        frame.columnconfigure(1, weight=1)
        
        # Current Program
        ttk.Label(frame, text="Current Program:", style='Status.TLabel').grid(
            row=0, column=0, sticky=tk.W, padx=(0, 10))
        self.current_prog_label = ttk.Label(frame, text="0 - Idle", 
                                           style='Status.TLabel', foreground="blue")
        self.current_prog_label.grid(row=0, column=1, sticky=tk.W)
        
        # Vision Connection
        ttk.Label(frame, text="Vision Server:", style='Status.TLabel').grid(
            row=1, column=0, sticky=tk.W, padx=(0, 10), pady=(5, 0))
        self.vision_status_label = ttk.Label(frame, text="● Disconnected", 
                                            foreground="red")
        self.vision_status_label.grid(row=1, column=1, sticky=tk.W, pady=(5, 0))
        
        # Workpiece Position
        ttk.Label(frame, text="Workpiece Position:", style='Status.TLabel').grid(
            row=2, column=0, sticky=tk.W, padx=(0, 10), pady=(5, 0))
        self.workpiece_label = ttk.Label(frame, textvariable=self.workpiece_position,
                                        style='Status.TLabel')
        self.workpiece_label.grid(row=2, column=1, sticky=tk.W, pady=(5, 0))
        
    def create_tabbed_interface(self, parent):
        """Create tabbed interface for programs, vision, and workpieces"""
        notebook = ttk.Notebook(parent)
        notebook.grid(row=3, column=0, sticky=(tk.W, tk.E, tk.N, tk.S), pady=(0, 5))
        
        # Tab 1: Robot Programs
        programs_tab = ttk.Frame(notebook, padding="5")
        notebook.add(programs_tab, text="Robot Programs")
        self.create_program_control_tab(programs_tab)
        
        # Tab 2: Vision Commands
        vision_tab = ttk.Frame(notebook, padding="5")
        notebook.add(vision_tab, text="Vision Commands")
        self.create_vision_commands_tab(vision_tab)
        
        # Tab 3: Workpieces
        workpieces_tab = ttk.Frame(notebook, padding="5")
        notebook.add(workpieces_tab, text="Workpieces")
        self.create_workpieces_tab(workpieces_tab)
        
        # Tab 4: Console
        console_tab = ttk.Frame(notebook, padding="5")
        notebook.add(console_tab, text="Console")
        self.create_console_tab(console_tab)
    
    def create_program_control_tab(self, parent):
        """Create robot programs control panel"""
        parent.columnconfigure(0, weight=1)
        parent.rowconfigure(1, weight=1)
        
        ttk.Label(parent, text="Robot Motion Programs (1-99):", style='Header.TLabel').grid(
            row=0, column=0, sticky=tk.W, pady=(0, 5))
        
        # Frame for program buttons
        button_frame = ttk.Frame(parent)
        button_frame.grid(row=1, column=0, sticky=(tk.W, tk.E, tk.N), pady=(0, 5))
        
        robot_programs = [
            (0, "Idle"),
            (1, "Pick New Workpiece"),
            (2, "Place New Workpiece"),
            (3, "Pick Measured Workpiece"),
            (4, "Place Measured Workpiece"),
            (5, "Calibration"),
            (6, "Test Calibration"),
        ]
        
        for i, (prog_num, prog_name) in enumerate(robot_programs):
            row = i // 3
            col = i % 3
            btn = ttk.Button(button_frame, text=f"{prog_num}: {prog_name}", 
                           command=lambda p=prog_num: self.set_program(p),
                           width=22)
            btn.grid(row=row, column=col, padx=3, pady=3, sticky=tk.W)
        
        # Quick actions frame
        actions_frame = ttk.LabelFrame(parent, text="Quick Actions", padding="5")
        actions_frame.grid(row=2, column=0, sticky=(tk.W, tk.E), pady=(5, 0))
        
        ttk.Button(actions_frame, text="Emergency Stop (Program 0)", 
                  command=lambda: self.set_program(0),
                  width=22).grid(row=0, column=0, padx=3, pady=3)
        
        ttk.Button(actions_frame, text="Cancel & Return Home", 
                  command=self.cancel_program,
                  width=22).grid(row=0, column=1, padx=3, pady=3)
        
        ttk.Button(actions_frame, text="Get Status", 
                  command=self.get_status,
                  width=22).grid(row=0, column=2, padx=3, pady=3)
    
    def create_vision_commands_tab(self, parent):
        """Create vision commands control panel"""
        parent.columnconfigure(0, weight=1)
        parent.rowconfigure(1, weight=1)
        
        ttk.Label(parent, text="Vision System Commands (100-199):", style='Header.TLabel').grid(
            row=0, column=0, sticky=tk.W, pady=(0, 5))
        
        # Frame for vision buttons
        button_frame = ttk.Frame(parent)
        button_frame.grid(row=1, column=0, sticky=(tk.W, tk.E, tk.N), pady=(0, 5))
        
        vision_commands = [
            (100, "Load References"),
            (101, "Set Auto Mode"),
            (102, "Set Calibration Mode"),
            (103, "Capture Data"),
            (104, "Locate Container"),
            (105, "Get Container Position"),
            (106, "Locate Parts"),
            (107, "Get Part Position"),
            (108, "Get Next Part Position"),
            (109, "Full Scan Sequence"),
            (111, "Get New Workpiece Position (Legacy)"),
        ]
        
        for i, (prog_num, prog_name) in enumerate(vision_commands):
            row = i // 3
            col = i % 3
            btn = ttk.Button(button_frame, text=f"{prog_num}: {prog_name}", 
                           command=lambda p=prog_num: self.set_program(p),
                           width=22)
            btn.grid(row=row, column=col, padx=3, pady=3, sticky=tk.W)
    
    def create_workpieces_tab(self, parent):
        """Create workpiece management tab with 2D visualization"""
        parent.columnconfigure(0, weight=1)
        parent.columnconfigure(1, weight=1)
        parent.rowconfigure(1, weight=1)
        
        ttk.Label(parent, text="Workpiece Database & Visualization:", style='Header.TLabel').grid(
            row=0, column=0, columnspan=2, sticky=tk.W, pady=(0, 5))
        
        # Left side: Treeview for workpieces
        tree_frame = ttk.LabelFrame(parent, text="Workpiece List", padding="5")
        tree_frame.grid(row=1, column=0, sticky=(tk.W, tk.E, tk.N, tk.S), padx=(0, 5))
        tree_frame.columnconfigure(0, weight=1)
        tree_frame.rowconfigure(0, weight=1)
        
        # Scrollbars
        vsb = ttk.Scrollbar(tree_frame, orient="vertical")
        hsb = ttk.Scrollbar(tree_frame, orient="horizontal")
        
        # Treeview
        columns = ("ID", "Ref", "Ori", "State", "Gripper", "X", "Y", "Z", "Rx", "Ry", "Rz", "Score")
        self.workpiece_tree = ttk.Treeview(tree_frame, columns=columns, show='headings',
                                           yscrollcommand=vsb.set, xscrollcommand=hsb.set)
        
        vsb.config(command=self.workpiece_tree.yview)
        hsb.config(command=self.workpiece_tree.xview)
        
        # Column headings
        self.workpiece_tree.heading("ID", text="ID")
        self.workpiece_tree.heading("Ref", text="Ref")
        self.workpiece_tree.heading("Ori", text="Ori")
        self.workpiece_tree.heading("State", text="State")
        self.workpiece_tree.heading("Gripper", text="Gripper")
        self.workpiece_tree.heading("X", text="X (mm)")
        self.workpiece_tree.heading("Y", text="Y (mm)")
        self.workpiece_tree.heading("Z", text="Z (mm)")
        self.workpiece_tree.heading("Rx", text="Rx (deg)")
        self.workpiece_tree.heading("Ry", text="Ry (deg)")
        self.workpiece_tree.heading("Rz", text="Rz (deg)")
        self.workpiece_tree.heading("Score", text="Score")
        
        # Column widths
        self.workpiece_tree.column("ID", width=80)
        self.workpiece_tree.column("Ref", width=40)
        self.workpiece_tree.column("Ori", width=35)
        self.workpiece_tree.column("State", width=80)
        self.workpiece_tree.column("Gripper", width=50)
        self.workpiece_tree.column("X", width=55)
        self.workpiece_tree.column("Y", width=55)
        self.workpiece_tree.column("Z", width=55)
        self.workpiece_tree.column("Rx", width=55)
        self.workpiece_tree.column("Ry", width=55)
        self.workpiece_tree.column("Rz", width=55)
        self.workpiece_tree.column("Score", width=50)
        
        self.workpiece_tree.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        vsb.grid(row=0, column=1, sticky=(tk.N, tk.S))
        hsb.grid(row=1, column=0, sticky=(tk.W, tk.E))
        
        # Right side: 2D visualization canvas
        viz_frame = ttk.LabelFrame(parent, text="Working Plane (700x400mm)", padding="5")
        viz_frame.grid(row=1, column=1, sticky=(tk.W, tk.E, tk.N, tk.S))
        viz_frame.columnconfigure(0, weight=1)
        viz_frame.rowconfigure(0, weight=1)
        
        # Create canvas for 2D visualization
        self.workpiece_canvas = tk.Canvas(viz_frame, bg='white', width=700, height=400)
        self.workpiece_canvas.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        
        # Draw grid
        self.draw_grid()
        
        # Store workpiece data for visualization
        self.workpiece_viz_data = []
        
        # Control buttons
        button_frame = ttk.Frame(parent)
        button_frame.grid(row=2, column=0, columnspan=2, sticky=(tk.W, tk.E), pady=(5, 0))
        
        ttk.Button(button_frame, text="Refresh Workpieces", 
                  command=self.refresh_workpieces,
                  width=18).grid(row=0, column=0, padx=3, pady=3)
        
        ttk.Button(button_frame, text="Get Queue Status", 
                  command=self.get_queue_status,
                  width=18).grid(row=0, column=1, padx=3, pady=3)
        
        ttk.Button(button_frame, text="Clear Queue", 
                  command=self.clear_workpiece_queue,
                  width=18).grid(row=0, column=2, padx=3, pady=3)
        
        ttk.Button(button_frame, text="Delete Selected", 
                  command=self.delete_selected_workpiece,
                  width=18).grid(row=0, column=3, padx=3, pady=3)
    
    def draw_grid(self):
        """Draw grid on workpiece visualization canvas"""
        canvas = self.workpiece_canvas
        
        # Grid parameters (700x400mm working plane)
        width = 700
        height = 400
        grid_size = 50  # 50mm grid
        
        # Draw grid lines
        for x in range(0, width + 1, grid_size):
            canvas.create_line(x, 0, x, height, fill='lightgray', width=1)
        for y in range(0, height + 1, grid_size):
            canvas.create_line(0, y, width, y, fill='lightgray', width=1)
        
        # Draw axes labels
        canvas.create_text(width - 20, height - 10, text="X", fill='black', font=('Arial', 10, 'bold'))
        canvas.create_text(10, 10, text="Y", fill='black', font=('Arial', 10, 'bold'))
        
        # Draw border
        canvas.create_rectangle(1, 1, width - 1, height - 1, outline='black', width=2)
    
    def update_workpiece_visualization(self, workpieces):
        """Update the 2D visualization with workpiece positions"""
        import math
        canvas = self.workpiece_canvas
        
        # Clear previous workpieces (keep grid)
        canvas.delete('workpiece')
        
        # Working plane dimensions (mm) - canvas size
        canvas_width = 700
        canvas_height = 500
        
        # Workpiece dimensions (mm)
        wp_length = 75
        wp_width = 35
        
        # Colors for different references
        ref_colors = {
            1: '#FF0000', # Pure Red
            2: '#800080', # Dark Green
            3: '#0000FF'  # Pure Blue
        }
        state_colors = {
            'AVAILABLE': '',
            'PICKED': 'orange',
            'MEASURING': 'purple',
            'MEASURED': 'blue',
            'RETURNED': 'gray'
        }
        
        for wp in workpieces:
            # Get workpiece position (mm) and rotation (degrees)
            x = float(wp.get('x', 0))
            y = float(wp.get('y', 0))
            rz = float(wp.get('rz', 0))  # Rotation in degrees
            ref = wp.get('reference', 1)
            state = wp.get('state', 'AVAILABLE')
            gripper = wp.get('gripper', '')
            wp_id = wp.get('id', '?')
            
            # Transform robot coordinates to canvas coordinates
            # Robot coordinate system: origin at robot base
            # Assume working area is approximately X: [-350, +350], Y: [-600, -200] 
            # (400mm range centered at Y=-400, which gives Y canvas range for 700x400 canvas)
            # Map X: [-350, +350] -> Canvas X: [0, 700]
            # Map Y: [-600, -200] -> Canvas Y: [0, 400]
            
            # Scale: 1 pixel = 1mm for both axes
            canvas_x = x + 150  # Shift X so -350 maps to 0
            canvas_y = -y - 250  # Flip Y (canvas Y increases downward) and shift so -200 maps to 0
            
            # Skip if outside visible area
            if canvas_x < -100 or canvas_x > canvas_width + 100 or canvas_y < -100 or canvas_y > canvas_height + 100:
                continue
            
            # Get color based on reference and state
            fill_color = ref_colors.get(ref, '#CCCCCC')
            outline_color = state_colors.get(state, 'black')
            outline_width = 3 if state == 'PICKED' else 2
            
            # Draw rotated workpiece rectangle
            # Convert rotation to radians
            angle_rad = math.radians(-rz)
            cos_a = math.cos(angle_rad)
            sin_a = math.sin(angle_rad)
            
            # Define rectangle corners relative to center
            half_length = wp_length / 2
            half_width = wp_width / 2
            corners = [
                (-half_length, -half_width),  # Top-left
                (+half_length, -half_width),  # Top-right
                (+half_length, +half_width),  # Bottom-right
                (-half_length, +half_width),  # Bottom-left
            ]
            
            # Rotate and translate corners
            rotated_corners = []
            for cx, cy in corners:
                # Rotate
                rx = cx * cos_a - cy * sin_a
                ry = cx * sin_a + cy * cos_a
                # Translate to canvas position
                rotated_corners.extend([canvas_x + rx, canvas_y + ry])
            
            # Draw rotated workpiece as polygon
            canvas.create_polygon(rotated_corners,
                                 fill=fill_color,
                                 outline=outline_color,
                                 width=outline_width,
                                 tags='workpiece')
            
            # Draw orientation arrow (X+ tool axis direction)
            # Arrow shows the direction of the gripper's X+ axis based on orientation
            orientation = wp.get('orientation', 0)
            rx = float(wp.get('rx', 0))  # A rotation (around X)
            ry = float(wp.get('ry', 0))  # B rotation (around Y)
            
            # Calculate arrow direction based on rz and orientation
            # Orientation 0 (regular): arrow points along workpiece length
            # Orientation 1 (180deg): arrow points opposite direction
            arrow_length = wp_length / 2
            arrow_angle_deg = -rz + (180 if orientation == 1 else 0)
            arrow_angle_rad = math.radians(arrow_angle_deg)
            
            # Arrow start and end points
            arrow_dx = arrow_length * math.cos(arrow_angle_rad)
            arrow_dy = arrow_length * math.sin(arrow_angle_rad)
            
            # Draw arrow showing X+ tool axis
            canvas.create_line(canvas_x, canvas_y,
                             canvas_x + arrow_dx, canvas_y + arrow_dy,
                             arrow=tk.LAST,
                             fill='yellow',
                             width=2,
                             tags='workpiece')
            
            # Draw revolution circle (projection on plane)
            revolution_radius = wp_length/2 + wp_width/4
            canvas.create_oval(canvas_x - revolution_radius, 
                              canvas_y - revolution_radius,
                              canvas_x + revolution_radius, 
                              canvas_y + revolution_radius,
                              outline='red',
                              dash=(2, 2),
                              width=1,
                              tags='workpiece')
            
            # Add label with ID and gripper location
            label = f"ID:{str(wp_id)[-4:]}"  # Last 4 digits of ID
            if gripper and gripper != 'None':
                label += f"\nG:{gripper}"
            canvas.create_text(canvas_x, canvas_y, 
                             text=label, 
                             fill='white',
                             font=('Arial', 8, 'bold'),
                             tags='workpiece')
        
        # Add legend
        legend_x = 10
        legend_y = 350
        canvas.create_text(legend_x, legend_y, text="Legend:", anchor='w', 
                          font=('Arial', 9, 'bold'), tags='workpiece')
        legend_y += 15
        for ref, color in ref_colors.items():
            canvas.create_rectangle(legend_x, legend_y, legend_x + 15, legend_y + 10, 
                                   fill=color, outline='black', tags='workpiece')
            canvas.create_text(legend_x + 20, legend_y + 5, text=f"Ref {ref}", 
                             anchor='w', font=('Arial', 8), tags='workpiece')
            legend_y += 12
    
    def create_console_tab(self, parent):
        """Create console output tab"""
        parent.columnconfigure(0, weight=1)
        parent.rowconfigure(1, weight=1)
        
        # Log level control frame
        level_frame = ttk.Frame(parent)
        level_frame.grid(row=0, column=0, sticky=(tk.W, tk.E), pady=(0, 5))
        
        ttk.Label(level_frame, text="Minimum Log Level:", 
                 style='Status.TLabel').pack(side=tk.LEFT, padx=(0, 5))
        
        self.log_level_combo = ttk.Combobox(level_frame, textvariable=self.log_level,
                                           values=self.log_levels, width=10, state='readonly')
        self.log_level_combo.pack(side=tk.LEFT, padx=(0, 10))
        self.log_level_combo.bind('<<ComboboxSelected>>', self.on_log_level_changed)
        
        ttk.Label(level_frame, text="(filters logs displayed in console and sent from robot)",
                 font=('Helvetica', 8), foreground='gray').pack(side=tk.LEFT)
        
        ttk.Button(level_frame, text="Pop Out", 
                  command=self.pop_out_console).pack(side=tk.RIGHT, padx=5)
        
        ttk.Button(level_frame, text="Clear Console", 
                  command=self.clear_console).pack(side=tk.RIGHT, padx=5)
        
        # Create console with wrap and make it copyable
        self.console = scrolledtext.ScrolledText(parent, height=20, width=80, 
                                                 wrap=tk.WORD,
                                                 font=('Courier', 9))
        self.console.grid(row=1, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        
        # Make console read-only but allow selection/copying
        # Block regular typing but allow Ctrl+C, Ctrl+A, etc.
        def prevent_edit(event):
            # Allow Ctrl/Cmd key combinations (copy, select all, etc)
            if event.state & 0x4:  # Control key
                return None  # Allow the event
            if event.state & 0x8:  # Alt key  
                return None  # Allow the event
            # Block all other key presses (regular typing)
            return "break"
        
        self.console.bind("<Key>", prevent_edit)
        
        # Configure tags for colored output
        self.console.tag_config('info', foreground='black')
        self.console.tag_config('success', foreground='green')
        self.console.tag_config('error', foreground='red')
        self.console.tag_config('warning', foreground='orange')
        self.console.tag_config('debug', foreground='gray')
        
        # Configure tags for alternating row backgrounds
        self.console.tag_config('row_even', background='white')
        self.console.tag_config('row_odd', background='#f0f0f0')
        
        # Track line count for alternating colors
        self.console_line_count = 0
        
        # Pop-out console window reference
        self.console_popup = None
        self.console_popup_widget = None
    
    def refresh_workpieces(self):
        """Request and refresh workpiece data from robot"""
        if self.send_command({'type': 'get_workpieces'}):
            self.log_console("Requesting workpiece data...", 'info')
    
    def clear_workpiece_queue(self):
        """Clear the workpiece queue on the robot"""
        if messagebox.askyesno("Clear Queue", "Are you sure you want to clear the workpiece queue?"):
            if self.send_command({'type': 'clear_queue'}):
                self.log_console("Workpiece queue cleared", 'info')
                self.workpiece_tree.delete(*self.workpiece_tree.get_children())
    
    def delete_selected_workpiece(self):
        """Delete the selected workpiece from the queue"""
        selection = self.workpiece_tree.selection()
        if not selection:
            messagebox.showwarning("No Selection", "Please select a workpiece to delete")
            return
        
        # Get the selected item values
        item = self.workpiece_tree.item(selection[0])
        values = item['values']
        if not values:
            return
        
        # Extract ID from the first column (may be truncated, need to get from stored data)
        # We need to find the workpiece by matching position or storing full IDs
        # For now, let's refresh workpieces and find by matching other fields
        workpiece_id_str = str(values[0])
        
        # Ask for confirmation
        if messagebox.askyesno("Delete Workpiece", 
                              f"Are you sure you want to delete workpiece ID ending in {workpiece_id_str}?"):
            # We need to find the full ID - let's store it in the tree
            # For now, try to extract from the last data we received
            if hasattr(self, 'last_workpieces_data'):
                for wp in self.last_workpieces_data:
                    wp_id_str = str(wp.get('id', ''))
                    if wp_id_str.endswith(workpiece_id_str) or wp_id_str == workpiece_id_str:
                        if self.send_command({'type': 'delete_workpiece', 'id': wp.get('id')}):
                            self.log_console(f"Deleting workpiece ID {wp.get('id')}", 'info')
                            # Refresh to update display
                            self.refresh_workpieces()
                        return
            
            messagebox.showerror("Error", "Could not find full workpiece ID. Please refresh and try again.")
        
    def log_console(self, message, level='info'):
        """Add message to console with timestamp, filtered by log level"""
        # Map level names
        level_map = {
            'info': 'INFO',
            'success': 'INFO',
            'error': 'ERROR',
            'warning': 'WARN',
            'debug': 'DEBUG'
        }
        
        msg_level = level_map.get(level.lower(), 'INFO')
        current_level = self.log_level.get()
        
        # Filter based on log level ordinal
        if self.log_level_ordinal.get(msg_level, 1) < self.log_level_ordinal.get(current_level, 0):
            return  # Don't display messages below the current log level
        
        timestamp = datetime.now().strftime("%H:%M:%S")
        formatted_line = f"[{timestamp}] {message}\n"
        
        # Add to main console (already in NORMAL state, no need to change)
        # Determine alternating row background
        row_tag = 'row_even' if self.console_line_count % 2 == 0 else 'row_odd'
        self.console_line_count += 1
        
        # Insert with both level color and row background
        line_start = self.console.index(tk.END)
        self.console.insert(tk.END, formatted_line)
        line_end = self.console.index(tk.END)
        
        # Apply tags (row background first, then text color)
        self.console.tag_add(row_tag, line_start, line_end)
        self.console.tag_add(level, line_start, line_end)
        
        # Ensure row background doesn't override text color
        self.console.tag_raise(level)
        
        self.console.see(tk.END)
        
        # Also add to popup console if it exists
        if self.console_popup_widget:
            try:
                popup_line_start = self.console_popup_widget.index(tk.END)
                self.console_popup_widget.insert(tk.END, formatted_line)
                popup_line_end = self.console_popup_widget.index(tk.END)
                self.console_popup_widget.tag_add(row_tag, popup_line_start, popup_line_end)
                self.console_popup_widget.tag_add(level, popup_line_start, popup_line_end)
                self.console_popup_widget.tag_raise(level)
                self.console_popup_widget.see(tk.END)
            except:
                # Popup might have been closed
                self.console_popup_widget = None
        
    def clear_console(self):
        """Clear console output"""
        self.console.delete(1.0, tk.END)
        self.console_line_count = 0  # Reset line counter
        
    def connect(self):
        """Connect to robot console server"""
        try:
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.socket.settimeout(5)
            self.socket.connect((self.robot_ip.get(), self.robot_port.get()))
            self.connected = True
            self.reconnecting = False
            self.current_retry_delay = self.INITIAL_RETRY_DELAY
            
            self.connect_btn.config(state=tk.DISABLED)
            self.disconnect_btn.config(state=tk.NORMAL)
            self.status_label.config(text="● Connected", foreground="green")
            
            self.log_console(f"Connected to robot at {self.robot_ip.get()}:{self.robot_port.get()}", 
                           'success')
            
            # Start listening thread
            self.listen_thread = threading.Thread(target=self.listen_to_robot, daemon=True)
            self.listen_thread.start()
            
            # Send initial log level to robot
            self.on_log_level_changed()
            
            # Start auto-refresh after a longer delay (3 seconds to ensure stability)
            self.log_console("Auto-refresh will start in 3 seconds...", 'info')
            self.root.after(500, self.start_auto_refresh)
            
        except Exception as e:
            messagebox.showerror("Connection Error", f"Failed to connect: {str(e)}")
            self.log_console(f"Connection failed: {str(e)}", 'error')
            if self.socket:
                self.socket.close()
                
    def disconnect(self):
        """Disconnect from robot"""
        self.reconnecting = False  # Stop any reconnection attempts
        self.connected = False
        
        # Stop auto-refresh
        self.stop_auto_refresh()
        
        if self.socket:
            try:
                self.socket.close()
            except:
                pass
            self.socket = None
            
        self.connect_btn.config(state=tk.NORMAL)
        self.disconnect_btn.config(state=tk.DISABLED)
        self.status_label.config(text="● Disconnected", foreground="red")
        self.log_console("Disconnected from robot", 'warning')
        self.current_retry_delay = self.INITIAL_RETRY_DELAY  # Reset for next connection
        
    def send_command(self, command):
        """Send command to robot"""
        if not self.connected or not self.socket:
            messagebox.showwarning("Not Connected", "Please connect to the robot first")
            return False
            
        try:
            import json as json_module  # Explicit import to avoid shadowing
            cmd_json = json_module.dumps(command) + "\n"
            self.socket.sendall(cmd_json.encode('utf-8'))
            self.log_console(f"Sent: {command}", 'info')
            return True
        except Exception as e:
            self.log_console(f"Send error: {str(e)}", 'error')
            self.disconnect()
            return False
            
    def listen_to_robot(self):
        """Listen for responses from robot"""
        buffer = ""
        while self.connected:
            try:
                data = self.socket.recv(1024).decode('utf-8')
                if not data:
                    # Connection closed by server
                    self.on_disconnect()
                    self.start_reconnect()
                    break
                    
                buffer += data
                while '\n' in buffer:
                    line, buffer = buffer.split('\n', 1)
                    if line.strip():
                        self.handle_response(line)
                        
            except socket.timeout:
                continue
            except Exception as e:
                if self.connected:
                    self.log_console(f"Listen error: {str(e)}", 'error')
                    self.on_disconnect()
                    self.start_reconnect()
                break
            
    def on_disconnect(self):
        """Handle disconnection"""
        self.connected = False
        if self.socket:
            try:
                self.socket.close()
            except:
                pass
        self.root.after(0, lambda: self.status_label.config(text="● Disconnected", foreground="red"))
    
    def start_reconnect(self):
        """Start reconnection thread with exponential backoff"""
        if self.reconnecting:
            return  # Already reconnecting
            
        self.reconnecting = True
        self.root.after(0, lambda: self.status_label.config(text="● Reconnecting...", foreground="orange"))
        
        # Start reconnection thread
        self.reconnect_thread = threading.Thread(target=self.reconnect_loop, daemon=True)
        self.reconnect_thread.start()
    
    def reconnect_loop(self):
        """Reconnection loop with exponential backoff"""
        while self.reconnecting and not self.connected:
            self.log_console(f"Reconnecting in {self.current_retry_delay:.1f} seconds...", 'info')
            time.sleep(self.current_retry_delay)
            
            try:
                # Close old socket before creating new one
                if self.socket:
                    try:
                        self.socket.close()
                    except:
                        pass
                
                self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.socket.settimeout(5)
                self.socket.connect((self.robot_ip.get(), self.robot_port.get()))
                self.connected = True
                self.reconnecting = False
                self.current_retry_delay = self.INITIAL_RETRY_DELAY  # Reset backoff
                
                self.root.after(0, lambda: self.status_label.config(text="● Connected", foreground="green"))
                self.log_console("Reconnected successfully!", 'success')
                
                # Restart listening thread
                self.listen_thread = threading.Thread(target=self.listen_to_robot, daemon=True)
                self.listen_thread.start()
                
                # Resend log level
                self.on_log_level_changed()
                
            except Exception as e:
                # Exponential backoff
                self.current_retry_delay = min(
                    self.current_retry_delay * self.BACKOFF_MULTIPLIER,
                    self.MAX_RETRY_DELAY
                )
                self.log_console(f"Reconnection failed: {str(e)}", 'error')
    
    def handle_response(self, response):
        """Handle response from robot"""
        import json as json_module  # Explicit import to avoid shadowing
        try:
            data = json_module.loads(response)
            
            response_type = data.get('type')
            
            # Handle different response types
            if response_type == 'status':
                # Silent - cyclic auto-refresh message
                self.update_status(data)
            elif response_type == 'queue_status':
                # User-requested, show in console
                status = data.get('status', 'No status available')
                self.log_console("Queue Status:\n" + status, 'info')
            elif response_type == 'workpieces':
                # Silent - cyclic auto-refresh message
                # Parse workpieces JSON string
                workpieces_json = data.get('workpieces', '[]')
                if isinstance(workpieces_json, str):
                    workpieces = json_module.loads(workpieces_json)
                else:
                    workpieces = workpieces_json
                self.update_workpiece_display(workpieces)
            elif response_type == 'log':
                # Robot log messages - always show
                level = data.get('level', 'info').lower()
                message = data.get('message', '')
                self.log_console(f"[ROBOT] {message}", level)
            elif response_type == 'log_level':
                # User-requested log level change
                current_level = data.get('level', 'DEBUG')
                self.log_console(f"Log level changed to {current_level}", 'info')
            elif response_type == 'response':
                # Generic response - show it
                self.log_console(f"Response: {data.get('message', '')}", 'success')
                
        except json_module.JSONDecodeError:
            # Handle non-JSON log entries (from NetworkListener)
            self.parse_log_entry(response)
    
    def update_workpiece_display(self, workpieces):
        """Update the workpiece treeview and 2D visualization with data from robot"""
        # Store for deletion functionality
        self.last_workpieces_data = workpieces
        
        # Clear existing items
        self.workpiece_tree.delete(*self.workpiece_tree.get_children())
        
        # Add workpieces to tree
        for wp in workpieces:
            # Get orientation arrow symbol
            orientation = wp.get('orientation', 0)
            ori_symbol = "→" if orientation == 0 else "↻"
            
            # Get reference string if available, otherwise use reference index
            ref_str = wp.get('referenceString', str(wp.get('reference', 'N/A')))
            
            self.workpiece_tree.insert('', 'end', values=(
                str(wp.get('id', 'N/A'))[-8:],  # Last 8 digits of ID
                ref_str,
                ori_symbol,
                wp.get('state', 'N/A'),
                wp.get('gripper', 'N/A'),
                f"{wp.get('x', 0):.1f}",
                f"{wp.get('y', 0):.1f}",
                f"{wp.get('z', 0):.1f}",
                f"{wp.get('rx', 0):.1f}",
                f"{wp.get('ry', 0):.1f}",
                f"{wp.get('rz', 0):.1f}",
                f"{wp.get('score', 0):.2f}"
            ))
        
        # Update 2D visualization
        self.update_workpiece_visualization(workpieces)
    
    def parse_log_entry(self, log_line):
        """Parse log entry from NetworkListener format: [HH:MM:SS.mmm] Source | LEVEL: message"""
        import re
        # Match pattern: [timestamp] source | LEVEL: message
        pattern = r'\[([^\]]+)\]\s+([^\|]+)\|\s*(\w+)\s*:\s*(.*)'
        match = re.match(pattern, log_line)
        
        if match:
            timestamp, source, level, message = match.groups()
            level_lower = level.strip().lower()
            # Map log levels to console tags
            if level_lower == 'debug':
                tag = 'debug'
            elif level_lower == 'info':
                tag = 'info'
            elif level_lower == 'warn':
                tag = 'warning'
            elif level_lower == 'error':
                tag = 'error'
            else:
                tag = 'info'
            
            self.log_console(f"[ROBOT] {source.strip()}: {message}", tag)
        else:
            # Fallback for unparseable log lines
            self.log_console(f"[ROBOT] {log_line}", 'info')
            
    def update_status(self, data):
        """Update status display from robot data"""
        if 'program' in data:
            prog_num = data['program']
            self.current_program.set(prog_num)
            # Updated program names for new numbering
            if prog_num == 0:
                prog_name = "Idle"
            elif prog_num <= 6:
                prog_names = ["", "Pick New", "Place New", "Pick Measured", "Place Measured", "Calibration", "Test Calib"]
                prog_name = prog_names[prog_num]
            elif 100 <= prog_num <= 199:
                prog_name = f"Vision Task"
            else:
                prog_name = "Unknown"
            self.current_prog_label.config(text=f"{prog_num} - {prog_name}")
            
        if 'vision_connected' in data:
            connected = data['vision_connected']
            self.vision_connected.set(connected)
            status = "● Connected" if connected else "● Disconnected"
            color = "green" if connected else "red"
            self.vision_status_label.config(text=status, foreground=color)
            
        if 'workpiece_position' in data:
            pos = data['workpiece_position']
            if pos and pos != "invalid":
                self.workpiece_position.set(pos)
            else:
                self.workpiece_position.set("Not retrieved")
                
    def set_program(self, program_number):
        """Set robot program number"""
        command = {
            'type': 'set_program',
            'program': program_number
        }
        if self.send_command(command):
            self.log_console(f"Setting program to {program_number}", 'success')
    
    def cancel_program(self):
        """Cancel current program and return home without opening grippers"""
        command = {'type': 'cancel_program'}
        if self.send_command(command):
            self.log_console("Cancelling program - robot will return home without opening grippers", 'warning')
            
    def get_status(self):
        """Request status from robot"""
        command = {'type': 'get_status'}
        self.send_command(command)
    
    def get_queue_status(self):
        """Request queue status from robot"""
        command = {'type': 'get_queue_status'}
        if self.send_command(command):
            self.log_console("Requesting queue status...", 'info')
    
    def on_log_level_changed(self, event=None):
        """Handle log level selection change"""
        new_level = self.log_level.get()
        self.log_console(f"Log level changed to {new_level}", 'info')
        
        # Send log level change to robot if connected
        if self.connected:
            command = {
                'type': 'set_log_level',
                'level': new_level
            }
            self.send_command(command)
        
    def update_connection_status(self):
        """Periodic update of connection status"""
        if self.connected:
            # Periodically request status
            if hasattr(self, 'status_counter'):
                self.status_counter += 1
                if self.status_counter >= 10:  # Every 10 seconds
                    self.get_status()
                    self.status_counter = 0
            else:
                self.status_counter = 0
                
        self.root.after(1000, self.update_connection_status)
    
    def start_auto_refresh(self):
        """Start auto-refresh of status and workpieces"""
        if self.connected:
            self.auto_refresh_enabled = True
            self.auto_refresh_data()
            self.log_console("Auto-refresh started", 'info')
    
    def auto_refresh_data(self):
        """Auto-refresh status and workpieces without logging each request"""
        # Only continue if still connected and enabled
        if not self.connected or not self.auto_refresh_enabled or not self.socket:
            self.stop_auto_refresh()
            return
            
        # Silently request status and workpieces
        try:
            # Double-check socket is still valid
            if self.socket.fileno() == -1:
                raise Exception("Socket closed")
                
            # Send commands without logging
            import json as json_module  # Explicit import to avoid any shadowing
            cmd1 = json_module.dumps({'type': 'get_status'}) + "\n"
            cmd2 = json_module.dumps({'type': 'get_workpieces'}) + "\n"
            
            self.socket.sendall(cmd1.encode('utf-8'))
            time.sleep(0.1)  # Increased delay between commands
            self.socket.sendall(cmd2.encode('utf-8'))
        except Exception as e:
            # If error, stop auto-refresh and log
            self.log_console(f"Auto-refresh error: {str(e)}", 'error')
            self.stop_auto_refresh()
            # Don't disconnect here - let the listen thread handle it
            return
        
        # Schedule next refresh
        if self.auto_refresh_enabled:
            self.auto_refresh_timer = self.root.after(self.auto_refresh_interval, self.auto_refresh_data)
    
    def stop_auto_refresh(self):
        """Stop auto-refresh"""
        self.auto_refresh_enabled = False
        if self.auto_refresh_timer:
            self.root.after_cancel(self.auto_refresh_timer)
            self.auto_refresh_timer = None
    
    def pop_out_console(self):
        """Pop out console into a separate window"""
        if self.console_popup and tk.Toplevel.winfo_exists(self.console_popup):
            # Already popped out, bring to front
            self.console_popup.lift()
            return
        
        # Create popup window
        self.console_popup = tk.Toplevel(self.root)
        self.console_popup.title("Robot Console")
        self.console_popup.geometry("900x600")
        
        # Create frame
        popup_frame = ttk.Frame(self.console_popup, padding="5")
        popup_frame.pack(fill=tk.BOTH, expand=True)
        
        # Control frame
        control_frame = ttk.Frame(popup_frame)
        control_frame.pack(fill=tk.X, pady=(0, 5))
        
        ttk.Label(control_frame, text="Console Output (Pop-out)", 
                 font=('Helvetica', 10, 'bold')).pack(side=tk.LEFT)
        
        ttk.Button(control_frame, text="Clear", 
                  command=self.clear_console).pack(side=tk.RIGHT, padx=5)
        
        # Create console text widget in popup (copyable)
        popup_console = scrolledtext.ScrolledText(popup_frame, 
                                                   wrap=tk.WORD,
                                                   font=('Courier', 9))
        popup_console.pack(fill=tk.BOTH, expand=True)
        
        # Make console read-only but allow selection/copying
        # Block regular typing but allow Ctrl+C, Ctrl+A, etc.
        def prevent_popup_edit(event):
            # Allow Ctrl/Cmd key combinations (copy, select all, etc)
            if event.state & 0x4:  # Control key
                return None  # Allow the event
            if event.state & 0x8:  # Alt key  
                return None  # Allow the event
            # Block all other key presses (regular typing)
            return "break"
        
        popup_console.bind("<Key>", prevent_popup_edit)
        
        # Copy tags
        for tag in ['info', 'success', 'error', 'warning', 'debug', 'row_even', 'row_odd']:
            tag_config_fg = self.console.tag_cget(tag, 'foreground')
            if tag_config_fg:
                popup_console.tag_config(tag, foreground=tag_config_fg)
            tag_config_bg = self.console.tag_cget(tag, 'background')
            if tag_config_bg:
                popup_console.tag_config(tag, background=tag_config_bg)
        
        # Copy existing content
        popup_console.insert('1.0', self.console.get('1.0', tk.END))
        
        # Store reference to popup console
        self.console_popup_widget = popup_console
        
        # Handle close
        def on_popup_close():
            self.console_popup_widget = None
            self.console_popup.destroy()
            self.console_popup = None
        
        self.console_popup.protocol("WM_DELETE_WINDOW", on_popup_close)


def main():
    root = tk.Tk()
    app = RobotControlGUI(root)
    
    # Handle window close
    def on_closing():
        if app.connected:
            app.disconnect()
        root.destroy()
        
    root.protocol("WM_DELETE_WINDOW", on_closing)
    root.mainloop()


if __name__ == "__main__":
    main()
