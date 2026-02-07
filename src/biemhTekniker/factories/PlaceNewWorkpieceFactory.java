package biemhTekniker.factories;

import biemhTekniker.programs.PlaceNewWorkpieceProgram;
import biemhTekniker.programs.ProgramAdapter;
import biemhTekniker.programs.ProgramContext;
import biemhTekniker.programs.ProgramFactory;

/**
 * Factory for creating PlaceNewWorkpieceProgram instances.
 */
public class PlaceNewWorkpieceFactory implements ProgramFactory
{
    /**
     * Default public no-arg constructor required for reflective instantiation.
     */
    public PlaceNewWorkpieceFactory()
    {
        // Default constructor
    }

    /**
     * Creates a PlaceNewWorkpieceProgram wrapped in a ProgramAdapter.
     *
     * @param ctx Context containing all dependencies
     * @return ProgramAdapter wrapping PlaceNewWorkpieceProgram
     */
    public ProgramAdapter create(final ProgramContext ctx)
    {
        return new ProgramAdapter()
        {
            public boolean execute()
            {
                PlaceNewWorkpieceProgram program = new PlaceNewWorkpieceProgram(ctx.application(), ctx.iiwa(), ctx.gripper(), ctx.gripperIO());
                return program.execute();
            }
        };
    }
}
