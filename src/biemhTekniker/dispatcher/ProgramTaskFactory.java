package biemhTekniker.dispatcher;

import biemhTekniker.model.ProgramDescriptor;
import biemhTekniker.tasks.ProgramTask;

/**
 * Factory interface for creating ProgramTask instances from descriptors.
 * Implementations can provide custom task creation logic.
 * Java 7 compatible.
 */
public interface ProgramTaskFactory {
    
    /**
     * Create a task for the given program descriptor.
     * 
     * @param descriptor The program descriptor
     * @return A ProgramTask instance, or null if not supported
     */
    ProgramTask createTask(ProgramDescriptor descriptor);
    
    /**
     * Check if this factory can handle the given program.
     * 
     * @param descriptor The program descriptor
     * @return true if this factory can create a task for this program
     */
    boolean canHandle(ProgramDescriptor descriptor);
}
