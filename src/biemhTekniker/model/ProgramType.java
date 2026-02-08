package biemhTekniker.model;

/**
 * Enum representing the type of program.
 * VISION programs interact with the camera system and run asynchronously.
 * ROBOT programs perform robot motions and run synchronously on the main thread.
 */
public enum ProgramType
{
    /**
     * Robot motion programs that execute on the main/application thread.
     * These programs can perform robot movements using iiwa APIs.
     */
    ROBOT,

    /**
     * Vision system programs that execute asynchronously.
     * These programs communicate with the vision system but do not perform robot motions.
     */
    VISION
}
