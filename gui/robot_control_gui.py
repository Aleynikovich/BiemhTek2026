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
        self.root.geometry("900x700")
        
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
        
        self.create_widgets()
        #self.update_connection_status()
        
    def create_widgets(self):
        # Style configuration
        style = ttk.Style()
        style.configure('Title.TLabel', font=('Helvetica', 16, 'bold'))
        style.configure('Header.TLabel', font=('Helvetica', 12, 'bold'))
        style.configure('Status.TLabel', font=('Helvetica', 10))
        
        # Main container
        main_frame = ttk.Frame(self.root, padding="10")
        main_frame.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        
        # Configure grid weights
        self.root.columnconfigure(0, weight=1)
        self.root.rowconfigure(0, weight=1)
        main_frame.columnconfigure(1, weight=1)
        
        # Title
        title_label = ttk.Label(main_frame, text="KUKA LBR iiwa Robot Control", 
                               style='Title.TLabel')
        title_label.grid(row=0, column=0, columnspan=2, pady=(0, 20))
        
        # Connection Frame
        self.create_connection_frame(main_frame)
        
        # Status Frame
        self.create_status_frame(main_frame)
        
        # Program Control Frame
        self.create_program_control_frame(main_frame)
        
        # Vision Commands Frame
        self.create_vision_commands_frame(main_frame)
        
        # Quick Actions Frame
        self.create_quick_actions_frame(main_frame)
        
        # Console Output Frame
        self.create_console_frame(main_frame)
        
    def create_connection_frame(self, parent):
        frame = ttk.LabelFrame(parent, text="Connection", padding="10")
        frame.grid(row=1, column=0, columnspan=2, sticky=(tk.W, tk.E), pady=(0, 10))
        
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
        frame = ttk.LabelFrame(parent, text="Robot Status", padding="10")
        frame.grid(row=2, column=0, columnspan=2, sticky=(tk.W, tk.E), pady=(0, 10))
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
        
    def create_program_control_frame(self, parent):
        frame = ttk.LabelFrame(parent, text="Robot Programs", padding="10")
        frame.grid(row=3, column=0, columnspan=2, sticky=(tk.W, tk.E), pady=(0, 10))
        
        ttk.Label(frame, text="Robot Motion Programs (1-99):", style='Header.TLabel').grid(
            row=0, column=0, columnspan=3, sticky=tk.W, pady=(0, 10))
        
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
            row = 1 + (i // 3)
            col = i % 3
            btn = ttk.Button(frame, text=f"{prog_num}: {prog_name}", 
                           command=lambda p=prog_num: self.set_program(p),
                           width=25)
            btn.grid(row=row, column=col, padx=5, pady=5, sticky=tk.W)
    
    def create_vision_commands_frame(self, parent):
        frame = ttk.LabelFrame(parent, text="Vision Commands", padding="10")
        frame.grid(row=4, column=0, columnspan=2, sticky=(tk.W, tk.E), pady=(0, 10))
        
        ttk.Label(frame, text="Vision System Commands (100-199):", style='Header.TLabel').grid(
            row=0, column=0, columnspan=3, sticky=tk.W, pady=(0, 10))
        
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
            row = 1 + (i // 3)
            col = i % 3
            btn = ttk.Button(frame, text=f"{prog_num}: {prog_name}", 
                           command=lambda p=prog_num: self.set_program(p),
                           width=25)
            btn.grid(row=row, column=col, padx=5, pady=5, sticky=tk.W)
        
    def create_quick_actions_frame(self, parent):
        frame = ttk.LabelFrame(parent, text="Quick Actions", padding="10")
        frame.grid(row=5, column=0, columnspan=2, sticky=(tk.W, tk.E), pady=(0, 10))
        
        ttk.Button(frame, text="Emergency Stop (Program 0)", 
                  command=lambda: self.set_program(0),
                  width=25).grid(row=0, column=0, padx=5, pady=5)
        
        ttk.Button(frame, text="Get Status", 
                  command=self.get_status,
                  width=25).grid(row=0, column=1, padx=5, pady=5)
        
        ttk.Button(frame, text="Get Queue Status", 
                  command=self.get_queue_status,
                  width=25).grid(row=0, column=2, padx=5, pady=5)
        
        ttk.Button(frame, text="Clear Console", 
                  command=self.clear_console,
                  width=25).grid(row=0, column=3, padx=5, pady=5)
        
    def create_console_frame(self, parent):
        frame = ttk.LabelFrame(parent, text="Console Output", padding="10")
        frame.grid(row=6, column=0, columnspan=2, sticky=(tk.W, tk.E, tk.N, tk.S), pady=(0, 10))
        parent.rowconfigure(6, weight=1)
        
        # Log level control frame
        level_frame = ttk.Frame(frame)
        level_frame.grid(row=0, column=0, sticky=(tk.W, tk.E), pady=(0, 5))
        
        ttk.Label(level_frame, text="Minimum Log Level:", 
                 style='Status.TLabel').pack(side=tk.LEFT, padx=(0, 5))
        
        self.log_level_combo = ttk.Combobox(level_frame, textvariable=self.log_level,
                                           values=self.log_levels, width=10, state='readonly')
        self.log_level_combo.pack(side=tk.LEFT, padx=(0, 10))
        self.log_level_combo.bind('<<ComboboxSelected>>', self.on_log_level_changed)
        
        ttk.Label(level_frame, text="(filters logs displayed in console and sent from robot)",
                 font=('Helvetica', 8), foreground='gray').pack(side=tk.LEFT)
        
        self.console = scrolledtext.ScrolledText(frame, height=15, width=80, 
                                                 state=tk.DISABLED,
                                                 font=('Courier', 9))
        self.console.grid(row=1, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        frame.columnconfigure(0, weight=1)
        frame.rowconfigure(1, weight=1)
        
        # Configure tags for colored output
        self.console.tag_config('info', foreground='black')
        self.console.tag_config('success', foreground='green')
        self.console.tag_config('error', foreground='red')
        self.console.tag_config('warning', foreground='orange')
        self.console.tag_config('debug', foreground='gray')
        
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
        self.console.config(state=tk.NORMAL)
        self.console.insert(tk.END, f"[{timestamp}] {message}\n", level)
        self.console.see(tk.END)
        self.console.config(state=tk.DISABLED)
        
    def clear_console(self):
        """Clear console output"""
        self.console.config(state=tk.NORMAL)
        self.console.delete(1.0, tk.END)
        self.console.config(state=tk.DISABLED)
        
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
            
        except Exception as e:
            self.log_console(f"Connection failed: {str(e)}", 'error')
            if self.socket:
                self.socket.close()
            self.start_reconnect()
                
    def disconnect(self):
        """Disconnect from robot"""
        self.reconnecting = False  # Stop any reconnection attempts
        self.connected = False
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
            cmd_json = json.dumps(command) + "\n"
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
        self.status_label.config(text="● Reconnecting...", foreground="orange")
        
        # Start reconnection thread
        self.reconnect_thread = threading.Thread(target=self.reconnect_loop, daemon=True)
        self.reconnect_thread.start()
    
    def reconnect_loop(self):
        """Reconnection loop with exponential backoff"""
        while self.reconnecting and not self.connected:
            self.log_console(f"Reconnecting in {self.current_retry_delay:.1f} seconds...", 'info')
            time.sleep(self.current_retry_delay)
            
            try:
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
        try:
            data = json.loads(response)
            
            if data.get('type') == 'status':
                self.update_status(data)
            elif data.get('type') == 'queue_status':
                status = data.get('status', 'No status available')
                self.log_console("Queue Status:\n" + status, 'info')
            elif data.get('type') == 'log':
                level = data.get('level', 'info').lower()
                message = data.get('message', '')
                self.log_console(f"[ROBOT] {message}", level)
            elif data.get('type') == 'log_level':
                current_level = data.get('level', 'DEBUG')
                self.log_console(f"Robot log level: {current_level}", 'info')
            elif data.get('type') == 'response':
                self.log_console(f"Response: {data.get('message', '')}", 'success')
                
        except json.JSONDecodeError:
            # Handle non-JSON log entries (from NetworkListener)
            self.parse_log_entry(response)
    
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
