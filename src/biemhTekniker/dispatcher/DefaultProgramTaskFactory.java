package biemhTekniker.dispatcher;

import biemhTekniker.data.WorkpieceData;
import biemhTekniker.logger.Logger;
import biemhTekniker.model.ProgramDescriptor;
import biemhTekniker.programs.*;
import biemhTekniker.tasks.*;
import biemhTekniker.vision.SmartPickingProtocol;

/**
 * Default factory for creating tasks from program descriptors.
 * This factory handles the standard robot programs.
 * Java 7 compatible.
 */
public class DefaultProgramTaskFactory implements ProgramTaskFactory {
    
    private static final Logger log = Logger.getLogger(DefaultProgramTaskFactory.class);
    
    private final SmartPickingProtocol protocol;
    private final WorkpieceData workpieceData;
    private final String configServiceBaseUrl;
    
    /**
     * Create a default program task factory.
     * 
     * @param protocol SmartPicking protocol for vision tasks
     * @param workpieceData Shared workpiece data
     * @param configServiceBaseUrl Base URL of config service
     */
    public DefaultProgramTaskFactory(SmartPickingProtocol protocol, 
                                     WorkpieceData workpieceData,
                                     String configServiceBaseUrl) {
        this.protocol = protocol;
        this.workpieceData = workpieceData;
        this.configServiceBaseUrl = configServiceBaseUrl;
    }
    
    @Override
    public ProgramTask createTask(ProgramDescriptor descriptor) {
        int programNumber = descriptor.getProgramNumber();
        
        // Program 1: Get New Workpiece Position (VISION)
        if (programNumber == 1) {
            return new GetNewWorkpiecePositionTask(descriptor, protocol, workpieceData, configServiceBaseUrl);
        }
        
        // Programs 2-7: Wrap existing programs as tasks
        // For now, we wrap the existing program implementations
        return createLegacyProgramWrapper(descriptor);
    }
    
    @Override
    public boolean canHandle(ProgramDescriptor descriptor) {
        int programNumber = descriptor.getProgramNumber();
        // Handle programs 1-7
        return programNumber >= 1 && programNumber <= 7;
    }
    
    /**
     * Create a wrapper task for legacy programs that haven't been converted yet.
     * This allows gradual migration.
     */
    private ProgramTask createLegacyProgramWrapper(final ProgramDescriptor descriptor) {
        final int programNumber = descriptor.getProgramNumber();
        
        // Create appropriate task type based on descriptor
        if (descriptor.getProgramType() == biemhTekniker.model.ProgramType.VISION) {
            return new VisionTask(descriptor) {
                @Override
                public TaskResult execute() {
                    try {
                        // Execute legacy program
                        switch (programNumber) {
                            case 2:
                                CalibrationProgram calibProg = new CalibrationProgram();
                                calibProg.setProtocol(protocol);
                                calibProg.run();
                                return TaskResult.success("Calibration completed");
                                
                            case 3:
                                TestCalibrationProgram testProg = new TestCalibrationProgram();
                                testProg.setProtocol(protocol);
                                testProg.run();
                                return TaskResult.success("Test calibration completed");
                                
                            default:
                                return TaskResult.failure("Legacy vision program not implemented: " + programNumber);
                        }
                    } catch (Exception e) {
                        return TaskResult.failure("Legacy program failed", e);
                    }
                }
            };
        } else {
            // ROBOT task
            return new RobotTask(descriptor) {
                @Override
                public TaskResult execute() {
                    try {
                        // Execute legacy robot program
                        switch (programNumber) {
                            case 4:
                                PickNewWorkpieceProgram pickNew = new PickNewWorkpieceProgram();
                                pickNew.setWorkpieceData(workpieceData);
                                pickNew.run();
                                return TaskResult.success("Pick new workpiece completed");
                                
                            case 5:
                                PlaceNewWorkpieceProgram placeNew = new PlaceNewWorkpieceProgram();
                                placeNew.run();
                                return TaskResult.success("Place new workpiece completed");
                                
                            case 6:
                                PickMeasuredWorkpieceProgram pickMeas = new PickMeasuredWorkpieceProgram();
                                pickMeas.run();
                                return TaskResult.success("Pick measured workpiece completed");
                                
                            case 7:
                                PlaceMeasuredWorkpieceProgram placeMeas = new PlaceMeasuredWorkpieceProgram();
                                placeMeas.run();
                                return TaskResult.success("Place measured workpiece completed");
                                
                            default:
                                return TaskResult.failure("Legacy robot program not implemented: " + programNumber);
                        }
                    } catch (Exception e) {
                        return TaskResult.failure("Legacy program failed", e);
                    }
                }
            };
        }
    }
}
