package biemhTekniker.factories;

import biemhTekniker.programs.ProgramAdapter;
import biemhTekniker.programs.ProgramContext;
import biemhTekniker.programs.ProgramFactory;
import biemhTekniker.programs.PickNewWorkpieceProgram;

/**
 * Factory for creating PickNewWorkpieceProgram instances.
 * Demonstrates the factory pattern for program instantiation.
 */
public class PickNewWorkpieceFactory implements ProgramFactory
{
    /**
     * Default public no-arg constructor required for reflective instantiation.
     */
    public PickNewWorkpieceFactory()
    {
        // Default constructor
    }

    /**
     * Creates a PickNewWorkpieceProgram wrapped in a ProgramAdapter.
     *
     * @param ctx Context containing all dependencies
     * @return ProgramAdapter wrapping PickNewWorkpieceProgram
     */
    public ProgramAdapter create(final ProgramContext ctx)
    {
        return new ProgramAdapter()
        {
            public boolean execute()
            {
                PickNewWorkpieceProgram program = new PickNewWorkpieceProgram(ctx.getApplication(), ctx.getIiwa(), ctx.getWorkpieceData(), ctx.getGripper(), ctx.getGripperIO());
                return program.execute();
            }
        };
    }
}
