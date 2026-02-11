package biemhTekniker.lib.robot;

/**
 * Interface for robot programs that execute on the main robot thread.
 * Robot programs control physical robot motions and gripper operations.
 * They consume data from the shared WorkpieceQueue.
 */
public interface RobotProgram
{
    /**
     * Executes the robot program logic.
     *
     * @param context Context providing access to robot, gripper, and shared data
     * @throws Exception if the program execution fails
     */
    void execute(RobotContext context) throws Exception;
}
