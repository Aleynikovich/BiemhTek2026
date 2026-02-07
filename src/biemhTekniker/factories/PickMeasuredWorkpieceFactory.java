package biemhTekniker.factories;

import biemhTekniker.programs.PickMeasuredWorkpieceProgram;
import biemhTekniker.programs.ProgramAdapter;
import biemhTekniker.programs.ProgramContext;
import biemhTekniker.programs.ProgramFactory;

/**
 * Factory for creating PickMeasuredWorkpieceProgram instances.
 */
public class PickMeasuredWorkpieceFactory implements ProgramFactory
{
    /**
     * Default public no-arg constructor required for reflective instantiation.
     */
    public PickMeasuredWorkpieceFactory()
    {
        // Default constructor
    }

    /**
     * Creates a PickMeasuredWorkpieceProgram wrapped in a ProgramAdapter.
     *
     * @param ctx Context containing all dependencies
     * @return ProgramAdapter wrapping PickMeasuredWorkpieceProgram
     */
    public ProgramAdapter create(final ProgramContext ctx)
    {
        return new ProgramAdapter()
        {
            public boolean execute()
            {
                PickMeasuredWorkpieceProgram program = new PickMeasuredWorkpieceProgram(ctx.getApplication(), ctx.getIiwa());
                return program.execute();
            }
        };
    }
}
