# Testing Guide - Orientation Strategy Changes

## Overview

This guide provides testing procedures to validate the orientation strategy changes where the robot now determines workpiece orientation based on the successful pick strategy.

## Testing Prerequisites

1. KUKA LBR iiwa 14 R820 robot operational
2. Vision system (SmartPicking) connected
3. Console GUI connected for monitoring
4. Workpieces in bin for all three references
5. Sunrise.Workbench for log monitoring (optional)

## Test Scenarios

### Test 1: Regular Pick (Orientation 0)

**Objective:** Verify orientation 0 is set when regular pick strategy succeeds

**Steps:**
1. Clear workpiece queue (restart or wait for empty)
2. Run Program 109 (Full Scan)
3. Run Program 1 (Pick New Workpiece)
4. Monitor console/logs for orientation determination

**Expected Results:**
- Log message: "Workpiece picked with orientation 0 (regular)"
- Workpiece data shows: `ref=10`, `ref=20`, or `ref=30` (depending on reference)
- JSON output shows: `"orientation": 0` and `"referenceString": "10"` (or 20/30)
- Vision scan still executes but doesn't change orientation
- Log shows: "robot orientation=0" after scan

**Success Criteria:**
- ✅ Orientation set to 0 immediately after pick
- ✅ Reference string in "X0" format
- ✅ Vision scan completes without overriding orientation

---

### Test 2: 180deg Pick (Orientation 1)

**Objective:** Verify orientation 1 is set when alternate pick strategy succeeds

**Setup:**
- Position workpiece in bin such that regular strategies fail
- Or modify configuration to skip regular strategies (not recommended for production)

**Steps:**
1. Position workpiece at edge/corner of bin
2. Run Program 109 (Full Scan)
3. Run Program 1 (Pick New Workpiece)
4. Wait for regular strategies to fail
5. Monitor when alternate strategy succeeds

**Expected Results:**
- Log message: "Motion failed with... (regular)" for first attempts
- Log message: "Motion succeeded with... (alternate)"
- Log message: "Workpiece picked with orientation 1 (180deg rotation)"
- Workpiece data shows: `ref=11`, `ref=21`, or `ref=31`
- JSON output shows: `"orientation": 1` and `"referenceString": "11"` (or 21/31)

**Success Criteria:**
- ✅ Orientation set to 1 when alternate strategy succeeds
- ✅ Reference string in "X1" format
- ✅ Pick motion actually uses 180deg rotation

---

### Test 3: Multiple References

**Objective:** Verify orientation tracking works across all three references

**Steps:**
1. Place workpieces from all three references in bin
2. Run Program 109 (Full Scan)
3. Run Program 1 multiple times to pick from different references
4. Monitor orientation for each reference

**Expected Results:**
- Reference 1 workpieces: "10" (regular) or "11" (180deg)
- Reference 2 workpieces: "20" (regular) or "21" (180deg)
- Reference 3 workpieces: "30" (regular) or "31" (180deg)
- Each workpiece independently tracked

**Success Criteria:**
- ✅ Each reference correctly shows index (1, 2, or 3)
- ✅ Each workpiece independently shows orientation (0 or 1)
- ✅ Reference string correctly combines both values

---

### Test 4: Console JSON Output

**Objective:** Verify console reports correct orientation data

**Steps:**
1. Connect console GUI to robot
2. Send command: `{"type": "get_workpieces"}`
3. Examine JSON response

**Expected JSON Structure:**
```json
{
  "type": "workpieces",
  "workpieces": "[
    {
      \"id\": 1234567890,
      \"reference\": 1,
      \"orientation\": 0,
      \"referenceString\": \"10\",
      \"state\": \"PICKED\",
      \"gripper\": \"A\",
      \"x\": 450.2,
      \"y\": 123.5,
      \"z\": 45.8,
      \"rz\": 90.5,
      \"score\": 0.95
    }
  ]"
}
```

**Success Criteria:**
- ✅ `reference` field present (1-3)
- ✅ `orientation` field present (0-1)
- ✅ `referenceString` field present ("10"-"31")
- ✅ All three fields consistent with each other

---

### Test 5: Vision Scan Visual Effect

**Objective:** Verify vision scan still executes but doesn't override orientation

**Steps:**
1. Run Program 1 (Pick New Workpiece)
2. Note orientation set after pick
3. Wait for robot to move to scan position
4. Observe vision scan execution (Program 110)
5. Check orientation after scan

**Expected Results:**
- Orientation set immediately after pick: "Workpiece picked with orientation X"
- Robot moves to scan position
- Vision scan executes: "Requesting workpiece orientation"
- Log shows both orientations: "vision orientation=Y, robot orientation=X"
- Final message: "keeping robot-determined orientation"
- Orientation unchanged after scan

**Success Criteria:**
- ✅ Orientation set during pick operation
- ✅ Vision scan completes successfully
- ✅ Orientation remains unchanged after scan
- ✅ Log shows comparison between vision and robot orientation

---

### Test 6: Pick-Measure-Return Cycle

**Objective:** Verify orientation persists through entire lifecycle

**Steps:**
1. Run Program 109 (Full Scan)
2. Run Program 1 (Pick New Workpiece) - note orientation
3. Run Program 2 (Place New Workpiece)
4. Run Program 3 (Pick Measured Workpiece)
5. Run Program 4 (Place Measured Workpiece)
6. Query workpiece queue at each step

**Expected Results:**
- Orientation set during step 2
- Orientation preserved in step 3
- Orientation preserved in step 4
- Orientation preserved in step 5
- Reference string consistent throughout

**Success Criteria:**
- ✅ Orientation set once during initial pick
- ✅ Orientation unchanged through all operations
- ✅ Reference string consistent in all log messages
- ✅ JSON output shows same orientation at all states

---

## Validation Checklist

### Code Validation
- [x] Java 1.7 compatibility (no lambdas, streams, diamond operators)
- [x] No Unicode characters in source code
- [x] Thread-safe operations (synchronized accessors)
- [x] Null pointer checks
- [x] Exception handling
- [x] CodeQL security scan passed

### Functional Validation
- [ ] Test 1: Regular pick (orientation 0) - PENDING
- [ ] Test 2: 180deg pick (orientation 1) - PENDING
- [ ] Test 3: Multiple references - PENDING
- [ ] Test 4: Console JSON output - PENDING
- [ ] Test 5: Vision scan visual effect - PENDING
- [ ] Test 6: Full lifecycle - PENDING

### Integration Validation
- [ ] Pick operations work correctly
- [ ] Place operations work correctly
- [ ] Console communication working
- [ ] Vision system communication working
- [ ] PLC handshaking working
- [ ] No performance degradation

## Troubleshooting

### Issue: Orientation always 0

**Possible Causes:**
- Regular strategies always succeed (this is expected behavior)
- Workpieces positioned for easy access

**Verification:**
- Check if alternate strategies are being tried
- Look for "Motion failed with... (regular)" messages
- Position workpiece at bin edge to force alternate strategy

### Issue: Orientation not showing in JSON

**Possible Causes:**
- Console client not updated
- Caching old JSON structure

**Resolution:**
- Restart console server (Program 0)
- Reconnect console client
- Clear any client-side caching

### Issue: Vision scan changes orientation

**Possible Causes:**
- Old version of ScanPickedWorkpiece
- Code not deployed to controller

**Resolution:**
- Verify latest code is deployed
- Check log message: "keeping robot-determined orientation"
- Restart application if needed

## Test Results Log Template

```
Date: ____________
Tester: ____________
Robot Serial: ____________
Software Version: ____________

Test 1 - Regular Pick:
[ ] PASS [ ] FAIL
Notes: ________________________________________________

Test 2 - 180deg Pick:
[ ] PASS [ ] FAIL
Notes: ________________________________________________

Test 3 - Multiple References:
[ ] PASS [ ] FAIL
Notes: ________________________________________________

Test 4 - Console JSON:
[ ] PASS [ ] FAIL
Notes: ________________________________________________

Test 5 - Vision Scan:
[ ] PASS [ ] FAIL
Notes: ________________________________________________

Test 6 - Full Lifecycle:
[ ] PASS [ ] FAIL
Notes: ________________________________________________

Overall Result: [ ] PASS [ ] FAIL
Comments: ______________________________________________
```

## Next Steps After Testing

1. Document any issues found
2. Update configuration if needed
3. Train operators on new orientation display
4. Update external systems (PLC, Schunk base) to use new reference string format
5. Monitor production for first week
6. Collect feedback from operators
