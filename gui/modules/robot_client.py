import json
import socket
import threading
import time


class RobotClient:
    """Handles low-level socket communication with the robot."""

    INITIAL_RETRY_DELAY = 1.0
    MAX_RETRY_DELAY = 60.0
    BACKOFF_MULTIPLIER = 2.0

    def __init__(self, ip, port, on_message_callback, on_connect_callback, on_disconnect_callback):
        self.ip = ip
        self.port = port
        self.on_message_callback = on_message_callback
        self.on_connect_callback = on_connect_callback
        self.on_disconnect_callback = on_disconnect_callback

        self.socket = None
        self.connected = False
        self.reconnecting = False
        self.current_retry_delay = self.INITIAL_RETRY_DELAY
        self.listen_thread = None
        self.reconnect_thread = None

    def connect(self):
        try:
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.socket.settimeout(5)
            self.socket.connect((self.ip, self.port))
            self.connected = True
            self.reconnecting = False
            self.current_retry_delay = self.INITIAL_RETRY_DELAY

            # Start listening thread
            self.listen_thread = threading.Thread(target=self._listen_loop, daemon=True)
            self.listen_thread.start()

            self.on_connect_callback()
            return True
        except Exception as e:
            if self.socket:
                self.socket.close()
            raise e

    def disconnect(self):
        self.reconnecting = False
        self.connected = False
        if self.socket:
            try:
                self.socket.close()
            except:
                pass
            self.socket = None
        self.on_disconnect_callback()

    def send_command(self, command):
        if not self.connected or not self.socket:
            return False
        try:
            cmd_json = json.dumps(command) + "\n"
            self.socket.sendall(cmd_json.encode('utf-8'))
            return True
        except Exception as e:
            self._handle_disconnect()
            return False

    def _listen_loop(self):
        buffer = ""
        while self.connected:
            try:
                data = self.socket.recv(1024).decode('utf-8')
                if not data:
                    self._handle_disconnect()
                    break
                buffer += data
                while '\n' in buffer:
                    line, buffer = buffer.split('\n', 1)
                    if line.strip():
                        self.on_message_callback(line)
            except socket.timeout:
                continue
            except Exception:
                if self.connected:
                    self._handle_disconnect()
                break

    def _handle_disconnect(self):
        if not self.connected:
            return
        self.connected = False
        if self.socket:
            try:
                self.socket.close()
            except:
                pass
        self.on_disconnect_callback()
        self._start_reconnect()

    def _start_reconnect(self):
        if self.reconnecting:
            return
        self.reconnecting = True
        self.reconnect_thread = threading.Thread(target=self._reconnect_loop, daemon=True)
        self.reconnect_thread.start()

    def _reconnect_loop(self):
        while self.reconnecting and not self.connected:
            time.sleep(self.current_retry_delay)
            try:
                if self.socket:
                    try:
                        self.socket.close()
                    except:
                        pass
                self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.socket.settimeout(5)
                self.socket.connect((self.ip, self.port))
                self.connected = True
                self.reconnecting = False
                self.current_retry_delay = self.INITIAL_RETRY_DELAY

                self.listen_thread = threading.Thread(target=self._listen_loop, daemon=True)
                self.listen_thread.start()

                self.on_connect_callback(reconnected=True)
            except Exception:
                self.current_retry_delay = min(
                    self.current_retry_delay * self.BACKOFF_MULTIPLIER,
                    self.MAX_RETRY_DELAY
                )
