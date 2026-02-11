package biemhTekniker.lib.exceptions;

/**
 * Exception thrown when a robot program is cancelled by the user.
 * This allows calling code to distinguish cancellation from other program failures.
 */
public class ProgramCancelledException extends Exception
{
    public ProgramCancelledException(String message)
    {
        super(message);
    }

    public ProgramCancelledException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
