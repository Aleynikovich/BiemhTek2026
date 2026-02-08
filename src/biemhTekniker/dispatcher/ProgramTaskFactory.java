package biemhTekniker.dispatcher;

import biemhTekniker.model.ProgramDescriptor;
import biemhTekniker.tasks.ProgramTask;

/**
 * Factory interface for creating ProgramTask instances from ProgramDescriptor.
 * Implementations should register handlers for specific program numbers.
 */
public interface ProgramTaskFactory
{
    /**
     * Create a task for the given program descriptor.
     *
     * @param descriptor the program descriptor from config service
     * @return ProgramTask instance or null if not supported
     */
    ProgramTask createTask(ProgramDescriptor descriptor);

    /**
     * Check if this factory supports the given program number.
     *
     * @param programNumber the program number to check
     * @return true if supported, false otherwise
     */
    boolean supports(int programNumber);
}
