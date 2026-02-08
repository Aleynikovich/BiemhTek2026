package biemhTekniker.tasks;

import biemhTekniker.logger.Logger;
import biemhTekniker.model.ProgramDescriptor;

/**
 * Abstract base class for robot tasks.
 * Robot tasks perform physical robot movements and operations.
 * Java 7 compatible.
 */
public abstract class RobotTask implements ProgramTask {
    
    protected static final Logger log = Logger.getLogger(RobotTask.class);
    
    protected final ProgramDescriptor descriptor;
    
    public RobotTask(ProgramDescriptor descriptor) {
        this.descriptor = descriptor;
    }
    
    @Override
    public ProgramDescriptor getDescriptor() {
        return descriptor;
    }
    
    @Override
    public boolean requiresVision() {
        return false;
    }
    
    /**
     * Execute the robot task.
     * May call robot motion APIs.
     */
    @Override
    public abstract TaskResult execute();
}
