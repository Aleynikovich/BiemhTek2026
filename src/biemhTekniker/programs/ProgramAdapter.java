package biemhTekniker.programs;

/**
 * Adapter to wrap executable actions as RobotProgram instances.
 * Allows easy wrapping of existing program classes without modification.
 * Java 1.7 compatible - uses inner interface instead of functional interface.
 */
public class ProgramAdapter implements RobotProgram
{
    /**
     * Interface for executable actions.
     * Implemented as anonymous inner class in Java 1.7.
     */
    public static interface Action
    {
        /**
         * Runs the action.
         *
         * @return true if action succeeded, false otherwise
         */
        boolean run();
    }

    private final String name;
    private final Action action;

    /**
     * Creates a new program adapter.
     *
     * @param name   The program name
     * @param action The action to execute
     */
    public ProgramAdapter(String name, Action action)
    {
        this.name   = name;
        this.action = action;
    }

    @Override
    public boolean execute()
    {
        return action.run();
    }

    @Override
    public String getName()
    {
        return name;
    }
}
