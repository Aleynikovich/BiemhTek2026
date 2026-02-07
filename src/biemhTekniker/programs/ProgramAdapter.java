package biemhTekniker.programs;

/**
 * Interface representing an executable program in the robot system.
 * Programs implement this interface to provide a uniform execute() method.
 */
public interface ProgramAdapter
{
    /**
     * Executes the program.
     *
     * @return true if program completed successfully, false otherwise
     */
    boolean execute();
}
