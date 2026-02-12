# Copilot Instructions Setup - Summary

This repository now has comprehensive GitHub Copilot instructions configured according to best practices.

## Files Created/Updated

### 1. Repository-Wide Instructions
**File**: `.github/copilot-instructions.md` (Enhanced)

**Sections**:
- Repository Overview with project type, size, and key technologies
- Quick Reference - Key Files for fast navigation
- Technical Constraints (Java 1.7, concurrency, thread safety)
- Architecture & Patterns (separation of concerns, external config, PLC ownership)
- Hardware Integration (Sunrise API, I/O mapping, HMI)
- Setup and Build (detailed Sunrise.Workbench instructions)
- Testing and Deployment (validation methods, no CI/CD)
- Code Style and Conventions (naming, generated code, error handling)
- Project Structure (detailed directory layout)
- Python GUI (optional component details)
- Configuration Parameters (complete list)
- Common Pitfalls and Critical Warnings
- How to Verify Changes

**Improvements Made**:
- Added project metrics (50 Java files, 10 Python files, 8,000 LOC)
- Detailed build process explanation (Eclipse auto-build, bin/ output)
- Comprehensive testing approach documentation
- Added critical warnings section
- Added verification checklist
- Expanded from 83 to 156 lines

### 2. Path-Specific Instructions
Created `.github/instructions/` directory with specialized guidance:

#### a. `robot-programs.instructions.md`
**Applies to**: `src/biemhTekniker/programs/**/*.java`
**Content**:
- Critical rules for motion programs
- PLC handshake patterns
- Thread safety for motion code
- Common code patterns with examples
- Motion types (PTP vs LIN)
- What NOT to do

#### b. `python-gui.instructions.md`
**Applies to**: `gui/**/*.py`
**Content**:
- GUI architecture (console client, command protocol)
- Thread safety patterns (main thread, network thread, queue communication)
- Common patterns with code examples
- Visualization coordinate system and color coding
- Error handling for network issues

#### c. `configuration.instructions.md`
**Applies to**: `src/biemhTekniker/lib/config/**/*.java`
**Content**:
- Configuration management principles
- Singleton pattern implementation
- Property naming conventions
- CSV parsing patterns
- Unit conversions (degrees/radians, mm/meters)

## Benefits

1. **Faster Onboarding**: New Copilot sessions will understand the project immediately
2. **Better Code Quality**: Context-aware suggestions that respect Java 1.7 constraints
3. **Reduced Errors**: Warnings about common pitfalls (Java 8+ features, generated code)
4. **Specialized Guidance**: Path-specific instructions provide relevant context
5. **Comprehensive Coverage**: All aspects documented (build, test, deploy, verify)

## Usage

### For Repository-Wide Context
The `.github/copilot-instructions.md` file is automatically used by:
- GitHub Copilot Chat when the repository is attached
- Copilot coding agent when working on any file
- Copilot code review when reviewing pull requests

### For Path-Specific Context
When working on files matching the `applyTo` patterns, Copilot will additionally use:
- Motion program instructions for files in `src/biemhTekniker/programs/`
- GUI instructions for files in `gui/`
- Configuration instructions for files in `src/biemhTekniker/lib/config/`

## Enabling/Disabling

Custom instructions are enabled by default. To disable for code review:
1. Go to repository Settings
2. Navigate to Code & automation → Copilot → Code review
3. Toggle "Use custom instructions when reviewing pull requests"

## Maintenance

These instructions should be updated when:
- New major components are added to the project
- Build or deployment process changes
- New common pitfalls are discovered
- Project structure changes significantly

## Validation

To verify instructions are working:
1. Open GitHub Copilot Chat
2. Attach this repository
3. Ask a question about the project
4. Check the References section - should show `copilot-instructions.md`
5. Verify responses respect Java 1.7 constraints and other guidelines

## References

- [GitHub Copilot Custom Instructions Documentation](https://docs.github.com/en/copilot/customizing-copilot/adding-custom-instructions-for-github-copilot)
- [Best Practices for Repository Instructions](https://docs.github.com/en/copilot/concepts/prompting/response-customization)
