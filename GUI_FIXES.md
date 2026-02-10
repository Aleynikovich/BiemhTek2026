# GUI Fixes - Connection & Console Issues

## Problems Reported

1. **Can't connect to robot** - Instant disconnection with "client disconnected EOF" error
2. **No Pop Out button** - Button missing from console tab
3. **Console not copyable** - Can't select/copy text from logs

## Root Causes

The automatic merges/rebases on the controller lost all the GUI improvements that were previously committed. This included:
- Auto-refresh functionality
- Pop-out console feature
- Console text improvements

Additionally, the auto-refresh was starting immediately on initialization, which caused the connection to be overwhelmed with requests before it was fully established, leading to EOF errors.

## Fixes Implemented (Commit 8c4dab1)

### 1. Connection Stability

**Problem**: Auto-refresh starting immediately overwhelmed the socket connection

**Solutions**:
- Auto-refresh now **disabled by default**
- Only starts **1 second after successful connection** to let it stabilize
- Added **50ms delay between commands** in auto-refresh to prevent flooding
- **Stops auto-refresh on disconnect** to prevent errors
- Better error handling with try/catch in auto_refresh_data()

**Code**:
```python
# In connect():
self.root.after(1000, self.start_auto_refresh)  # 1 second delay

# In auto_refresh_data():
self.socket.sendall(cmd1.encode('utf-8'))
time.sleep(0.05)  # Small delay between commands
self.socket.sendall(cmd2.encode('utf-8'))

# In disconnect():
self.stop_auto_refresh()  # Clean shutdown
```

### 2. Console Made Copyable

**Problem**: Console was in `DISABLED` state, preventing text selection

**Solutions**:
- Removed `state=tk.DISABLED` parameter
- Added `wrap=tk.WORD` for better text wrapping
- Used key binding to prevent typing: `self.console.bind("<Key>", lambda e: "break")`
- Text now fully selectable and copyable with mouse

**Before**:
```python
self.console = scrolledtext.ScrolledText(parent, state=tk.DISABLED, ...)
```

**After**:
```python
self.console = scrolledtext.ScrolledText(parent, wrap=tk.WORD, ...)
self.console.bind("<Key>", lambda e: "break")  # Prevent typing but allow selection
```

### 3. Pop-Out Console Restored

**Problem**: Pop-out button and functionality were lost in merge

**Solutions**:
- Added "Pop Out" button to console tab
- Implemented `pop_out_console()` method
- Created separate window with its own ScrolledText widget
- Real-time sync between main and popup console
- Both consoles support text selection

**Features**:
- Opens 900x600 window
- Copies all existing logs on creation
- New logs appear in both consoles simultaneously
- Independent close handling
- Brings to front if already open

### 4. Auto-Refresh Restored

**Problem**: Auto-refresh functionality was completely lost

**Solutions**:
- Added auto-refresh timer system
- Fetches status and workpieces every 2 seconds
- Silent operation (no console spam for refresh requests)
- Properly parses JSON response from workpieces command
- Updates 2D visualization automatically

**Methods Added**:
- `start_auto_refresh()` - Enables and starts the refresh cycle
- `auto_refresh_data()` - Performs silent data fetch
- `stop_auto_refresh()` - Cancels timer and disables refresh

## Testing Instructions

### To Test Connection:
1. Start the GUI: `python3 gui/robot_control_gui.py`
2. Click "Connect" - should connect without EOF error
3. Watch console - should see "Auto-refresh started" after 1 second
4. Status should update automatically
5. No disconnection should occur

### To Test Copyable Console:
1. Type or select text in console with mouse
2. Text should be selectable (highlighted in blue)
3. Ctrl+C or right-click → Copy should work
4. Paste into another application to verify

### To Test Pop-Out:
1. Go to Console tab
2. Click "Pop Out" button
3. Separate window should open with all logs
4. Type in main console - should appear in both
5. Close popup - main console continues working

### To Test Auto-Refresh:
1. Connect to robot
2. Go to Workpieces tab
3. Should see 2D visualization update every 2 seconds
4. No console spam about refreshing
5. Workpiece data should update automatically

## Technical Notes

### Console Widget State
- Previously: `state=tk.DISABLED` (not copyable)
- Now: Normal state with key binding to prevent editing
- Benefit: Text selectable while still read-only

### Auto-Refresh Timing
- Delay before start: 1000ms (1 second)
- Refresh interval: 2000ms (2 seconds)
- Delay between commands: 50ms
- These values prevent connection overload

### JSON Parsing
Updated to handle workpieces response as JSON string:
```python
workpieces_json = data.get('workpieces', '[]')
if isinstance(workpieces_json, str):
    workpieces = json.loads(workpieces_json)
```

## Files Changed

- `gui/robot_control_gui.py` - All fixes in single file

## Lines Changed

- Added: ~150 lines (auto-refresh, pop-out methods)
- Modified: ~10 lines (console creation, connection handling)
- Total: ~160 lines changed

## Next Steps

The robot application needs to be **restarted** for the backend `get_workpieces` command to work. Once restarted:
1. GUI will connect successfully
2. Auto-refresh will work
3. 2D visualization will populate with real data
4. No more EOF errors
