package biemhTekniker.factories;

import biemhTekniker.programs.CalibrationProgram;
import biemhTekniker.programs.ProgramAdapter;
import biemhTekniker.programs.ProgramContext;
import biemhTekniker.programs.ProgramFactory;

/**
 * Factory for creating CalibrationProgram instances.
 */
public class CalibrationFactory implements ProgramFactory
{
    /**
     * Default public no-arg constructor required for reflective instantiation.
     */
    public CalibrationFactory()
    {
        // Default constructor
    }

    /**
     * Creates a CalibrationProgram wrapped in a ProgramAdapter.
     *
     * @param ctx Context containing all dependencies
     * @return ProgramAdapter wrapping CalibrationProgram
     */
    public ProgramAdapter create(final ProgramContext ctx)
    {
        return new ProgramAdapter()
        {
            public boolean execute()
            {
                CalibrationProgram program = new CalibrationProgram(ctx.getApplication(), ctx.getIiwa(), ctx.getVisionProtocol(), ctx.getGripper());
                return program.execute();
            }
        };
    }
}
