### BiemhTek2026 Refactoring & Digital Twin - Status Report

#### 1. What was done (Java Backend)

- **Motion Overrides**: Created `biemhTekniker.lib.robot.motions.MotionOverrides` to store runtime forced parameters (
  Workpiece ID, redundancy offsets, Z rotation angles).
- **MotionStrategyGenerator**: Updated to prioritize `MotionOverrides` before reading static configuration files.
- **WorkpieceQueue**: Exposed `getById(long)` method to retrieve workpieces without modifying their state.
- **Forced Pick**: Updated `PickNewWorkpieceProgram` to check for a forced ID from the console. If set, it attempts to
  pick that specific workpiece instead of the next in queue.
- **Gripper States**:
    - Extended `ConsoleServerInterface` with `isGripper1Closed()`, `isGripper2Closed()`, `isGripper3Closed()`.
    - Implemented these in `AppController` (reading from `MediaFlangeIOGroup`) and delegated in `Main`.
- **Console Commands**: Added new JSON commands in `ConsoleCommandHandler`:
    - `pick_specific_workpiece`: Sets the forced ID and starts program 1.
    - `set_motion_override`: Accepts `redundancy` and `zrot` CSV strings (degrees) and converts them to radians for the
      generator.
    - `clear_motion_override`: Resets overrides to default.
    - Updated `status` command to include `gripper1_closed`, `gripper2_closed`, `gripper3_closed`.

#### 2. What was done (Python GUI)

- **Digital Twin (GripperPanel)**:
    - Added LED indicators (circles) that turn green when a gripper is closed and gray when open.
    - Added `set_gripper_states` method to update visuals based on robot status.
- **Correct 3D->2D Visualization**:
    - Updated `WorkpieceCanvas` to use **full Rx, Ry, Rz (Intrinsic Rz->Ry->Rx)** transformation.
    - The code now calculates the projection of local X+ and Y+ basis vectors onto the table plane.
    - The yellow arrow and workpiece rectangle now correctly reflect tilts and rotations, solving the "incorrect X+
      arrow" issue.

#### 3. What is LEFT to do

- **GUI Controls (Wiring)**:
    - **Pick Selected Button**: Add a button in the "Workpieces" tab. When clicked, it should find the full ID of the
      selected row and send `{"type": "pick_specific_workpiece", "id": <full_id>}`.
    - **Override UI**: Add two text entries for "Forced Redundancy" and "Forced Z-Rot" with an "Apply" button sending
      `{"type": "set_motion_override", "redundancy": "...", "zrot": "..."}` and a "Clear" button.
- **Gripper State Loop**:
    - In `robot_control_gui.py`, the `update_status_ui` method needs to extract `gripper1_closed` (etc.) from the
      response and call `self.gripper_panel.set_gripper_states(g1, g2, g3)`.
- **Testing**:
    - Verify that the forced pick actually consumes the ID from `MotionOverrides` in the backend.
    - Verify that the CSV parsing in `ConsoleCommandHandler` handles different locales or empty strings gracefully.

#### 4. Files Modified

- **Java**: `MotionOverrides.java` (new), `MotionStrategyGenerator.java`, `WorkpieceQueue.java`,
  `PickNewWorkpieceProgram.java`, `ConsoleServerInterface.java`, `AppController.java`, `Main.java`,
  `ConsoleCommandHandler.java`.
- **Python**: `gui/modules/gripper_panel.py`, `gui/modules/workpiece_canvas.py`.
