---
applyTo: "src/biemhTekniker/programs/**/*.java"
---

# Instructions for Robot Motion Programs

These files contain the actual robot motion sequences and must follow strict safety and synchronization rules.

## Critical Rules for Motion Programs
- **Extend RoboticsAPIBackgroundTask**: All motion programs must extend `RoboticsAPIBackgroundTask` for background execution
- **PLC Handshake**: ALWAYS check PLC signals before starting motions (e.g., `Ready`, `AutoMode`)
- **Impedance Control**: Use `ImpedanceConfig.applyTo(motion)` for compliant motions when handling workpieces
- **Thread Safety**: Read shared data (from `WorkpieceQueue`, `MotionOverrides`) into local variables at the start
- **Error Recovery**: Implement proper exception handling and return robot to home position on failures
- **State Updates**: Update workpiece states in the queue after successful pick/place operations

## Common Patterns
```java
// 1. Background program structure
public class MyProgram extends RoboticsAPIBackgroundTask {
    @Override
    public void runTask() {
        // Lock, check PLC, execute motion, update state, unlock
    }
}

// 2. Reading shared data safely
Workpiece wp = WorkpieceQueue.getInstance().getNext();
if (wp == null) {
    return; // No work to do
}

// 3. PLC handshake pattern
if (!plcIO.getReady() || !plcIO.getAutoMode()) {
    getLogger().warn("PLC not ready");
    return;
}

// 4. Impedance control for compliant motion
IMotionContainer motion = lbr.move(lin(targetFrame).setMode(impedanceMode));
```

## Motion Types
- **PTP (Point-to-Point)**: Use for fast moves between waypoints, allows joint space motion
- **LIN (Linear)**: Use for precise Cartesian paths, required near workpieces/obstacles
- **Velocity**: Access via `ConfigManager.getInstance().getJointVelocity()`, typically 0.25 (25%)

## What NOT to Do
- Never hardcode positions or velocities - use `FrameRepository` and `ConfigManager`
- Never start motion without checking PLC signals first
- Never hold locks during long motions - motion should be lock-free
- Never modify workpiece queue state optimistically - only after successful completion
