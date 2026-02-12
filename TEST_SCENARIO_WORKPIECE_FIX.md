# Test Scenario: Workpiece Gripper State Fix

## Issue Fixed
**Problem**: Robot keeps attempting to pick already-picked workpieces
**Root Cause**: Vision system was clearing gripper location when scanning AVAILABLE workpieces

## Testing Instructions

### Prerequisites
1. KUKA Sunrise.Workbench environment configured
2. Vision system operational and connected
3. At least 3 workpieces in the bin/table area

### Test Case 1: Normal Pick Flow (Should Work)
**Objective**: Verify that picked workpieces are no longer reset by vision scans

**Steps**:
1. Start the robot application
2. Trigger a vision scan (full scan) - should detect workpieces on table
3. Start pick program with Gripper 1
4. **WAIT** - Allow robot to pick the first workpiece (state: AVAILABLE, gripper="1")
5. Trigger another vision scan while robot holds the workpiece
6. **EXPECTED**: Vision scan should log: "Skipping vision update for workpiece held by gripper 1: id=X"
7. **VERIFY**: Workpiece should remain in gripper="1" state (check GUI or logs)
8. Complete the place operation normally

**Expected Result**:
- ✅ Robot picks workpiece successfully
- ✅ Vision scan does NOT reset gripper state
- ✅ Robot does NOT attempt to pick the same workpiece again
- ✅ Workpiece remains in gripper until explicitly placed

### Test Case 2: Multiple Workpieces (Should Work)
**Objective**: Verify multiple workpieces can be picked without interference

**Steps**:
1. Place 3 workpieces in the bin
2. Trigger vision scan - should detect all 3 workpieces (state: AVAILABLE, gripper=null)
3. Pick first workpiece with Gripper 1 (state: AVAILABLE, gripper="1")
4. Trigger vision scan - should detect 2 remaining workpieces, skip the one in gripper
5. **EXPECTED**: 
   - Workpiece in Gripper 1: No update, remains gripper="1"
   - Other 2 workpieces: Can be updated by vision if needed
6. Complete the workflow

**Expected Result**:
- ✅ All 3 workpieces detected initially
- ✅ Picked workpiece is NOT reset by subsequent scans
- ✅ Other workpieces can still be updated by vision
- ✅ Robot picks different workpieces sequentially

### Test Case 3: Workpiece Return Flow (Should Work)
**Objective**: Verify returned workpieces can be picked again

**Steps**:
1. Pick a workpiece with Gripper 1 (state: AVAILABLE, gripper="1")
2. Place workpiece on measuring machine (state: MEASURING, gripper="3")
3. Trigger measurement
4. Pick measured workpiece with Gripper 2 (state: MEASURED, gripper="2")
5. Return workpiece to table - call `markReturned()` (state: AVAILABLE, gripper=null)
6. Trigger vision scan
7. **EXPECTED**: Vision scan CAN update this workpiece now (gripper=null)
8. Pick the returned workpiece again

**Expected Result**:
- ✅ Returned workpiece has gripper=null
- ✅ Vision scan updates returned workpiece position/orientation
- ✅ Robot can pick the workpiece again

### Test Case 4: Vision Update Logs (Verification)
**Objective**: Verify correct log messages

**Steps**:
1. Monitor the robot console logs during operations
2. Look for these specific log messages:

**When vision finds workpiece on table** (gripper=null):
```
INFO: Updated existing workpiece with vision data: id=X, ref=Y, score=Z
```

**When vision finds workpiece held by gripper** (gripper="1", "2", or "3"):
```
DEBUG: Skipping vision update for workpiece held by gripper X: id=Y
```

**Expected Result**:
- ✅ Correct log messages appear
- ✅ No "Clearing stale gripper location" messages (this was the bug)

## Key Validation Points

### Code Changes
- **File**: `src/biemhTekniker/lib/data/WorkpieceQueue.java`
- **Method**: `addOrUpdateWorkpiece()` at lines 469-495

### Before Fix (BUGGY CODE):
```java
if (existing.getState() == WorkpieceState.AVAILABLE) {
    existing.set(x, y, z, rx, ry, rz, score);
    if (existing.getGripperLocation() != null) {
        existing.setGripperLocation(null); // ❌ BUG: Clears picked state!
    }
}
```

### After Fix (CORRECT CODE):
```java
if (existing.getState() == WorkpieceState.AVAILABLE) {
    if (existing.getGripperLocation() != null) {
        return existing; // ✅ Skip update, preserve gripper state
    }
    existing.set(x, y, z, rx, ry, rz, score); // ✅ Only update table workpieces
}
```

## Troubleshooting

### If robot still picks same workpiece twice:
1. Check logs for "Skipping vision update" message - if missing, fix not applied
2. Verify `markPicked()` is called after successful pick
3. Check if workpiece position tolerance is too large (causing wrong matches)

### If workpiece positions not updating:
1. This is expected for picked workpieces (gripper != null)
2. For table workpieces (gripper=null), vision should still update
3. Check vision system is detecting workpieces correctly

## Success Criteria
- ✅ No "Clearing stale gripper location" log messages
- ✅ Picked workpieces maintain gripper state across vision scans
- ✅ Robot picks different workpieces, not the same one repeatedly
- ✅ Normal workflow completes without errors
- ✅ GUI shows correct gripper states ("In Gripper X" for picked workpieces)
