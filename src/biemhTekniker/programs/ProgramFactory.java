package biemhTekniker.programs;

/**
 * Interface for program factories.
 * Each factory is responsible for creating a specific program instance using the provided context.
 */
public interface ProgramFactory
{
    /**
     * Creates a program instance using the provided context.
     *
     * @param context Context containing all dependencies needed by the program
     * @return ProgramAdapter instance ready to execute
     */
    ProgramAdapter create(ProgramContext context);
}
