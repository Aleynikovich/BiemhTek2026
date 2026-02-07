package biemhTekniker.factories;

import biemhTekniker.programs.GetNewWorkpiecePositionProgram;
import biemhTekniker.programs.ProgramAdapter;
import biemhTekniker.programs.ProgramContext;
import biemhTekniker.programs.ProgramFactory;

/**
 * Factory for creating GetNewWorkpiecePositionProgram instances.
 */
public class GetNewWorkpieceFactory implements ProgramFactory
{
    /**
     * Default public no-arg constructor required for reflective instantiation.
     */
    public GetNewWorkpieceFactory()
    {
        // Default constructor
    }

    /**
     * Creates a GetNewWorkpiecePositionProgram wrapped in a ProgramAdapter.
     *
     * @param ctx Context containing all dependencies
     * @return ProgramAdapter wrapping GetNewWorkpiecePositionProgram
     */
    public ProgramAdapter create(final ProgramContext ctx)
    {
        return new ProgramAdapter()
        {
            public boolean execute()
            {
                GetNewWorkpiecePositionProgram program = new GetNewWorkpiecePositionProgram(ctx.visionProtocol(), ctx.workpieceData());
                return program.execute();
            }
        };
    }
}
