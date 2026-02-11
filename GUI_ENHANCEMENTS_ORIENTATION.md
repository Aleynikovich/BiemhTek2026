# GUI Enhancements - Orientation Visualization and Workpiece Management

## Overview
This document describes the GUI enhancements added to support orientation visualization and improved workpiece management.

## Table Enhancements

### New Columns Added

| Column | Description | Example |
|--------|-------------|---------|
| **Ori** | Orientation symbol | → (regular, 0) or ↻ (180deg, 1) |
| **Rx** | Rotation around X-axis in degrees | 12.5 |
| **Ry** | Rotation around Y-axis in degrees | -5.3 |
| **Rz** | Rotation around Z-axis in degrees | 90.0 |

### Updated Columns

| Column | Before | After |
|--------|--------|-------|
| **ID** | Full ID (may be very long) | Last 8 digits for readability |
| **Ref** | Reference index (1, 2, 3) | Reference string ("10", "11", "20", etc.) |

### Complete Table Layout

```
╔════════╦═════╦═════╦══════════╦═════════╦═══════╦═══════╦═══════╦═══════╦═══════╦═══════╦═══════╗
║   ID   ║ Ref ║ Ori ║  State   ║ Gripper ║   X   ║   Y   ║   Z   ║   Rx  ║   Ry  ║   Rz  ║ Score ║
╠════════╬═════╬═════╬══════════╬═════════╬═══════╬═══════╬═══════╬═══════╬═══════╬═══════╬═══════╣
║ 12345678║  10 ║  →  ║AVAILABLE ║   N/A   ║ 450.2 ║ 123.5 ║  45.8 ║  12.3 ║  -5.1 ║  90.5 ║  0.95 ║
║ 23456789║  11 ║  ↻  ║  PICKED  ║    A    ║ 432.1 ║ 156.2 ║  47.3 ║  15.2 ║  -3.8 ║  92.1 ║  0.92 ║
║ 34567890║  20 ║  →  ║AVAILABLE ║   N/A   ║ 415.6 ║ 178.9 ║  46.2 ║  10.5 ║  -6.2 ║  88.7 ║  0.89 ║
╚════════╩═════╩═════╩══════════╩═════════╩═══════╩═══════╩═══════╩═══════╩═══════╩═══════╩═══════╝
```

**Column Widths:**
- ID: 80px (shortened for readability)
- Ref: 40px (shows reference string like "10", "11")
- Ori: 35px (shows arrow symbol)
- State: 80px
- Gripper: 50px
- X, Y, Z: 55px each
- Rx, Ry, Rz: 55px each
- Score: 50px

## 2D Visualization Enhancements

### Orientation Arrow (X+ Tool Axis)

Each workpiece now displays a **yellow arrow** showing the direction of the gripper's X+ tool axis:

```
        Orientation 0 (Regular - →)          Orientation 1 (180deg - ↻)
        
              ↑ Y                                   ↑ Y
              │                                     │
        ┌─────┼─────┐                         ┌─────┼─────┐
        │     │     │                         │     │     │
    ────┤     ●─────┤──→ X                ────┤─────●     ┤──→ X
        │   Yellow  │                         │  Yellow   │
        │   Arrow → │                         │ ← Arrow   │
        └───────────┘                         └───────────┘
        
   Arrow points FORWARD                  Arrow points BACKWARD
   (along workpiece length)              (opposite direction)
```

**Arrow Calculation:**
- **Base direction:** Determined by Rz rotation angle
- **Orientation adjustment:** 
  - Orientation 0: No adjustment (arrow points forward)
  - Orientation 1: Add 180° (arrow points backward)

**Visual Properties:**
- Color: Yellow (#FFFF00)
- Width: 2 pixels
- Arrow head: Displayed at the tip
- Length: Half of workpiece length

### Complete Workpiece Visualization

Each workpiece on the 2D canvas shows:

1. **Colored rectangle** - Reference color (Red=1, Purple=2, Blue=3)
2. **Orientation arrow** - Yellow arrow showing X+ tool axis
3. **Revolution circle** - Dashed red circle showing rotation envelope
4. **State outline** - Color-coded border (Orange=PICKED, Purple=MEASURING, etc.)
5. **ID label** - Last 4 digits of workpiece ID
6. **Gripper label** - Shows which gripper holds it (A or B)

Example visualization:
```
         Revolution circle (dashed red)
              ╭─────────────╮
             ╱               ╲
            ╱  ┌─────────┐   ╲
           │   │█████████│────▶  Yellow arrow (X+ axis)
           │   │   ID:   │     │
           │   │  1234   │     │
            ╲  │  G: A   │    ╱
             ╲ └─────────┘   ╱
              ╰─────────────╯
         
         Rectangle color = Reference
         Border color = State
```

## Button Controls

### Updated Layout

```
┌──────────────────┬──────────────────┬──────────────────┬──────────────────┐
│ Refresh          │ Get Queue        │ Clear Queue      │ Delete Selected  │
│ Workpieces       │ Status           │                  │                  │
└──────────────────┴──────────────────┴──────────────────┴──────────────────┘
```

### Button Functions

#### 1. Refresh Workpieces
- **Action:** Requests fresh workpiece data from robot
- **Command:** `{"type": "get_workpieces"}`
- **Updates:** Both table and 2D visualization

#### 2. Get Queue Status
- **Action:** Displays queue statistics in console
- **Command:** `{"type": "get_queue_status"}`
- **Output:** Total count, state breakdown

#### 3. Clear Queue ✨ *FIXED*
- **Action:** Deletes ALL workpieces from queue
- **Command:** `{"type": "clear_queue"}`
- **Confirmation:** Yes/No dialog
- **Backend:** Now properly implemented in ConsoleCommandHandler
- **Effect:** WorkpieceQueue.clear() is called

#### 4. Delete Selected ✨ *NEW*
- **Action:** Deletes the currently selected workpiece
- **Command:** `{"type": "delete_workpiece", "id": <workpiece_id>}`
- **Confirmation:** Yes/No dialog showing truncated ID
- **Selection:** Click on workpiece row to select
- **Effect:** WorkpieceQueue.removeWorkpiece(id) is called
- **Auto-refresh:** Refreshes display after deletion

## Backend API

### New Commands

#### Clear Queue Command
```json
{
  "type": "clear_queue"
}
```
**Response:**
```json
{
  "type": "response",
  "message": "Workpiece queue cleared successfully",
  "success": true
}
```

#### Delete Workpiece Command
```json
{
  "type": "delete_workpiece",
  "id": 1234567890
}
```
**Success Response:**
```json
{
  "type": "response",
  "message": "Workpiece 1234567890 deleted successfully",
  "success": true
}
```
**Error Response:**
```json
{
  "type": "error",
  "message": "Workpiece not found: 1234567890",
  "success": false
}
```

### Enhanced JSON Output

Workpiece data now includes rx and ry:

```json
{
  "id": 1234567890,
  "reference": 1,
  "orientation": 0,
  "referenceString": "10",
  "state": "PICKED",
  "gripper": "A",
  "x": 450.2,
  "y": 123.5,
  "z": 45.8,
  "rx": 12.3,    ← NEW
  "ry": -5.1,    ← NEW
  "rz": 90.5,
  "score": 0.95
}
```

## Orientation Symbol Legend

| Symbol | Orientation | Description |
|--------|-------------|-------------|
| → | 0 | Regular pick (gripper in normal orientation) |
| ↻ | 1 | 180-degree rotation pick (gripper rotated 180° around Z) |

The arrow on the visualization shows the actual X+ tool axis direction, accounting for both the Rz rotation and the orientation.

## User Workflow

### Viewing Workpieces
1. Click "Refresh Workpieces"
2. View workpieces in table with orientation arrows
3. See visual representation on 2D canvas with yellow arrows

### Deleting Individual Workpiece
1. Click on workpiece row in table to select it
2. Click "Delete Selected" button
3. Confirm deletion in dialog
4. Workpiece is removed from queue
5. Display automatically refreshes

### Clearing All Workpieces
1. Click "Clear Queue" button
2. Confirm in dialog
3. All workpieces removed from queue
4. Table and visualization cleared

## Technical Implementation

### Backend (Java)
- **WorkpieceQueue:** Added `removeWorkpiece(long id)` method
- **ConsoleCommandHandler:** Added handlers for `clear_queue` and `delete_workpiece`
- **SimpleJSON:** Added `getLong()` method for ID parsing
- **ConsoleServerInterface:** Added interface methods for clear and delete

### Frontend (Python)
- **Table:** Added Ori, Rx, Ry, Rz columns
- **Visualization:** Added yellow arrow calculation and drawing
- **Controls:** Added Delete Selected button
- **Data Management:** Store last_workpieces_data for ID lookup

## Benefits

1. **Clear Orientation Visibility:** Users can instantly see which orientation was used to pick each workpiece
2. **Complete Rotation Data:** All three rotation angles (Rx, Ry, Rz) now visible for debugging
3. **Visual Confirmation:** Yellow arrow on canvas shows exact tool orientation
4. **Better Management:** Can now delete individual problematic workpieces instead of clearing entire queue
5. **Fixed Functionality:** Clear queue now actually works (backend was missing)

## Example Use Cases

### Case 1: Verify Orientation
*User wants to confirm orientation before sending to Schunk base*
1. Look at "Ori" column - see → or ↻
2. Check yellow arrow on visualization
3. Verify matches expected pick strategy

### Case 2: Remove Bad Workpiece
*Vision detected workpiece at bad angle*
1. See workpiece with unusual Rx/Ry values
2. Select the row
3. Click "Delete Selected"
4. Robot ignores that workpiece

### Case 3: Fresh Start
*Starting new batch of parts*
1. Click "Clear Queue"
2. Confirm
3. All old workpieces removed
4. Run scan to populate with new parts
