package biemhTekniker.factories;

import biemhTekniker.programs.PickNewWorkpieceProgram;
import biemhTekniker.programs.ProgramAdapter;
import biemhTekniker.programs.ProgramContext;
import biemhTekniker.programs.ProgramFactory;

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
                PickNewWorkpieceProgram program = new PickNewWorkpieceProgram(ctx.application(), ctx.iiwa(), ctx.workpieceData(), ctx.gripper(), ctx.gripperIO());
                return program.execute();
            }
        };
    }
}
