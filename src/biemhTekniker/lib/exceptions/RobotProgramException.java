package biemhTekniker.lib.exceptions;

/**
 * Exception thrown when a robot program fails during execution.
 */
public class RobotProgramException extends Exception
{
    public RobotProgramException(String message)
    {
        super(message);
    }

    public RobotProgramException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
