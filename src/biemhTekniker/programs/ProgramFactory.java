package biemhTekniker.programs;

/**
 * Factory interface for creating robot programs.
 * Allows registration of program creators that use ProgramContext.
 * Java 1.7 compatible.
 */
public interface ProgramFactory
{
    /**
     * Creates a new robot program instance using the provided context.
     *
     * @param ctx The program context containing shared dependencies
     * @return A new robot program instance
     */
    RobotProgram create(ProgramContext ctx);
}
