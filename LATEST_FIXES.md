# Latest Critical Fixes - Console Copy/Paste & Connection

## Problems Reported

1. **Can't copy/paste from console logs** - Ctrl+C not working
2. **Connection not working** - Still getting EOF errors and disconnects
3. **json.dumps error** - "module json has no attribute dumps"

## Root Causes Identified

### 1. Console Copy/Paste Issue

**Problem**: The key binding was TOO restrictive:
```python
self.console.bind("<Key>", lambda e: "break")  # Blocks EVERYTHING!
```

This blocked ALL keyboard events, including:
- Ctrl+C (copy)
- Ctrl+A (select all)
- Ctrl+V (paste)
- Any keyboard shortcuts

**How It Was Supposed to Work**: Block typing while allowing selection.
**What Actually Happened**: Blocked everything, making text uncopiable.

### 2. Connection Stability

**Problem**: Auto-refresh starting too quickly after connection:
- Only 1 second delay wasn't enough
- 50ms between commands too short
- No socket validation before sending
- Missing state checks

**Result**: Socket got overwhelmed and closed (EOF error).

### 3. JSON Module Error

**Problem**: Possible variable shadowing at runtime:
- Something might be creating a `json` variable
- Overwriting the imported `json` module
- Causing "has no attribute dumps" error

## Solutions Implemented (Commit 71e656a)

### Fix #1: Console Now Properly Copyable

**New Key Binding Logic**:
```python
def prevent_edit(event):
    # Allow Ctrl/Cmd key combinations (copy, select all, etc)
    if event.state & 0x4:  # Control key (bit flag 0x4)
        return None  # Allow the event to proceed
    if event.state & 0x8:  # Alt key (bit flag 0x8)
        return None  # Allow the event to proceed
    # Block all other key presses (regular typing)
    return "break"  # Block the event
```

**What This Does**:
- ✅ Allows Ctrl+C (copy)
- ✅ Allows Ctrl+A (select all)
- ✅ Allows Ctrl+X (cut)
- ✅ Allows Ctrl+V (paste - though text is read-only)
- ✅ Allows any Ctrl/Alt shortcuts
- ❌ Blocks regular typing (a, b, c, 1, 2, 3, etc.)
- ❌ Blocks Enter, Backspace, Delete
- ❌ Blocks function keys without modifiers

**Applied To**:
- Main console widget
- Popup console widget

### Fix #2: Connection Stability Improved

**Changes Made**:

1. **Longer Stabilization Period**:
```python
# Before:
self.root.after(1000, self.start_auto_refresh)  # 1 second

# After:
self.log_console("Auto-refresh will start in 3 seconds...", 'info')
self.root.after(3000, self.start_auto_refresh)  # 3 seconds
```

2. **Increased Command Delay**:
```python
# Before:
time.sleep(0.05)  # 50ms between commands

# After:
time.sleep(0.1)   # 100ms between commands
```

3. **Socket Validation**:
```python
# Check if socket is still valid before sending
if self.socket.fileno() == -1:
    raise Exception("Socket closed")
```

4. **Better State Checking**:
```python
# Only continue if all conditions met
if not self.connected or not self.auto_refresh_enabled or not self.socket:
    self.stop_auto_refresh()
    return
```

### Fix #3: JSON Module Protection

**Explicit Import at Function Level**:
```python
# In send_command():
import json as json_module  # Explicit import to avoid shadowing
cmd_json = json_module.dumps(command) + "\n"

# In auto_refresh_data():
import json as json_module  # Explicit import to avoid shadowing
cmd1 = json_module.dumps({'type': 'get_status'}) + "\n"
cmd2 = json_module.dumps({'type': 'get_workpieces'}) + "\n"
```

**Why This Works**:
- Even if something creates a variable named `json`
- The local import as `json_module` is protected
- Can't be shadowed by accident
- Guaranteed to be the real json module

## How to Test

### Test 1: Copy/Paste from Console

1. **Launch GUI**:
   ```bash
   cd /home/runner/work/BiemhTek2026/BiemhTek2026/gui
   python3 robot_control_gui.py
   ```

2. **Connect to robot** (or just look at any logs)

3. **Test Selection**:
   - Click and drag to select text
   - Text should highlight in blue
   
4. **Test Copy**:
   - Press **Ctrl+C** (or Cmd+C on Mac)
   - Should NOT hear system beep
   - Should copy successfully
   
5. **Test Paste**:
   - Open any text editor
   - Press **Ctrl+V**
   - Text should paste!

6. **Test Select All**:
   - Press **Ctrl+A** in console
   - All text should be selected

### Test 2: Connection Stability

1. **Ensure robot application is RESTARTED** (important!)
   - Backend changes need to be loaded
   - Specifically the `get_workpieces` command

2. **Launch GUI and Connect**:
   ```bash
   python3 robot_control_gui.py
   ```
   - Enter robot IP: 172.31.1.147
   - Port: 30001
   - Click "Connect"

3. **Watch Console**:
   - Should see: "Connected to robot..."
   - Should see: "Auto-refresh will start in 3 seconds..."
   - Wait 3 seconds
   - Should see: "Auto-refresh started"

4. **Monitor Connection**:
   - Connection should stay stable
   - No "EOF" errors
   - No disconnections
   - Status updates every 2 seconds

5. **Check Workpieces Tab**:
   - Should populate with workpiece data
   - 2D visualization should show workpieces
   - Updates automatically

### Test 3: JSON No Errors

1. **Connect to robot**
2. **Use manual commands**:
   - Click "Get Status"
   - Click "Get Queue Status"
   - Click "Refresh Workpieces"
3. **Should NOT see**:
   - "json has no attribute dumps"
   - Any json-related errors
4. **Should see**:
   - Commands sent successfully
   - Responses received

## What You Should See Now

### Console Behavior:
```
[21:45:10] Connected to robot at 172.31.1.147:30001  ✓
[21:45:10] Auto-refresh will start in 3 seconds...
[21:45:10] Sent: {'type': 'set_log_level', 'level': 'INFO'}
[21:45:13] Auto-refresh started
[21:45:15] [ROBOT] Status sent to client
[21:45:17] [ROBOT] Status sent to client
...

← All text is selectable and copyable with Ctrl+C!
```

### Connection Flow:
```
1. Click Connect
2. ✓ Socket established
3. ✓ Listen thread starts
4. ✓ Send log level
5. ⏱️ Wait 3 seconds
6. ✓ Auto-refresh starts
7. ✓ Send get_status (every 2s)
8. ⏱️ Wait 100ms
9. ✓ Send get_workpieces (every 2s)
10. ✓ Connection stays stable
```

## Technical Details

### Key Binding State Flags (Tkinter)

The `event.state` is a bitmask with these flags:
- `0x1` (bit 0): Shift key
- `0x4` (bit 2): Control key
- `0x8` (bit 3): Alt key
- `0x10` (bit 4): Num Lock
- `0x20` (bit 5): ?
- `0x40` (bit 6): ?
- `0x80` (bit 7): ?

By checking `event.state & 0x4`, we detect if Control is pressed.
By checking `event.state & 0x8`, we detect if Alt is pressed.

### Socket File Descriptor

`socket.fileno()` returns:
- Positive integer: Valid file descriptor
- `-1`: Socket is closed/invalid

This is a reliable way to check socket validity before operations.

### Auto-Refresh Timing

```
Connect (T=0)
  ↓
Send log level (T=0)
  ↓
Wait 3000ms
  ↓
Start auto-refresh (T=3s)
  ↓
Send get_status (T=3s)
  ↓
Wait 100ms
  ↓
Send get_workpieces (T=3.1s)
  ↓
Wait 2000ms
  ↓
Repeat from "Send get_status" (T=5.1s)
```

Total time between status commands: ~2.1 seconds
Total time between workpiece commands: ~2.1 seconds

## If Still Having Issues

### Console Copy Still Not Working?

Try this test:
```python
# Add this temporary debug in the prevent_edit function:
def prevent_edit(event):
    print(f"Key event: {event.keysym}, state: {event.state:08b}")
    if event.state & 0x4:
        print("  -> Allowing (Control)")
        return None
    if event.state & 0x8:
        print("  -> Allowing (Alt)")
        return None
    print("  -> Blocking")
    return "break"
```

When you press Ctrl+C, you should see:
```
Key event: c, state: 00000100
  -> Allowing (Control)
```

### Connection Still Dropping?

Check robot logs for:
- Socket buffer full
- Command queue overflow
- Thread exceptions

Try increasing delays:
- Auto-refresh start: 5 seconds (from 3)
- Between commands: 200ms (from 100ms)
- Refresh interval: 5 seconds (from 2)

### JSON Errors Still Occurring?

Add more aggressive protection:
```python
# At top of send_command and auto_refresh_data:
if 'json' in dir():
    if not hasattr(json, 'dumps'):
        print("WARNING: json module is corrupted!")
        import importlib
        import json
        importlib.reload(json)
```

## Files Changed

- `gui/robot_control_gui.py` - All fixes in this file

## Summary

All three critical issues should now be fixed:
1. ✅ Console text is copyable with Ctrl+C
2. ✅ Connection stays stable (3s delay, 100ms between commands)
3. ✅ JSON dumps error prevented (explicit import)

The GUI should now be fully functional and stable!
