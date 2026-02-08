package biemhTekniker.exceptions;

/**
 * Exception thrown when a vision task fails during execution.
 */
public class VisionTaskException extends Exception
{
    public VisionTaskException(String message)
    {
        super(message);
    }

    public VisionTaskException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
