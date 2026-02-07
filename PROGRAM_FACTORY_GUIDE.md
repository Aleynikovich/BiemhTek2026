# Program Factory Configuration Guide

## Overview

This project uses a configurable program factory system that allows you to modify program-to-factory mappings without recompiling code. The system uses a properties file to map program IDs to factory classes.

## Configuration File

The program mappings are defined in `programs.properties` at the project root.

### Default Location
- `./programs.properties` (relative to the robot controller working directory)

### Custom Location
You can override the default location using a system property:
```
-Dbiemh.programs.config=/path/to/custom/programs.properties
```

## File Format

The properties file uses a simple key-value format:
```properties
# Program ID = Fully Qualified Factory Class Name
1=biemhTekniker.factories.GetNewWorkpieceFactory
2=biemhTekniker.factories.CalibrationFactory
4=biemhTekniker.factories.PickNewWorkpieceFactory
```

- **Key**: Program ID (integer)
- **Value**: Fully qualified class name of the factory implementing `ProgramFactory`

## Adding a New Program

To add a new program without modifying Main.java:

### Step 1: Create the Program Class (if needed)
If your program class doesn't exist yet, create it following the existing pattern:

```java
package biemhTekniker.programs;

public class MyNewProgram {
    private final RoboticsAPIApplication application;
    private final LBR iiwa;
    // ... other dependencies
    
    public MyNewProgram(RoboticsAPIApplication application, LBR iiwa) {
        this.application = application;
        this.iiwa = iiwa;
    }
    
    public boolean execute() {
        // Program logic here
        return true;
    }
}
```

### Step 2: Create a Factory Class
Create a factory in `src/biemhTekniker/factories/`:

```java
package biemhTekniker.factories;

import biemhTekniker.programs.MyNewProgram;
import biemhTekniker.programs.ProgramAdapter;
import biemhTekniker.programs.ProgramContext;
import biemhTekniker.programs.ProgramFactory;

public class MyNewProgramFactory implements ProgramFactory {
    // Required: public no-arg constructor for reflection
    public MyNewProgramFactory() {
    }
    
    public ProgramAdapter create(final ProgramContext ctx) {
        return new ProgramAdapter() {
            public boolean execute() {
                MyNewProgram program = new MyNewProgram(
                    ctx.getApplication(), 
                    ctx.getIiwa()
                );
                return program.execute();
            }
        };
    }
}
```

### Step 3: Update programs.properties
Add a mapping in `programs.properties`:
```properties
8=biemhTekniker.factories.MyNewProgramFactory
```

### Step 4: Deploy
- Copy the updated `programs.properties` file to the robot controller
- Deploy your application with the new factory class included
- The new program is now available as Program ID 8

## Available Dependencies in ProgramContext

The `ProgramContext` object provides access to all robot dependencies:

- `getApplication()` - RoboticsAPIApplication instance
- `getIiwa()` - LBR robot instance
- `getGripper()` - Tool gripper instance
- `getGripperIO()` - MediaFlangeIOGroup for gripper I/O
- `getWorkpieceData()` - Shared workpiece data from vision system
- `getVisionProtocol()` - SmartPickingProtocol for camera communication

## Fallback Behavior

If `programs.properties` is not found or a mapping is missing, the system automatically falls back to the hard-coded program registration in Main.java. This ensures backward compatibility and fail-safe operation.

## Troubleshooting

### Factory Not Found
**Error**: "Factory class not found for program ID X"
- Ensure the factory class is compiled and included in the deployed JAR
- Verify the fully qualified class name is correct in programs.properties

### Instantiation Failed
**Error**: "Failed to instantiate factory for program ID X"
- Ensure your factory class has a public no-arg constructor
- Check that the factory class is not abstract
- Verify the factory implements the ProgramFactory interface

### Configuration File Not Loaded
**Warning**: "Failed to load program factory configuration"
- Check that programs.properties exists in the working directory
- Verify file permissions allow reading
- Use `-Dbiemh.programs.config=<path>` to specify a custom location

## Example programs.properties

```properties
# Program ID to Factory Class Mapping
# Format: programId=fully.qualified.FactoryClassName

# Vision and Calibration Programs
1=biemhTekniker.factories.GetNewWorkpieceFactory
2=biemhTekniker.factories.CalibrationFactory

# Pick and Place Programs
4=biemhTekniker.factories.PickNewWorkpieceFactory
5=biemhTekniker.factories.PlaceNewWorkpieceFactory
6=biemhTekniker.factories.PickMeasuredWorkpieceFactory
7=biemhTekniker.factories.PlaceMeasuredWorkpieceFactory
```

## Future Enhancements

The current implementation uses a properties file for simplicity in the robot controller environment. Future enhancements could include:

- Database-backed configuration (SQLite or PostgreSQL)
- Hot-reloading of configuration without restart
- Web-based configuration UI
- Program versioning and rollback support
