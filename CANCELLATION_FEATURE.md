# Program Cancellation Feature

## Overview
This document describes the implementation of the program cancellation feature that allows operators to safely cancel a running robot program and return the robot to home position without opening grippers (preserving any workpiece that may be held).

## Changes Made

### Java Backend Changes

#### 1. RobotContext.java
- Added `volatile boolean cancellationRequested` flag for thread-safe cancellation signaling
- Added `requestCancellation()` method to set the cancellation flag
- Added `isCancellationRequested()` method to check the flag
- Added `clearCancellation()` method to reset the flag before starting new programs

#### 2. ConsoleServerInterface.java
- Added `cancelCurrentProgram()` method to the interface

#### 3. AppController.java
- Updated constructor to accept `RobotContext` and `HomePositionManager` references
- Implemented `cancelCurrentProgram()` method that:
  - Sets cancellation flag in RobotContext
  - Resets program number to 0 (idle)
  - Requests home position move

#### 4. ConsoleCommandHandler.java
- Added handling for `cancel_program` command type
- Implemented `handleCancelProgram()` method that calls `serverInterface.cancelCurrentProgram()`

#### 5. Main.java
- Updated AppController instantiation to pass RobotContext and HomePositionManager
- Added cancellation flag clearing before dispatching each program
- Implemented `cancelCurrentProgram()` method to delegate to AppController

#### 6. Robot Programs (PickNewWorkpieceProgram.java, PlaceNewWorkpieceProgram.java)
- Added cancellation checks at strategic points:
  - Before starting motion sequences
  - Between motion strategies
  - Before final positioning moves
- Programs throw exception with "Program cancelled by user" message when cancellation is detected

### Python GUI Changes

#### 1. Added Cancel Button
- New "Cancel & Return Home" button in Quick Actions section
- Positioned alongside Emergency Stop button for easy access
- Sends `cancel_program` command to robot controller

#### 2. Layout Improvements
- **Reduced vertical spacing**: Changed padding from 10px to 5px throughout
- **Smaller fonts**: Title reduced from 16pt to 14pt, headers from 12pt to 10pt, status from 10pt to 9pt
- **Compact buttons**: Reduced button width from 25 to 22 characters, padding from 5px to 3px
- **Increased console height**: From 15 to 20 lines (now possible due to space savings)

#### 3. Console Logger Enhancements
- **Alternating row colors**: White and light grey (#f0f0f0) backgrounds
- **Better readability**: Easier to track individual log lines
- **Line counter**: Tracks odd/even lines for alternating colors
- **Tag priority**: Text color (error/warning/info) takes precedence over background

## Usage

### From GUI
1. Click the "Cancel & Return Home" button during any program execution
2. The robot will:
   - Stop executing the current program at the next safe checkpoint
   - Keep grippers closed (preserving any workpiece)
   - Return to home position automatically

### From Console/API
Send JSON command:
```json
{
  "type": "cancel_program"
}
```

## Safety Features

1. **No gripper opening**: Grippers remain in their current state to preserve workpieces
2. **Safe checkpoints**: Cancellation is checked between motion steps, not during motions
3. **Home position return**: Robot automatically returns to home after cancellation
4. **Clean state**: Cancellation flag is cleared before starting next program

## Technical Details

### Cancellation Timing
Programs check for cancellation at these points:
- Before starting any motion sequence
- Between motion strategy attempts
- Before final positioning moves
- Not during active motion commands (safety consideration)

### Thread Safety
- Uses `volatile` keyword for the cancellation flag
- Ensures visibility across threads without explicit locking
- Safe for use from console handler thread and main robot thread

### Error Handling
- Cancelled programs throw exception with clear message
- Exception is caught and logged in Main.java dispatch method
- Program resets to idle (0) automatically
- Home position move is requested

## GUI Layout Comparison

### Before
- Larger fonts (Title: 16pt, Headers: 12pt, Status: 10pt)
- More padding (10px frames, 5px button spacing)
- Wider buttons (25 characters)
- Console height: 15 lines
- No alternating row colors
- Total estimated height: ~1000px

### After
- Smaller fonts (Title: 14pt, Headers: 10pt, Status: 9pt)
- Less padding (5px frames, 3px button spacing)
- Narrower buttons (22 characters)
- Console height: 20 lines
- Alternating row colors (white/light grey)
- Total estimated height: ~900px (more space for console)

## Testing Recommendations

1. **Cancellation during Pick**: Test cancelling while picking a workpiece
2. **Cancellation during Place**: Test cancelling while placing a workpiece
3. **Multiple cancellations**: Verify flag resets properly between programs
4. **GUI responsiveness**: Verify cancel button works immediately
5. **Home position**: Verify robot returns home after cancellation
6. **Gripper state**: Verify grippers don't open unexpectedly
7. **Console readability**: Verify alternating colors improve readability

## Notes

- The cancellation mechanism is cooperative - programs must check the flag
- Not all programs have been updated yet (only Pick/Place examples shown)
- Vision programs (100-199) may need separate handling
- Consider adding timeout mechanism for programs that don't check cancellation
