#!/usr/bin/env python3
"""
KUKA Robot Control GUI - Refactored
Connects to the robot's console server and allows controlling program execution.
"""

import json
import re
import tkinter as tk
from tkinter import ttk, scrolledtext, messagebox

from modules.gripper_panel import GripperPanel
from modules.log_manager import LogManager
from modules.robot_client import RobotClient
from modules.workpiece_canvas import WorkpieceCanvas
from modules.workpiece_manager import WorkpieceManager


class RobotControlGUI:
    def __init__(self, root):
        self.root = root
        self.root.title("KUKA Robot Control - BiemhTek2026")
        self.root.geometry("900x1000")

        # UI State Variables
        self.robot_ip = tk.StringVar(value="172.31.1.147")
        self.robot_port = tk.IntVar(value=30001)
        self.current_program = tk.IntVar(value=0)
        self.vision_connected = tk.BooleanVar(value=False)
        self.workpiece_position = tk.StringVar(value="Not retrieved")
        self.log_level = tk.StringVar(value="INFO")

        # Auto-refresh Settings
        self.auto_refresh_enabled = False
        self.auto_refresh_interval = 2000
        self.auto_refresh_timer = None

        # Components (Initialized after widgets)
        self.client = None
        self.log_manager = None
        self.wp_manager = None
        
        self.create_widgets()
        self._initialize_components()

    def _initialize_components(self):
        self.log_manager = LogManager(self.console, self.log_level)
        self.wp_manager = WorkpieceManager(self.workpiece_tree, self.workpiece_canvas,
                                           getattr(self, 'gripper_panel', None))
        
    def create_widgets(self):
        style = ttk.Style()
        try:
            style.theme_use('clam')
        except Exception:
            pass
        style.configure('Title.TLabel', font=('Helvetica', 14, 'bold'))
        style.configure('Header.TLabel', font=('Helvetica', 10, 'bold'))
        style.configure('Status.TLabel', font=('Helvetica', 9))
        style.configure('Accent.TButton', padding=5)
        
        main_frame = ttk.Frame(self.root, padding="5")
        main_frame.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        self.root.columnconfigure(0, weight=1)
        self.root.rowconfigure(0, weight=1)
        main_frame.columnconfigure(0, weight=1)
        main_frame.rowconfigure(3, weight=1)

        ttk.Label(main_frame, text="KUKA LBR iiwa Robot Control", style='Title.TLabel').grid(row=0, column=0,
                                                                                             pady=(0, 10),
                                                                                             sticky=(tk.W, tk.E))
        
        self.create_connection_frame(main_frame)
        self.create_status_frame(main_frame)
        self.create_tabbed_interface(main_frame)

    def create_connection_frame(self, parent):
        frame = ttk.LabelFrame(parent, text="Connection", padding="5")
        frame.grid(row=1, column=0, sticky=(tk.W, tk.E), pady=(0, 5))
        
        ttk.Label(frame, text="Robot IP:").grid(row=0, column=0, sticky=tk.W, padx=(0, 5))
        ttk.Entry(frame, textvariable=self.robot_ip, width=20).grid(row=0, column=1, sticky=tk.W, padx=(0, 20))
        
        ttk.Label(frame, text="Port:").grid(row=0, column=2, sticky=tk.W, padx=(0, 5))
        ttk.Entry(frame, textvariable=self.robot_port, width=10).grid(row=0, column=3, sticky=tk.W, padx=(0, 20))
        
        self.connect_btn = ttk.Button(frame, text="Connect", command=self.connect)
        self.connect_btn.grid(row=0, column=4, padx=5)

        self.disconnect_btn = ttk.Button(frame, text="Disconnect", command=self.disconnect, state=tk.DISABLED)
        self.disconnect_btn.grid(row=0, column=5, padx=5)
        
        self.status_label = ttk.Label(frame, text="● Disconnected", foreground="red")
        self.status_label.grid(row=0, column=6, padx=20)

    def create_status_frame(self, parent):
        frame = ttk.LabelFrame(parent, text="Robot Status", padding="5")
        frame.grid(row=2, column=0, sticky=(tk.W, tk.E), pady=(0, 5))
        frame.columnconfigure(1, weight=1)

        ttk.Label(frame, text="Current Program:", style='Status.TLabel').grid(row=0, column=0, sticky=tk.W,
                                                                              padx=(0, 10))
        self.current_prog_label = ttk.Label(frame, text="0 - Idle", style='Status.TLabel', foreground="blue")
        self.current_prog_label.grid(row=0, column=1, sticky=tk.W)

        ttk.Label(frame, text="Vision Server:", style='Status.TLabel').grid(row=1, column=0, sticky=tk.W, padx=(0, 10),
                                                                            pady=(5, 0))
        self.vision_status_label = ttk.Label(frame, text="● Disconnected", foreground="red")
        self.vision_status_label.grid(row=1, column=1, sticky=tk.W, pady=(5, 0))

        ttk.Label(frame, text="Workpiece Position:", style='Status.TLabel').grid(row=2, column=0, sticky=tk.W,
                                                                                 padx=(0, 10), pady=(5, 0))
        ttk.Label(frame, textvariable=self.workpiece_position, style='Status.TLabel').grid(row=2, column=1, sticky=tk.W,
                                                                                           pady=(5, 0))

    def create_tabbed_interface(self, parent):
        notebook = ttk.Notebook(parent)
        notebook.grid(row=3, column=0, sticky=(tk.W, tk.E, tk.N, tk.S), pady=(0, 5))

        tabs = [
            ("Robot Programs", self.create_program_control_tab),
            ("Vision Commands", self.create_vision_commands_tab),
            ("Workpieces", self.create_workpieces_tab),
            ("Console", self.create_console_tab)
        ]
        for name, creator in tabs:
            tab = ttk.Frame(notebook, padding="5")
            notebook.add(tab, text=name)
            creator(tab)

    def create_program_control_tab(self, parent):
        parent.columnconfigure(0, weight=1)
        ttk.Label(parent, text="Robot Motion Programs (1-99):", style='Header.TLabel').grid(row=0, column=0,
                                                                                            sticky=tk.W, pady=(0, 5))

        btn_frame = ttk.Frame(parent)
        btn_frame.grid(row=1, column=0, sticky=(tk.W, tk.E, tk.N), pady=(0, 5))

        programs = [
            (0, "Idle"), (1, "Pick New Workpiece"), (2, "Place New Workpiece"),
            (3, "Pick Measured Workpiece"), (4, "Place Measured Workpiece"),
            (5, "Calibration"), (6, "Test Calibration"),
        ]
        for i, (num, name) in enumerate(programs):
            ttk.Button(btn_frame, text=f"{num}: {name}", command=lambda p=num: self.set_program(p), width=22).grid(
                row=i // 3, column=i % 3, padx=3, pady=3, sticky=tk.W)
        
        actions_frame = ttk.LabelFrame(parent, text="Quick Actions", padding="5")
        actions_frame.grid(row=2, column=0, sticky=(tk.W, tk.E), pady=(5, 0))

        actions = [
            ("Emergency Stop (Program 0)", lambda: self.set_program(0)),
            ("Cancel & Return Home", self.cancel_program),
            ("Get Status", self.get_status)
        ]
        for i, (text, cmd) in enumerate(actions):
            ttk.Button(actions_frame, text=text, command=cmd, width=22).grid(row=0, column=i, padx=3, pady=3)

    def create_vision_commands_tab(self, parent):
        parent.columnconfigure(0, weight=1)
        ttk.Label(parent, text="Vision System Commands (100-199):", style='Header.TLabel').grid(row=0, column=0,
                                                                                                sticky=tk.W,
                                                                                                pady=(0, 5))

        btn_frame = ttk.Frame(parent)
        btn_frame.grid(row=1, column=0, sticky=(tk.W, tk.E, tk.N), pady=(0, 5))

        cmds = [
            (100, "Load References"), (101, "Set Auto Mode"), (102, "Set Calibration Mode"),
            (103, "Capture Data"), (104, "Locate Container"), (105, "Get Container Position"),
            (106, "Locate Parts"), (107, "Get Part Position"), (108, "Get Next Part Position"),
            (109, "Full Scan Sequence"), (111, "Get New Workpiece Position (Legacy)"),
        ]
        for i, (num, name) in enumerate(cmds):
            ttk.Button(btn_frame, text=f"{num}: {name}", command=lambda p=num: self.set_program(p), width=22).grid(
                row=i // 3, column=i % 3, padx=3, pady=3, sticky=tk.W)

    def create_workpieces_tab(self, parent):
        parent.columnconfigure(0, weight=1)
        parent.columnconfigure(1, weight=1)
        parent.rowconfigure(1, weight=1)
        ttk.Label(parent, text="Workpiece Database & Visualization:", style='Header.TLabel').grid(row=0, column=0,
                                                                                                  columnspan=2,
                                                                                                  sticky=tk.W,
                                                                                                  pady=(0, 5))
        
        tree_frame = ttk.LabelFrame(parent, text="Workpiece List", padding="5")
        tree_frame.grid(row=1, column=0, sticky=(tk.W, tk.E, tk.N, tk.S), padx=(0, 5))
        tree_frame.columnconfigure(0, weight=1)
        tree_frame.rowconfigure(0, weight=1)
        
        vsb = ttk.Scrollbar(tree_frame, orient="vertical")
        hsb = ttk.Scrollbar(tree_frame, orient="horizontal")

        cols = ("ID", "Ref", "Ori", "State", "Gripper", "X", "Y", "Z", "Rx", "Ry", "Rz", "Score")
        self.workpiece_tree = ttk.Treeview(tree_frame, columns=cols, show='headings', yscrollcommand=vsb.set,
                                           xscrollcommand=hsb.set)
        vsb.config(command=self.workpiece_tree.yview)
        hsb.config(command=self.workpiece_tree.xview)

        col_configs = {
            "ID": 80, "Ref": 40, "Ori": 35, "State": 80, "Gripper": 50,
            "X": 55, "Y": 55, "Z": 55, "Rx": 55, "Ry": 55, "Rz": 55, "Score": 50
        }
        for c, w in col_configs.items():
            self.workpiece_tree.heading(c, text=c if c not in ["X", "Y", "Z", "Rx", "Ry",
                                                               "Rz"] else f"{c} ({'mm' if len(c) == 1 else 'deg'})")
            self.workpiece_tree.column(c, width=w)
            
        self.workpiece_tree.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        vsb.grid(row=0, column=1, sticky=(tk.N, tk.S))
        hsb.grid(row=1, column=0, sticky=(tk.W, tk.E))
        
        viz_frame = ttk.LabelFrame(parent, text="Working Plane (700x400mm)", padding="5")
        viz_frame.grid(row=1, column=1, sticky=(tk.W, tk.E, tk.N, tk.S))
        viz_frame.columnconfigure(0, weight=1)
        viz_frame.rowconfigure(0, weight=1)
        viz_frame.rowconfigure(1, weight=0)

        self.workpiece_canvas = WorkpieceCanvas(viz_frame, width=700, height=400)
        self.workpiece_canvas.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        # Gripper digital twin panel
        self.gripper_panel = GripperPanel(viz_frame)
        self.gripper_panel.grid(row=1, column=0, sticky=(tk.W, tk.E), pady=(6, 0))

        btn_frame = ttk.Frame(parent)
        btn_frame.grid(row=2, column=0, columnspan=2, sticky=(tk.W, tk.E), pady=(5, 0))

        actions = [
            ("Refresh Workpieces", self.refresh_workpieces),
            ("Get Queue Status", self.get_queue_status),
            ("Clear Queue", self.clear_workpiece_queue),
            ("Delete Selected", self.delete_selected_workpiece),
            ("Pick Selected", self.pick_selected_workpiece),
        ]
        for i, (text, cmd) in enumerate(actions):
            ttk.Button(btn_frame, text=text, command=cmd, width=18).grid(row=0, column=i, padx=3, pady=3)
        
        # Motion Override Controls
        override_frame = ttk.LabelFrame(parent, text="Motion Overrides (Advanced)", padding="10")
        override_frame.grid(row=3, column=0, columnspan=2, sticky=(tk.W, tk.E), pady=(5, 0))
        override_frame.columnconfigure(0, weight=1)
        
        # Master enable checkbox
        self.motion_override_enabled = tk.BooleanVar(value=False)
        enable_cb = ttk.Checkbutton(override_frame, text="Enable Motion Overrides", 
                                     variable=self.motion_override_enabled,
                                     command=self.on_motion_override_enabled_changed)
        enable_cb.grid(row=0, column=0, sticky=tk.W, pady=(0, 10))
        
        # Pick Motion Section
        pick_frame = ttk.LabelFrame(override_frame, text="Pick Motion", padding="5")
        pick_frame.grid(row=1, column=0, sticky=(tk.W, tk.E), pady=(0, 10))
        pick_frame.columnconfigure(1, weight=1)
        
        ttk.Label(pick_frame, text="Redundancy Offsets:").grid(row=0, column=0, sticky=tk.W, padx=(0, 5))
        self.pick_redundancy_var = tk.StringVar(value="Default (-80,80,-60,60)")
        self.pick_redundancy_combo = ttk.Combobox(pick_frame, textvariable=self.pick_redundancy_var,
                                                   values=[
                                                       "Default (-80,80,-60,60)",
                                                       "Narrow (-40,40,-30,30)",
                                                       "Wide (-100,100,-80,80)",
                                                       "Single (0)",
                                                       "None"
                                                   ], state='readonly', width=35)
        self.pick_redundancy_combo.grid(row=0, column=1, sticky=(tk.W, tk.E), padx=(0, 5))
        
        self.pick_alternate_only = tk.BooleanVar(value=False)
        ttk.Checkbutton(pick_frame, text="Alternate position only (180°)", 
                        variable=self.pick_alternate_only).grid(row=1, column=0, columnspan=2, sticky=tk.W, pady=(5, 0))
        
        # Place Motion Section
        place_frame = ttk.LabelFrame(override_frame, text="Place Motion", padding="5")
        place_frame.grid(row=2, column=0, sticky=(tk.W, tk.E), pady=(0, 10))
        place_frame.columnconfigure(1, weight=1)
        
        ttk.Label(place_frame, text="Redundancy Offsets:").grid(row=0, column=0, sticky=tk.W, padx=(0, 5))
        self.place_redundancy_var = tk.StringVar(value="Default (-80,80,-60,60)")
        self.place_redundancy_combo = ttk.Combobox(place_frame, textvariable=self.place_redundancy_var,
                                                    values=[
                                                        "Default (-80,80,-60,60)",
                                                        "Narrow (-40,40,-30,30)",
                                                        "Wide (-100,100,-80,80)",
                                                        "Single (0)",
                                                        "None"
                                                    ], state='readonly', width=35)
        self.place_redundancy_combo.grid(row=0, column=1, sticky=(tk.W, tk.E), padx=(0, 5))
        
        ttk.Label(place_frame, text="Z-Rotation Angles:").grid(row=1, column=0, sticky=tk.W, padx=(0, 5), pady=(5, 0))
        self.place_zrot_var = tk.StringVar(value="Default (90,45,0,135,180,-45,-90,-135)")
        self.place_zrot_combo = ttk.Combobox(place_frame, textvariable=self.place_zrot_var,
                                              values=[
                                                  "Default (90,45,0,135,180,-45,-90,-135)",
                                                  "Cardinal (0,90,180,-90)",
                                                  "Fine (0,15,30,45,60,75,90)",
                                                  "Single (0)",
                                                  "Single (90)"
                                              ], state='readonly', width=35)
        self.place_zrot_combo.grid(row=1, column=1, sticky=(tk.W, tk.E), padx=(0, 5), pady=(5, 0))
        
        self.place_alternate_only = tk.BooleanVar(value=False)
        ttk.Checkbutton(place_frame, text="Alternate position only (180°)", 
                        variable=self.place_alternate_only).grid(row=2, column=0, columnspan=2, sticky=tk.W, pady=(5, 0))
        
        # Apply button
        self.apply_overrides_btn = ttk.Button(override_frame, text="Apply Overrides", 
                                               command=self.apply_motion_overrides, width=20, state=tk.DISABLED)
        self.apply_overrides_btn.grid(row=3, column=0, pady=(5, 0))


    def create_console_tab(self, parent):
        parent.columnconfigure(0, weight=1)
        parent.rowconfigure(1, weight=1)
        
        level_frame = ttk.Frame(parent)
        level_frame.grid(row=0, column=0, sticky=(tk.W, tk.E), pady=(0, 5))

        ttk.Label(level_frame, text="Minimum Log Level:", style='Status.TLabel').pack(side=tk.LEFT, padx=(0, 5))
        self.log_level_combo = ttk.Combobox(level_frame, textvariable=self.log_level,
                                            values=["DEBUG", "INFO", "WARN", "ERROR"], width=10, state='readonly')
        self.log_level_combo.pack(side=tk.LEFT, padx=(0, 10))
        self.log_level_combo.bind('<<ComboboxSelected>>', self.on_log_level_changed)

        ttk.Label(level_frame, text="(filters logs displayed in console)", font=('Helvetica', 8),
                  foreground='gray').pack(side=tk.LEFT)
        ttk.Button(level_frame, text="Pop Out", command=lambda: self.log_manager.pop_out(self.root)).pack(side=tk.RIGHT,
                                                                                                          padx=5)
        ttk.Button(level_frame, text="Clear Console", command=lambda: self.log_manager.clear()).pack(side=tk.RIGHT,
                                                                                                     padx=5)

        self.console = scrolledtext.ScrolledText(parent, height=20, width=80, wrap=tk.WORD, font=('Courier', 9))
        self.console.grid(row=1, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        
        def prevent_edit(event):
            if event.state & 0x4 or event.state & 0x8: return None
            return "break"
        self.console.bind("<Key>", prevent_edit)

    # Robot Communication Handlers
    def connect(self):
        try:
            self.client = RobotClient(
                self.robot_ip.get(), self.robot_port.get(),
                self.handle_response, self.on_connect, self.on_disconnect
            )
            self.client.connect()
        except Exception as e:
            messagebox.showerror("Connection Error", f"Failed to connect: {str(e)}")
            self.log_manager.log(f"Connection failed: {str(e)}", 'error')

    def disconnect(self):
        self.stop_auto_refresh()
        if self.client:
            self.client.disconnect()

    def on_connect(self, reconnected=False):
        self.connect_btn.config(state=tk.DISABLED)
        self.disconnect_btn.config(state=tk.NORMAL)
        self.status_label.config(text="● Connected", foreground="green")

        msg = "Reconnected successfully!" if reconnected else f"Connected to robot at {self.robot_ip.get()}:{self.robot_port.get()}"
        self.log_manager.log(msg, 'success')

        self.on_log_level_changed()
        if not reconnected:
            self.root.after(3000, self.start_auto_refresh)

    def on_disconnect(self):
        self.status_label.config(text="● Disconnected", foreground="red")
        if not self.client or not self.client.reconnecting:
            self.connect_btn.config(state=tk.NORMAL)
            self.disconnect_btn.config(state=tk.DISABLED)
            self.log_manager.log("Disconnected from robot", 'warning')
        else:
            self.status_label.config(text="● Reconnecting...", foreground="orange")

    def handle_response(self, response):
        try:
            data = json.loads(response)
            rtype = data.get('type')
            if rtype == 'status':
                self.update_status_ui(data)
            elif rtype == 'queue_status':
                self.log_manager.log("Queue Status:\n" + data.get('status', ''), 'info')
            elif rtype == 'workpieces':
                self.wp_manager.update_data(data.get('workpieces', '[]'))
            elif rtype == 'log':
                self.log_manager.log(f"[ROBOT] {data.get('message', '')}", data.get('level', 'info'))
            elif rtype == 'log_level':
                self.log_manager.log(f"Log level changed to {data.get('level', 'DEBUG')}", 'info')
            elif rtype == 'response':
                self.log_manager.log(f"Response: {data.get('message', '')}", 'success')
        except json.JSONDecodeError:
            self.parse_raw_log(response)

    def parse_raw_log(self, line):
        match = re.match(r'\[([^\]]+)\]\s+([^\|]+)\|\s*(\w+)\s*:\s*(.*)', line)
        if match:
            _, src, level, msg = match.groups()
            self.log_manager.log(f"[ROBOT] {src.strip()}: {msg}", level.lower())
        else:
            self.log_manager.log(f"[ROBOT] {line}", 'info')

    def update_status_ui(self, data):
        if 'program' in data:
            num = data['program']
            self.current_program.set(num)
            names = {0: "Idle", 1: "Pick New", 2: "Place New", 3: "Pick Measured", 4: "Place Measured",
                     5: "Calibration", 6: "Test Calib"}
            name = names.get(num, "Vision Task" if 100 <= num <= 199 else "Unknown")
            self.current_prog_label.config(text=f"{num} - {name}")
        
        if 'vision_connected' in data:
            conn = data['vision_connected']
            self.vision_connected.set(conn)
            self.vision_status_label.config(text="● Connected" if conn else "● Disconnected",
                                            foreground="green" if conn else "red")
            
        if 'workpiece_position' in data:
            pos = data['workpiece_position']
            self.workpiece_position.set(pos if pos and pos != "invalid" else "Not retrieved")
        
        # Update gripper states in the gripper panel
        if 'gripper1_closed' in data and 'gripper2_closed' in data and 'gripper3_closed' in data:
            g1 = data['gripper1_closed']
            g2 = data['gripper2_closed']
            g3 = data['gripper3_closed']
            if hasattr(self, 'gripper_panel') and self.gripper_panel is not None:
                self.gripper_panel.set_gripper_states(g1, g2, g3)

    # Command Methods
    def send_cmd(self, cmd):
        if self.client and self.client.send_command(cmd):
            return True
        messagebox.showwarning("Not Connected", "Please connect to the robot first")
        return False

    def set_program(self, num):
        if self.send_cmd({'type': 'set_program', 'program': num}):
            self.log_manager.log(f"Setting program to {num}", 'success')

    def cancel_program(self):
        if self.send_cmd({'type': 'cancel_program'}):
            self.log_manager.log("Cancelling program - returning home", 'warning')

    def get_status(self):
        self.send_cmd({'type': 'get_status'})

    def get_queue_status(self):
        self.send_cmd({'type': 'get_queue_status'})

    def refresh_workpieces(self):
        self.send_cmd({'type': 'get_workpieces'})

    def clear_workpiece_queue(self):
        if messagebox.askyesno("Clear Queue", "Are you sure?"):
            if self.send_cmd({'type': 'clear_queue'}):
                self.wp_manager.update_data([])

    def delete_selected_workpiece(self):
        sel = self.workpiece_tree.selection()
        if not sel: return messagebox.showwarning("No Selection", "Please select a workpiece")

        short_id = self.workpiece_tree.item(sel[0])['values'][0]
        wp = self.wp_manager.find_workpiece_by_short_id(short_id)
        if wp and messagebox.askyesno("Delete", f"Delete workpiece {short_id}?"):
            if self.send_cmd({'type': 'delete_workpiece', 'id': wp.get('id')}):
                self.refresh_workpieces()

    def pick_selected_workpiece(self):
        sel = self.workpiece_tree.selection()
        if not sel:
            return messagebox.showwarning("No Selection", "Please select a workpiece to pick")
        
        short_id = self.workpiece_tree.item(sel[0])['values'][0]
        wp = self.wp_manager.find_workpiece_by_short_id(short_id)
        if not wp:
            return messagebox.showerror("Error", f"Could not find workpiece {short_id}")
        
        full_id = wp.get('id')
        if messagebox.askyesno("Pick Workpiece", f"Pick workpiece {short_id}?"):
            if self.send_cmd({'type': 'pick_specific_workpiece', 'id': full_id}):
                self.log_manager.log(f"Forced pick requested for workpiece {short_id}", 'success')

    def on_motion_override_enabled_changed(self):
        """Handle master enable checkbox change"""
        if self.motion_override_enabled.get():
            # Enable the apply button
            self.apply_overrides_btn.config(state=tk.NORMAL)
        else:
            # Disable the apply button and clear overrides on the robot
            self.apply_overrides_btn.config(state=tk.DISABLED)
            if self.send_cmd({'type': 'clear_motion_override'}):
                self.log_manager.log("Motion overrides disabled and cleared", 'success')

    def apply_motion_overrides(self):
        """Apply motion overrides based on dropdown selections"""
        if not self.motion_override_enabled.get():
            return messagebox.showwarning("Overrides Disabled", "Please enable motion overrides first")
        
        cmd = {'type': 'set_motion_override'}
        
        # Map dropdown values to CSV strings or None (for default)
        redundancy_map = {
            "Default (-80,80,-60,60)": None,  # Use robot defaults
            "Narrow (-40,40,-30,30)": "-40,40,-30,30",
            "Wide (-100,100,-80,80)": "-100,100,-80,80",
            "Single (0)": "0",
            "None": "0"
        }
        
        zrot_map = {
            "Default (90,45,0,135,180,-45,-90,-135)": None,  # Use robot defaults
            "Cardinal (0,90,180,-90)": "0,90,180,-90",
            "Fine (0,15,30,45,60,75,90)": "0,15,30,45,60,75,90",
            "Single (0)": "0",
            "Single (90)": "90"
        }
        
        # Pick redundancy
        pick_red = redundancy_map.get(self.pick_redundancy_var.get())
        if pick_red is not None:
            cmd['pick_redundancy'] = pick_red
        
        # Pick alternate only
        if self.pick_alternate_only.get():
            cmd['pick_alternate_only'] = True
        else:
            cmd['pick_alternate_only'] = False
        
        # Place redundancy
        place_red = redundancy_map.get(self.place_redundancy_var.get())
        if place_red is not None:
            cmd['place_redundancy'] = place_red
        
        # Place Z-rotation
        place_zrot = zrot_map.get(self.place_zrot_var.get())
        if place_zrot is not None:
            cmd['place_zrot'] = place_zrot
        
        # Place alternate only
        if self.place_alternate_only.get():
            cmd['place_alternate_only'] = True
        else:
            cmd['place_alternate_only'] = False
        
        if self.send_cmd(cmd):
            self.log_manager.log(f"Motion overrides applied: Pick={self.pick_redundancy_var.get()}, "
                                f"Pick Alt={self.pick_alternate_only.get()}, Place={self.place_redundancy_var.get()}, "
                                f"Place ZRot={self.place_zrot_var.get()}, Place Alt={self.place_alternate_only.get()}", 
                                'success')

    def on_log_level_changed(self, event=None):
        lvl = self.log_level.get()
        self.log_manager.log(f"Local log level: {lvl}", 'info')
        if self.client and self.client.connected:
            self.client.send_command({'type': 'set_log_level', 'level': lvl})

    # Auto-refresh logic
    def start_auto_refresh(self):
        if self.client and self.client.connected:
            self.auto_refresh_enabled = True
            self.auto_refresh_data()

    def auto_refresh_data(self):
        if not self.client or not self.client.connected or not self.auto_refresh_enabled:
            return

        try:
            self.client.send_command({'type': 'get_status'})
            self.root.after(100, lambda: self.client.send_command({'type': 'get_workpieces'}))
        except Exception:
            self.stop_auto_refresh()
            return

        self.auto_refresh_timer = self.root.after(self.auto_refresh_interval, self.auto_refresh_data)

    def stop_auto_refresh(self):
        self.auto_refresh_enabled = False
        if self.auto_refresh_timer:
            self.root.after_cancel(self.auto_refresh_timer)
            self.auto_refresh_timer = None

def main():
    root = tk.Tk()
    app = RobotControlGUI(root)
    root.protocol("WM_DELETE_WINDOW", lambda: (app.disconnect(), root.destroy()))
    root.mainloop()

if __name__ == "__main__":
    main()
