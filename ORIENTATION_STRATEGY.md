# Orientation Strategy - Implementation Summary

## Overview

The orientation strategy determines how workpieces are picked and tracked through the system. The robot now determines workpiece orientation based on the successful pick strategy, rather than relying solely on vision system feedback.

## Key Changes

### 1. Robot-Determined Orientation

**Previous Behavior:**
- Vision system scanned picked workpiece and determined orientation
- Orientation set after pick operation during scanning phase

**New Behavior:**
- Robot determines orientation during pick based on successful strategy
- Orientation set immediately after successful pick
- Vision scan kept for visual effect but does not override robot decision

### 2. Reference String Format

The new reference string format is **"xy"** where:
- **x** = Reference number (1, 2, or 3)
- **y** = Orientation (0 or 1)

**Examples:**
- `"10"` = Reference 1, regular orientation (0 deg)
- `"11"` = Reference 1, rotated orientation (180 deg)
- `"20"` = Reference 2, regular orientation (0 deg)
- `"21"` = Reference 2, rotated orientation (180 deg)
- `"30"` = Reference 3, regular orientation (0 deg)
- `"31"` = Reference 3, rotated orientation (180 deg)

### 3. Orientation Values

| Value | Description | Robot Behavior |
|-------|-------------|----------------|
| 0 | Regular | Picked with regular gripper orientation |
| 1 | 180deg Rotation | Picked with 180deg rotation around Z-axis |

## Implementation Details

### MotionStrategy Enhancement

Added `getOrientation()` method to expose which orientation was used:

```java
public int getOrientation()
{
    return useAlternatePosition ? 1 : 0;
}
```

### Pick Operation Flow

1. **Try Regular Strategies First** (`useAlternatePosition=false`)
   - If successful: orientation = 0
   
2. **Try Alternate Strategies** (`useAlternatePosition=true`)
   - If successful: orientation = 1
   
3. **Set Workpiece Orientation**
   - Immediately after successful pick
   - Based on successful strategy's `getOrientation()`

### Code Changes

**PickNewWorkpieceProgram.java:**
```java
// Track successful strategy
MotionStrategy successfulStrategy = null;
for (MotionStrategy strategy : motionStrategies) {
    if (strategy.executeMotion(...)) {
        successfulStrategy = strategy;
        break;
    }
}

// Set orientation based on successful strategy
int orientation = successfulStrategy.getOrientation();
workpieceData.setOrientation(orientation);
```

**ScanPickedWorkpiece.java:**
```java
// Vision scan kept for visual effect only
int visionOrientation = refWithOrientationInt % 10;
int robotOrientation = workpieceData.getOrientation();

// Do NOT override robot-determined orientation
log.info("Vision orientation=" + visionOrientation + 
         ", robot orientation=" + robotOrientation);
```

**WorkpieceData.java:**
```java
// New method to get reference string in "xy" format
public synchronized String getReferenceString()
{
    return String.valueOf(referenceIndex) + String.valueOf(orientation);
}
```

## Integration Points

### Console/GUI Integration

The workpiece JSON now includes:
- `reference`: Reference index (1-3)
- `orientation`: Orientation value (0-1)
- `referenceString`: Combined "xy" format string

**Example JSON:**
```json
{
  "id": 1234567890,
  "reference": 1,
  "orientation": 1,
  "referenceString": "11",
  "state": "PICKED",
  "gripper": "A",
  "x": 450.2,
  "y": 123.5,
  "z": 45.8,
  "rz": 90.5,
  "score": 0.95
}
```

### Schunk Base Communication

When placing workpieces on the Schunk base, the orientation is available via:
- `workpieceData.getOrientation()` - Returns 0 or 1
- `workpieceData.getReferenceString()` - Returns "xy" format

This allows the machine to know the workpiece orientation for proper handling.

## Benefits

1. **Immediate Feedback**: Orientation known right after pick, not after scan
2. **Deterministic**: Based on actual robot motion, not vision interpretation
3. **Reliable**: Orientation matches actual physical pick strategy used
4. **Trackable**: Orientation tracked through entire lifecycle
5. **Compatible**: Vision scan still occurs for visual confirmation

## Vision System Notes

- Vision system always provides orientation 0 for pick positions
- Vision still scans picked workpiece (Program 110)
- Scan results logged but not used for orientation determination
- Camera trigger at scan position maintained for visual effect
- This keeps the demonstration appealing while using robot-determined orientation

## Testing Recommendations

1. **Pick with Regular Orientation**: Verify orientation=0 is set when regular strategies succeed
2. **Pick with 180deg Rotation**: Verify orientation=1 is set when alternate strategies succeed
3. **Reference String**: Verify `getReferenceString()` returns correct "xy" format
4. **JSON Output**: Verify console shows orientation and referenceString fields
5. **Vision Scan**: Verify scan still occurs but doesn't override orientation
6. **Schunk Base**: Verify orientation is available when placing workpiece

## Migration Notes

- Existing workpieces in queue will have orientation=0 (default)
- New picks will have orientation set based on strategy
- Vision scan program (110) can still run but won't change orientation
- Console clients should use `referenceString` field for display
