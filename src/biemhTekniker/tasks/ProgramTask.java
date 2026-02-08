package biemhTekniker.tasks;

import biemhTekniker.model.ProgramDescriptor;

/**
 * Interface for program tasks that can be executed by the dispatcher.
 * Java 7 compatible.
 */
public interface ProgramTask {
    
    /**
     * Execute the task.
     * 
     * @return TaskResult indicating success or failure
     */
    TaskResult execute();
    
    /**
     * Get the program descriptor associated with this task.
     * 
     * @return The program descriptor
     */
    ProgramDescriptor getDescriptor();
    
    /**
     * Check if this task requires vision system connection.
     * 
     * @return true if vision connection is required
     */
    boolean requiresVision();
}
