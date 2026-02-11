import tkinter as tk
from datetime import datetime
from tkinter import ttk, scrolledtext


class LogManager:
    """Manages console logging and filtering."""

    LOG_LEVELS = ["DEBUG", "INFO", "WARN", "ERROR"]
    LOG_LEVEL_ORDINAL = {"DEBUG": 0, "INFO": 1, "WARN": 2, "ERROR": 3}
    LEVEL_MAP = {
        'info': 'INFO',
        'success': 'INFO',
        'error': 'ERROR',
        'warning': 'WARN',
        'debug': 'DEBUG'
    }

    def __init__(self, console_widget, log_level_var):
        self.console = console_widget
        self.log_level_var = log_level_var
        self.console_line_count = 0
        self.console_popup = None
        self.console_popup_widget = None

        self._setup_tags(self.console)

    def _setup_tags(self, widget):
        widget.tag_config('info', foreground='black')
        widget.tag_config('success', foreground='green')
        widget.tag_config('error', foreground='red')
        widget.tag_config('warning', foreground='orange')
        widget.tag_config('debug', foreground='gray')
        widget.tag_config('row_even', background='white')
        widget.tag_config('row_odd', background='#f0f0f0')

    def log(self, message, level='info'):
        msg_level = self.LEVEL_MAP.get(level.lower(), 'INFO')
        current_level = self.log_level_var.get()

        if self.LOG_LEVEL_ORDINAL.get(msg_level, 1) < self.LOG_LEVEL_ORDINAL.get(current_level, 0):
            return

        timestamp = datetime.now().strftime("%H:%M:%S")
        formatted_line = f"[{timestamp}] {message}\n"

        row_tag = 'row_even' if self.console_line_count % 2 == 0 else 'row_odd'
        self.console_line_count += 1

        self._insert_to_widget(self.console, formatted_line, level, row_tag)

        if self.console_popup_widget:
            try:
                self._insert_to_widget(self.console_popup_widget, formatted_line, level, row_tag)
            except Exception:
                self.console_popup_widget = None

    def _insert_to_widget(self, widget, line, level, row_tag):
        line_start = widget.index(tk.END)
        widget.insert(tk.END, line)
        line_end = widget.index(tk.END)
        widget.tag_add(row_tag, line_start, line_end)
        widget.tag_add(level, line_start, line_end)
        widget.tag_raise(level)
        widget.see(tk.END)

    def clear(self):
        self.console.delete(1.0, tk.END)
        self.console_line_count = 0
        if self.console_popup_widget:
            try:
                self.console_popup_widget.delete(1.0, tk.END)
            except Exception:
                self.console_popup_widget = None

    def pop_out(self, root):
        if self.console_popup and tk.Toplevel.winfo_exists(self.console_popup):
            self.console_popup.lift()
            return

        self.console_popup = tk.Toplevel(root)
        self.console_popup.title("Robot Console")
        self.console_popup.geometry("900x600")

        popup_frame = ttk.Frame(self.console_popup, padding="5")
        popup_frame.pack(fill=tk.BOTH, expand=True)

        control_frame = ttk.Frame(popup_frame)
        control_frame.pack(fill=tk.X, pady=(0, 5))
        ttk.Label(control_frame, text="Console Output (Pop-out)", font=('Helvetica', 10, 'bold')).pack(side=tk.LEFT)
        ttk.Button(control_frame, text="Clear", command=self.clear).pack(side=tk.RIGHT, padx=5)

        self.console_popup_widget = scrolledtext.ScrolledText(popup_frame, wrap=tk.WORD, font=('Courier', 9))
        self.console_popup_widget.pack(fill=tk.BOTH, expand=True)

        def prevent_edit(event):
            if event.state & 0x4 or event.state & 0x8:
                return None
            return "break"

        self.console_popup_widget.bind("<Key>", prevent_edit)

        self._setup_tags(self.console_popup_widget)
        self.console_popup_widget.insert('1.0', self.console.get('1.0', tk.END))

        def on_popup_close():
            self.console_popup_widget = None
            self.console_popup.destroy()
            self.console_popup = None

        self.console_popup.protocol("WM_DELETE_WINDOW", on_popup_close)
