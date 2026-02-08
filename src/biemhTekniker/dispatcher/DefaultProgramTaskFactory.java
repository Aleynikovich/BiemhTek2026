package biemhTekniker.dispatcher;

import biemhTekniker.data.WorkpieceData;
import biemhTekniker.logger.Logger;
import biemhTekniker.model.ProgramDescriptor;
import biemhTekniker.programs.*;
import biemhTekniker.tasks.*;
import biemhTekniker.vision.SmartPickingProtocol;

/**
 * Default implementation of ProgramTaskFactory.
 * Creates task instances for the standard robot programs.
 * 
 * This factory converts existing program implementations into tasks:
 * - Program 1 (GetNewWorkpiecePosition) -> VisionTask
 * - Program 2-7 -> RobotTask wrappers around existing programs
 */
public class DefaultProgramTaskFactory implements ProgramTaskFactory
{
    private static final Logger log = Logger.getLogger(DefaultProgramTaskFactory.class);

    private final SmartPickingProtocol protocol;
    private final WorkpieceData        workpieceData;
    private final String               configServiceUrl;

    public DefaultProgramTaskFactory(SmartPickingProtocol protocol,
                                     WorkpieceData workpieceData,
                                     String configServiceUrl)
    {
        this.protocol          = protocol;
        this.workpieceData     = workpieceData;
        this.configServiceUrl  = configServiceUrl;
    }

    @Override public ProgramTask createTask(ProgramDescriptor descriptor)
    {
        int programNumber = descriptor.getProgramNumber();

        switch (programNumber)
        {
            case 1:
                // GetNewWorkpiecePosition - VisionTask
                return new GetNewWorkpiecePositionTask(protocol, workpieceData, configServiceUrl);

            case 2:
                // Calibration - VISION program wrapped as RobotTask (legacy)
                return createLegacyCalibrationTask(descriptor);

            case 3:
                // TestCalibration - VISION program wrapped as RobotTask (legacy)
                return createLegacyTestCalibrationTask(descriptor);

            case 4:
                // PickNewWorkpiece - ROBOT program
                return createLegacyPickNewWorkpieceTask(descriptor);

            case 5:
                // PlaceNewWorkpiece - ROBOT program
                return createLegacyPlaceNewWorkpieceTask(descriptor);

            case 6:
                // PickMeasuredWorkpiece - ROBOT program
                return createLegacyPickMeasuredWorkpieceTask(descriptor);

            case 7:
                // PlaceMeasuredWorkpiece - ROBOT program
                return createLegacyPlaceMeasuredWorkpieceTask(descriptor);

            default:
                log.warn("No task handler for program number: " + programNumber);
                return null;
        }
    }

    @Override public boolean supports(int programNumber)
    {
        return programNumber >= 1 && programNumber <= 7;
    }

    // Legacy task wrappers - these wrap existing program implementations as tasks

    private ProgramTask createLegacyCalibrationTask(final ProgramDescriptor descriptor)
    {
        return new RobotTask(descriptor.getProgramNumber(), descriptor.getProgramName())
        {
            @Override public TaskResult execute() throws Exception
            {
                CalibrationProgram program = new CalibrationProgram();
                program.setProtocol(protocol);
                program.run();
                return TaskResult.success("Calibration completed");
            }
        };
    }

    private ProgramTask createLegacyTestCalibrationTask(final ProgramDescriptor descriptor)
    {
        return new RobotTask(descriptor.getProgramNumber(), descriptor.getProgramName())
        {
            @Override public TaskResult execute() throws Exception
            {
                TestCalibrationProgram program = new TestCalibrationProgram();
                program.setProtocol(protocol);
                program.run();
                return TaskResult.success("Test calibration completed");
            }
        };
    }

    private ProgramTask createLegacyPickNewWorkpieceTask(final ProgramDescriptor descriptor)
    {
        return new RobotTask(descriptor.getProgramNumber(), descriptor.getProgramName())
        {
            @Override public TaskResult execute() throws Exception
            {
                PickNewWorkpieceProgram program = new PickNewWorkpieceProgram();
                program.setWorkpieceData(workpieceData);
                program.run();
                return TaskResult.success("Pick new workpiece completed");
            }
        };
    }

    private ProgramTask createLegacyPlaceNewWorkpieceTask(final ProgramDescriptor descriptor)
    {
        return new RobotTask(descriptor.getProgramNumber(), descriptor.getProgramName())
        {
            @Override public TaskResult execute() throws Exception
            {
                PlaceNewWorkpieceProgram program = new PlaceNewWorkpieceProgram();
                program.run();
                return TaskResult.success("Place new workpiece completed");
            }
        };
    }

    private ProgramTask createLegacyPickMeasuredWorkpieceTask(final ProgramDescriptor descriptor)
    {
        return new RobotTask(descriptor.getProgramNumber(), descriptor.getProgramName())
        {
            @Override public TaskResult execute() throws Exception
            {
                PickMeasuredWorkpieceProgram program = new PickMeasuredWorkpieceProgram();
                program.run();
                return TaskResult.success("Pick measured workpiece completed");
            }
        };
    }

    private ProgramTask createLegacyPlaceMeasuredWorkpieceTask(final ProgramDescriptor descriptor)
    {
        return new RobotTask(descriptor.getProgramNumber(), descriptor.getProgramName())
        {
            @Override public TaskResult execute() throws Exception
            {
                PlaceMeasuredWorkpieceProgram program = new PlaceMeasuredWorkpieceProgram();
                program.run();
                return TaskResult.success("Place measured workpiece completed");
            }
        };
    }
}
