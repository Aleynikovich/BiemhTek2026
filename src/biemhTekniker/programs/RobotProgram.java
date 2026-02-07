package biemhTekniker.programs;

/**
 * Interface for robot programs.
 * Defines the contract for all executable robot programs.
 * Java 1.7 compatible.
 */
public interface RobotProgram
{
    /**
     * Executes the robot program.
     *
     * @return true if execution succeeded, false otherwise
     */
    boolean execute();

    /**
     * Gets the name of the program.
     *
     * @return The program name
     */
    String getName();
}
