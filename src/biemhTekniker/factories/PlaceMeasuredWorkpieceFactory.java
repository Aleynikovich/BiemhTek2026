package biemhTekniker.factories;

import biemhTekniker.programs.PlaceMeasuredWorkpieceProgram;
import biemhTekniker.programs.ProgramAdapter;
import biemhTekniker.programs.ProgramContext;
import biemhTekniker.programs.ProgramFactory;

/**
 * Factory for creating PlaceMeasuredWorkpieceProgram instances.
 */
public class PlaceMeasuredWorkpieceFactory implements ProgramFactory
{
    /**
     * Default public no-arg constructor required for reflective instantiation.
     */
    public PlaceMeasuredWorkpieceFactory()
    {
        // Default constructor
    }

    /**
     * Creates a PlaceMeasuredWorkpieceProgram wrapped in a ProgramAdapter.
     *
     * @param ctx Context containing all dependencies
     * @return ProgramAdapter wrapping PlaceMeasuredWorkpieceProgram
     */
    public ProgramAdapter create(final ProgramContext ctx)
    {
        return new ProgramAdapter()
        {
            public boolean execute()
            {
                PlaceMeasuredWorkpieceProgram program = new PlaceMeasuredWorkpieceProgram(ctx.getApplication(), ctx.getIiwa());
                return program.execute();
            }
        };
    }
}
