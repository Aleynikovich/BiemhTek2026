# Impedance Control Implementation

## Overview

This document describes the impedance control implementation for the KUKA LBR iiwa robot. Impedance control makes the robot compliant and safe by allowing it to yield to external forces, which is essential for collaborative applications and interaction with grippers and workpieces.

## What is Impedance Control?

Impedance control is a control strategy that allows the robot to behave like a spring-damper system. Instead of rigidly following commanded positions, the robot can deflect when external forces are applied. This provides:

- **Safety**: The robot yields to unexpected collisions
- **Compliance**: The robot can absorb forces from grippers, fixtures, and workpieces
- **Gentle contact**: Reduced impact forces during contact operations

## Implementation Details

### Configuration

Impedance control parameters are configured in `configs/application.properties`:

```properties
# Impedance Control Parameters (for compliance and safety)
# Enable/disable impedance control
impedance.enabled=true

# Cartesian stiffness values [N/m for translation, Nm/rad for rotation]
# Lower values = more compliant, Higher values = more rigid

# Translation stiffness (X, Y, Z) - range: 0-5000 N/m
impedance.stiffness.x=1000
impedance.stiffness.y=1000
impedance.stiffness.z=800

# Rotational stiffness (A, B, C) - range: 0-300 Nm/rad
impedance.stiffness.a=150
impedance.stiffness.b=150
impedance.stiffness.c=150

# Damping values for all axes (dimensionless) - range: 0.1-1.0
impedance.damping=0.7
```

### Default Values

The implementation uses lower stiffness values than KUKA defaults for better compliance:

| Parameter | Default Value | KUKA Default | Unit |
|-----------|--------------|--------------|------|
| X Translation | 1000 | 2000 | N/m |
| Y Translation | 1000 | 2000 | N/m |
| Z Translation | 800 | 2000 | N/m |
| A Rotation | 150 | 200 | Nm/rad |
| B Rotation | 150 | 200 | Nm/rad |
| C Rotation | 150 | 200 | Nm/rad |
| Damping | 0.7 | 0.7 | - |

### Components

1. **ImpedanceConfig** (`src/biemhTekniker/config/ImpedanceConfig.java`)
   - Loads impedance parameters from configuration
   - Creates `CartesianImpedanceControlMode` instances
   - Validates parameter ranges according to KUKA specifications

2. **MotionStrategy** (`src/biemhTekniker/programs/MotionStrategy.java`)
   - Extended to accept an optional `CartesianImpedanceControlMode` parameter
   - Applies impedance control to all motion commands (PTP and LIN)
   - Logs when impedance control is active

3. **MotionStrategyGenerator** (`src/biemhTekniker/programs/MotionStrategyGenerator.java`)
   - Automatically creates strategies with impedance control when enabled
   - All generated strategies include impedance mode if configured

## Usage

### Automatic Application

Impedance control is automatically applied to all robot motions when enabled in configuration. No code changes are required in robot programs.

All existing programs that use `MotionStrategyGenerator` will automatically benefit from impedance control:
- `PickMeasuredWorkpieceProgram`
- `PlaceMeasuredWorkpieceProgram`
- Any custom programs using motion strategies

### Manual Control

To disable impedance control for specific operations, set `impedance.enabled=false` in the configuration file.

### Verification

Check the robot logs for impedance control status:
```
[INFO] Impedance control ENABLED
[INFO]   Stiffness: X=1000.0 Y=1000.0 Z=800.0 A=150.0 B=150.0 C=150.0
[INFO]   Damping: 0.7
```

Motion logs will include `[impedance]` tag when active:
```
[INFO] Attempting motion with tcp=TCPB (regular) [tool-coord] [impedance]: Frame[...]
```

## Tuning Guidelines

### Increasing Compliance (Lower Stiffness)

For more compliant behavior:
- Decrease translational stiffness values (e.g., 500-800 N/m)
- Decrease rotational stiffness values (e.g., 100-150 Nm/rad)

Use cases:
- Delicate assembly operations
- Workpieces with high grip forces
- Enhanced safety during collaborative operations

### Increasing Rigidity (Higher Stiffness)

For more rigid behavior:
- Increase translational stiffness values (e.g., 1500-2000 N/m)
- Increase rotational stiffness values (e.g., 200-250 Nm/rad)

Use cases:
- Precise positioning requirements
- Heavy payload handling
- High-speed operations

### Adjusting Damping

- Lower damping (0.3-0.5): Faster response, may oscillate
- Higher damping (0.8-1.0): Slower response, more stable

Standard value (0.7) provides good balance for most applications.

## Safety Considerations

1. **Testing**: Always test impedance settings in simulation or with low speeds first
2. **Workspace**: Ensure sufficient clearance around the robot when using low stiffness
3. **Monitoring**: Watch for unexpected robot deflections that may indicate:
   - Stiffness values too low for the application
   - External forces higher than expected
   - Collision or interference issues

## Example: Gripper Force Compliance

The issue description mentions that "when we pick/place at gripper3, the force that gripper3 exerts on the robot when it grips the workpiece, moves the robot a bit."

With impedance control enabled:
1. The robot approaches the pick position
2. The gripper closes and applies force to the workpiece
3. The robot complies with the force, allowing slight movement
4. This prevents excessive stress on the workpiece and gripper
5. The robot maintains safe, compliant behavior

## Technical References

- KUKA Sunrise OS API: `CartesianImpedanceControlMode`
- Documentation: `Documentation/KUKA_SunriseOS_116_END_en.pdf`
- Example implementation: `Documentation/BinPicking_EKI.java`

## Troubleshooting

### Impedance Not Active

Check:
1. `impedance.enabled=true` in `application.properties`
2. Configuration files loaded correctly (check startup logs)
3. `ImpedanceConfig` initialization successful

### Robot Too Compliant

- Increase stiffness values
- Check for excessive external forces
- Verify gripper force settings

### Robot Too Rigid

- Decrease stiffness values
- Verify impedance control is enabled
- Check that strategies are created with impedance mode

## Future Enhancements

Possible future improvements:
1. Dynamic impedance adjustment based on operation type
2. Different impedance profiles for pick vs place operations
3. Adaptive impedance based on measured forces
4. Per-axis impedance tuning for specific applications
