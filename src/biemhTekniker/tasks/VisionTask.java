package biemhTekniker.tasks;

import biemhTekniker.logger.Logger;
import biemhTekniker.model.ProgramDescriptor;

/**
 * Abstract base class for vision tasks.
 * Vision tasks must NOT perform any robot motion - they only interact with
 * the vision system and the config service.
 * Java 7 compatible.
 */
public abstract class VisionTask implements ProgramTask {
    
    protected static final Logger log = Logger.getLogger(VisionTask.class);
    
    protected final ProgramDescriptor descriptor;
    
    public VisionTask(ProgramDescriptor descriptor) {
        this.descriptor = descriptor;
    }
    
    @Override
    public ProgramDescriptor getDescriptor() {
        return descriptor;
    }
    
    @Override
    public boolean requiresVision() {
        return true;
    }
    
    /**
     * Execute the vision task.
     * Must not call any robot motion APIs.
     */
    @Override
    public abstract TaskResult execute();
}
