package biemhTekniker.programs;

/**
 * Defines valid program number ranges.
 * Robot programs: 1-99 (execute on main robot thread, synchronous)
 * Vision programs: 100-199 (execute on vision thread, asynchronous)
 */
public class ProgramRange
{
    public static final int IDLE = 0;

    public static final int ROBOT_MIN = 1;
    public static final int ROBOT_MAX = 99;

    public static final int VISION_MIN = 100;
    public static final int VISION_MAX = 199;

    /**
     * Checks if a program number is a valid robot program.
     *
     * @param programNumber Program number to check
     * @return true if in range 1-99
     */
    public static boolean isRobotProgram(int programNumber)
    {
        return programNumber >= ROBOT_MIN && programNumber <= ROBOT_MAX;
    }

    /**
     * Checks if a program number is a valid vision program.
     *
     * @param programNumber Program number to check
     * @return true if in range 100-199
     */
    public static boolean isVisionProgram(int programNumber)
    {
        return programNumber >= VISION_MIN && programNumber <= VISION_MAX;
    }

    /**
     * Checks if a program number is valid (0-199).
     *
     * @param programNumber Program number to check
     * @return true if valid
     */
    public static boolean isValid(int programNumber)
    {
        return programNumber == IDLE || isRobotProgram(programNumber) || isVisionProgram(programNumber);
    }
}
