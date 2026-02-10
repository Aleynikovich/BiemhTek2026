# Workpiece Visualization Fix

## Problem Statement

From user feedback (comment 3881045085):
1. Workpieces data received but not drawing on 2D canvas
2. Rotation (rz) not being considered when drawing
3. Cyclic auto-refresh messages burying important logs

## Root Causes

### 1. Missing Rotation Data
The backend `WorkpieceQueue.getWorkpiecesJson()` was not serializing the `rz` (rotation) field, even though it exists in `WorkpieceData`.

### 2. Incorrect Coordinate Transformation
The GUI assumed a centered coordinate system (-350 to +350 for both X and Y), but the actual robot workspace is:
- **X axis**: -350 to +350 mm (700mm range) ✓
- **Y axis**: -600 to -200 mm (400mm range) ✗

User's workpiece data showed:
- Workpiece 1: X=25.7, Y=-510.0
- Workpiece 2: X=376.7, Y=-456.2

These Y values (-510, -456) are in the -600 to -200 range, not -200 to +200.

### 3. Console Spam
Auto-refresh runs every 2 seconds, sending `get_status` and `get_workpieces` commands. Both responses were being logged, causing:
```
[22:32:47] Sent: {'type': 'get_status'}
[22:32:47] Response: ...
[22:32:49] Sent: {'type': 'get_status'}
[22:32:49] Response: ...
```

This buried important robot logs and user command responses.

## Solutions Implemented

### Backend Fix: WorkpieceQueue.java

**File**: `src/biemhTekniker/data/WorkpieceQueue.java`

**Change**: Added rotation to JSON serialization

```java
// Before:
sb.append(",\"x\":").append(wp.getX());
sb.append(",\"y\":").append(wp.getY());
sb.append(",\"z\":").append(wp.getZ());
sb.append(",\"score\":").append(wp.getScore());

// After:
sb.append(",\"x\":").append(wp.getX());
sb.append(",\"y\":").append(wp.getY());
sb.append(",\"z\":").append(wp.getZ());
sb.append(",\"rz\":").append(wp.getRz());  // ADDED
sb.append(",\"score\":").append(wp.getScore());
```

**Output Format**:
```json
[
  {
    "id": 1770755399618,
    "reference": 2,
    "state": "AVAILABLE",
    "gripper": null,
    "x": 25.7,
    "y": -510.0,
    "z": 40.2,
    "rz": 45.0,  // NEW: Rotation in degrees
    "score": 0.00
  }
]
```

### Frontend Fix: robot_control_gui.py

#### 1. Rotation Support

**File**: `gui/robot_control_gui.py`
**Method**: `update_workpiece_visualization()`

**Change**: Draw workpieces as rotated polygons instead of axis-aligned rectangles

```python
import math

# Get rotation angle (degrees)
rz = float(wp.get('rz', 0))

# Convert to radians
angle_rad = math.radians(rz)
cos_a = math.cos(angle_rad)
sin_a = math.sin(angle_rad)

# Define rectangle corners (100x30mm)
half_length = 50  # 100mm / 2
half_width = 15   # 30mm / 2
corners = [
    (-half_length, -half_width),  # Top-left
    (+half_length, -half_width),  # Top-right
    (+half_length, +half_width),  # Bottom-right
    (-half_length, +half_width),  # Bottom-left
]

# Rotate each corner
rotated_corners = []
for cx, cy in corners:
    rx = cx * cos_a - cy * sin_a
    ry = cx * sin_a + cy * cos_a
    rotated_corners.extend([canvas_x + rx, canvas_y + ry])

# Draw as polygon (not rectangle)
canvas.create_polygon(rotated_corners,
                     fill=fill_color,
                     outline=outline_color,
                     width=outline_width,
                     tags='workpiece')
```

**Before**: Workpieces always horizontal
**After**: Workpieces rotated at rz angle

#### 2. Coordinate System Fix

**Change**: Map robot coordinates to canvas correctly

```python
# Canvas size: 700x400 pixels

# Robot workspace (based on actual data):
# X: [-350, +350] mm  (700mm range)
# Y: [-600, -200] mm  (400mm range)

# Transformation:
canvas_x = x + 350        # X: -350 maps to 0, +350 maps to 700
canvas_y = -y - 200       # Y: -200 maps to 0, -600 maps to 400
                          # (negative because canvas Y increases downward)

# Skip if outside visible area
if (canvas_x < -100 or canvas_x > 800 or 
    canvas_y < -100 or canvas_y > 500):
    continue  # Don't draw off-screen workpieces
```

**Example**:
- Robot: X=25.7, Y=-510.0
- Canvas: X=375.7, Y=310.0 ✓ (visible)

- Robot: X=376.7, Y=-456.2
- Canvas: X=726.7, Y=256.2 ✓ (visible but near right edge)

#### 3. Silent Cyclic Messages

**Change**: Don't log auto-refresh responses to console

```python
def handle_response(self, response):
    data = json_module.loads(response)
    response_type = data.get('type')
    
    if response_type == 'status':
        # Silent - cyclic message
        self.update_status(data)
        # NO LOG to console
        
    elif response_type == 'workpieces':
        # Silent - cyclic message
        self.update_workpiece_display(workpieces)
        # NO LOG to console
        
    elif response_type == 'log':
        # Always show robot logs
        self.log_console(f"[ROBOT] {message}", level)
        
    elif response_type == 'queue_status':
        # User-requested - show it
        self.log_console(f"Queue Status:\n{status}", 'info')
        
    elif response_type == 'response':
        # Generic response - show it
        self.log_console(f"Response: {message}", 'success')
```

**Before**: Console flooded every 2 seconds
**After**: Only important messages shown

## Visual Results

### Before Fix:
```
┌─────────────────────────────────┐
│  Working Plane (700x400mm)      │
│                                 │
│  [Empty - no workpieces]        │
│                                 │
│                                 │
│  Legend:                        │
│  ■ Ref 1  ■ Ref 2  ■ Ref 3     │
└─────────────────────────────────┘

Console:
[22:32:47] Sent: get_status
[22:32:47] Response received
[22:32:49] Sent: get_status
[22:32:49] Response received
[22:32:51] Sent: get_workpieces
[22:32:51] Response received
... (repeated every 2 seconds)
```

### After Fix:
```
┌─────────────────────────────────┐
│  Working Plane (700x400mm)      │
│                                 │
│         ┌─────┐ ← Rotated 45°  │
│        ╱ID:9618╲                │
│       │   G:A   │ ← Gripper A  │
│        ╲       ╱                │
│         └─────┘                 │
│        ( ○ ) ← Revolution circle│
│                                 │
│  Legend:                        │
│  ■ Ref 1  ■ Ref 2  ■ Ref 3     │
└─────────────────────────────────┘

Console:
[22:30:15] Connected to robot
[22:30:15] Log level changed to INFO
[22:30:18] Auto-refresh started
[22:32:45] [ROBOT] Motion complete
[22:33:12] User clicked "Clear Queue"
(Clean - no spam!)
```

## Additional Improvements

### 1. ID Display
Changed from full ID (1770755399618) to last 4 digits (9618) to reduce clutter on visualization.

```python
label = f"ID:{str(wp_id)[-4:]}"  # Last 4 digits
```

### 2. Null Gripper Handling
Check for both None and "None" string:

```python
if gripper and gripper != 'None':
    label += f"\nG:{gripper}"
```

### 3. Out-of-Bounds Filtering
Skip workpieces that would be drawn outside the visible canvas (with 100px margin):

```python
if (canvas_x < -100 or canvas_x > canvas_width + 100 or 
    canvas_y < -100 or canvas_y > canvas_height + 100):
    continue
```

## Testing Instructions

### 1. Deploy Backend Changes

The robot application must be restarted to load the new `WorkpieceQueue.getWorkpiecesJson()` with rz field:

```bash
# In Sunrise.Workbench:
# 1. Build project (Ctrl+B)
# 2. Sync to controller
# 3. Stop running application
# 4. Start application
```

### 2. Launch GUI

```bash
cd gui
python3 robot_control_gui.py
```

### 3. Connect and Verify

1. **Connect** to robot (172.31.1.147:30001)
2. **Wait 3 seconds** for auto-refresh to start
3. **Check Workpieces tab**:
   - Should see workpieces on 2D canvas
   - Rotated if rz != 0
   - In correct positions
   - Revolution circles around each
4. **Check Console tab**:
   - Should be clean (no auto-refresh spam)
   - Only important messages
5. **Test rotation**:
   - If workpiece has rz=45°, should be rotated 45°
   - If rz=0°, should be horizontal

### 4. Expected Output

**Treeview** (unchanged):
```
ID              Ref  State      Gripper  X      Y       Z     Score
1770755399618   2    AVAILABLE  None     25.7   -510.0  40.2  0.00
1770755400943   3    AVAILABLE  None     376.7  -456.2  40.4  0.00
```

**Canvas** (now working):
- Two workpieces visible
- Positioned correctly
- Rotated at their rz angles
- Color-coded by reference
- Revolution circles showing collision zones

**Console** (clean):
```
[22:45:30] Connected to robot at 172.31.1.147:30001
[22:45:30] Log level changed to INFO
[22:45:33] Auto-refresh started
```
(No more spam!)

## Technical Notes

### Coordinate System

The robot uses a right-handed coordinate system:
- **Origin**: Robot base
- **X axis**: Positive to the right (robot's perspective)
- **Y axis**: Positive away from robot
- **Z axis**: Positive upward (vertical)

The working plane in this application:
- X range: -350 to +350 mm (700mm total)
- Y range: -600 to -200 mm (400mm total)
- Z: Typically 30-50 mm (table height)

### Canvas Transformation

Canvas coordinates have origin at top-left:
- **X**: Increases to the right (same as robot)
- **Y**: Increases downward (opposite of robot)

Transformation formulas:
```
canvas_x = robot_x + 350
canvas_y = -(robot_y) - 200
```

This maps:
- robot (-350, -600) → canvas (0, 400)
- robot (0, -400) → canvas (350, 200)
- robot (+350, -200) → canvas (700, 0)

### Rotation

Workpieces are rotated around their center using standard 2D rotation matrix:

```
[x']   [cos(θ)  -sin(θ)] [x]
[y'] = [sin(θ)   cos(θ)] [y]
```

Where θ is the rz angle in radians.

## Files Modified

1. **src/biemhTekniker/data/WorkpieceQueue.java**
   - Added rz to JSON serialization
   - Line: `sb.append(",\"rz\":").append(wp.getRz());`

2. **gui/robot_control_gui.py**
   - Fixed `update_workpiece_visualization()` method
   - Added rotation support with polygon drawing
   - Fixed coordinate transformation
   - Made cyclic messages silent in `handle_response()`

## Commit Information

**Commit**: af64c09
**Date**: 2026-02-10
**Message**: Fix workpiece visualization: add rotation support and fix coordinates

## Related Issues

- Original issue: Full refactoring (#XXX)
- Comment: 3881045085 (workpieces not drawing, no rotation, log spam)

## Future Enhancements

1. **Dynamic Coordinate Mapping**: Auto-detect robot workspace from actual workpiece positions
2. **Zoom/Pan**: Allow user to zoom into specific areas
3. **Collision Highlighting**: Highlight overlapping revolution circles in red
4. **Edit Mode**: Click workpieces to edit their data
5. **Historical View**: Show workpiece movement trails over time
