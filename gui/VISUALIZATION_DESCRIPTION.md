# 2D Workpiece Visualization - Visual Description

## Overview
The new 2D visualization in the Workpieces tab provides a real-time graphical representation of workpiece positions on the 700x400mm working plane.

## Layout

```
┌─────────────────────────────────────────────────────────────────┐
│ Working Plane (700x400mm)                                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│ Y ↑                                                         X → │
│   │                                                             │
│   │    [Grid: 50mm spacing]                                    │
│   │                                                             │
│   │    ┌─────────────┐                                         │
│   │    │   ID: 1     │ ← Red rectangle (100x30mm)             │
│   │    │   Ref 1     │   representing workpiece               │
│   │    └─────────────┘                                         │
│   │      ○○○○○○○○○○    ← Dashed circle (50mm radius)          │
│   │                      shows revolution projection          │
│   │                                                             │
│   │              ╔═════════════╗                               │
│   │              ║   ID: 3     ║ ← Thick orange outline       │
│   │              ║   G: A      ║   indicates PICKED state     │
│   │              ╚═════════════╝   Cyan = Ref 2               │
│   │                                                             │
│   │                        ┌─────────────┐                     │
│   │                        │   ID: 6     │ ← Blue = Ref 3     │
│   │                        │             │                     │
│   │                        └─────────────┘                     │
│   │                                                             │
│   └─────────────────────────────────────────────────────────→  │
│                                                                 │
│   Legend:                                                       │
│   ■ Ref 1  ■ Ref 2  ■ Ref 3                                   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Features

### 1. **Workpiece Representation**
- **Rectangle**: 100mm x 30mm (actual workpiece dimensions)
- **Fill Color**: Indicates reference type
  - Red (#FF6B6B): Reference 1 (53)
  - Cyan (#4ECDC4): Reference 2 (55)
  - Blue (#45B7D1): Reference 3 (60)

### 2. **State Indication**
- **Outline Color**: Indicates workpiece state
  - Green: AVAILABLE (ready to pick)
  - Orange: PICKED (in gripper)
  - Purple: MEASURING (on measurement machine)
  - Blue: MEASURED (measurement complete)
  - Gray: RETURNED (placed back)
- **Outline Width**: 
  - 3px for PICKED workpieces (emphasis)
  - 2px for other states

### 3. **Revolution Projection**
- **Dashed Circle**: 50mm radius around workpiece center
- **Purpose**: Shows the area swept during workpiece rotation
- **Benefit**: Helps identify potential collisions when placing measured workpieces

### 4. **Labels**
- **Workpiece ID**: Displayed on each piece
- **Gripper Location**: Shows "G:A" or "G:B" when workpiece is picked
- **Color**: White text for visibility

### 5. **Coordinate System**
- **Origin**: Center of working plane (350mm, 200mm from top-left)
- **X-axis**: Positive to the right
- **Y-axis**: Positive upward (inverted from canvas coordinates)
- **Grid**: 50mm spacing for spatial reference

### 6. **Legend**
- **Reference Colors**: Shows which color represents which reference
- **Location**: Bottom-left corner

## Use Cases

### Collision Detection
The revolution circles allow operators to see if placing a measured workpiece at a new location would potentially collide with existing workpieces.

**Example:**
```
    ┌───────┐
    │ ID: 3 │
    └───────┘
     ○○○○○○○  ← These circles overlap!
      ○○○○○○  ← Potential collision
    ┌───────┐
    │ ID: 4 │
    └───────┘
```

### Spatial Understanding
Operators can quickly see:
- Which workpieces are where
- Which ones are picked (thick orange outline)
- Which gripper holds which piece
- Density of workpieces in different areas

### Real-Time Monitoring
- Updates automatically when workpieces are scanned
- Shows workpiece movement as they're picked and placed
- Synchronized with the workpiece treeview list

## Technical Details

### Coordinate Transformation
```python
# Robot coordinates (center origin) → Canvas coordinates (top-left origin)
canvas_x = plane_width / 2 + robot_x
canvas_y = plane_height / 2 - robot_y  # Y inverted
```

### Dimensions
- **Working Plane**: 700mm × 400mm
- **Workpiece**: 100mm × 30mm rectangles
- **Revolution Circle**: 50mm radius (100mm diameter)
- **Grid**: 50mm squares

### Color Palette
- **References**: Distinct, high-contrast colors
- **States**: Standard traffic light colors (green=go, orange=caution, etc.)
- **Grid**: Light gray (non-intrusive)
- **Background**: White (maximum contrast)

## Future Enhancements
- Click workpiece to select in treeview
- Drag workpiece to test new positions
- Show travel path for robot motion
- Heatmap of frequently accessed areas
- Zoom and pan controls
