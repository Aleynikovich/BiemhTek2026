# Workpiece State Model

## Overview
This document describes the simplified workpiece state model that eliminates unnecessary states and uses gripper location to track workpiece handling.

## States

### WorkpieceState Enum
The system uses only 3 states:

1. **AVAILABLE** - Workpiece is on the table, ready to be picked
2. **MEASURING** - Workpiece is on the measuring machine
3. **MEASURED** - Measuring is complete, ready to be removed from machine

### Gripper Location Field
Separately from the state, each workpiece has a `gripperLocation` field that can be:
- `null` - Not held by any gripper
- `"1"` - Held by Gripper 1
- `"2"` - Held by Gripper 2  
- `"3"` - Held by Gripper 3

## State Transitions

### Normal Workflow
```
AVAILABLE (on table, gripper=null)
    ↓ [pick]
AVAILABLE (held, gripper="1")
    ↓ [place on measuring machine]
MEASURING (on machine, gripper=null)
    ↓ [measurement complete]
MEASURED (on machine, gripper=null)
    ↓ [pick from machine]
MEASURED (held, gripper="2")
    ↓ [return to table]
AVAILABLE (on table, gripper=null)
```

## GUI Display Logic

The GUI displays workpiece state based on both the `state` and `gripper` fields:

- If `gripper` is set (1, 2, or 3): Display **"In Gripper X"**
- If `gripper` is null: Display the actual state (AVAILABLE, MEASURING, MEASURED)

### Examples
| State | Gripper | GUI Display |
|-------|---------|-------------|
| AVAILABLE | null | AVAILABLE |
| AVAILABLE | "1" | In Gripper 1 |
| MEASURING | null | MEASURING |
| MEASURED | null | MEASURED |
| MEASURED | "2" | In Gripper 2 |

## API Methods

### WorkpieceQueue Methods

#### Picking Operations
- `takeNextForPicking(int gripperNumber)` - Gets best AVAILABLE workpiece and sets its gripper location
- `peekNextForPicking()` - Preview next workpiece without changing state
- `markPicked(long workpieceId, int gripperNumber)` - Marks a specific workpiece with gripper location

#### State Transitions
- `markMeasuring(long workpieceId)` - Sets state to MEASURING, clears gripper
- `markMeasured(long workpieceId)` - Sets state to MEASURED
- `markReturned(long workpieceId)` - Sets state to AVAILABLE, clears gripper
- `clearGripper(long workpieceId)` - Clears gripper location without changing state

#### Queries
- `getPickedWorkpiece(int gripperNumber)` - Find workpiece held by specific gripper (0 for any)
- `takeMeasuredWorkpiece()` - Gets MEASURED workpiece and clears gripper

## Benefits of New Model

1. **Clarity** - State directly shows where workpiece is (table vs machine)
2. **Gripper Tracking** - Separate field makes it clear which gripper holds which workpiece
3. **Simplicity** - Only 3 states instead of 5
4. **Flexibility** - Easy to add more grippers without changing state enum
5. **No Ambiguity** - "PICKED" was vague; "In Gripper 2" is explicit

## Migration Notes

### Removed States
- **PICKED** - Replaced by gripper location field
- **RETURNED** - Replaced by AVAILABLE (returned workpieces are just available again)

### Code Changes Required
- Any code that checked for `WorkpieceState.PICKED` should check gripper location instead
- Any code that checked for `WorkpieceState.RETURNED` should check for `AVAILABLE` with `gripper == null`
- Methods that set state to PICKED should now set gripper location
- No deprecated/legacy methods remain in the codebase
