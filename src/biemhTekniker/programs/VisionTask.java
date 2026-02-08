package biemhTekniker.programs;

/**
 * Interface for vision tasks that execute on the vision thread.
 * Vision tasks send commands to the camera and are non-blocking from the robot's perspective.
 * Results are placed into the shared WorkpieceQueue.
 */
public interface VisionTask
{
    /**
     * Executes the vision task logic.
     *
     * @param context Context providing access to vision protocol and shared data
     * @throws Exception if the task execution fails
     */
    void execute(VisionContext context) throws Exception;
}
