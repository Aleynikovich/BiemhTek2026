package biemhTekniker.exceptions;

/**
 * Exception thrown when the robot fails to move to home position.
 */
public class HomePositionException extends Exception
{
    public HomePositionException(String message)
    {
        super(message);
    }

    public HomePositionException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
