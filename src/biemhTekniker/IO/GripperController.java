package biemhTekniker.IO;

import com.kuka.generated.ioAccess.Gripper1IOGroup;
import com.kuka.generated.ioAccess.Gripper2IOGroup;

/**
 * Controller for Gripper 1 and Gripper 2.
 * Provides open/close operations and status queries for both grippers.
 * IO dependencies are passed via constructor for better testability.
 */
public class GripperController
{

    private final Gripper1IOGroup gripper1IO;
    private final Gripper2IOGroup gripper2IO;

    /**
     * Creates a new gripper controller with the specified IO groups.
     *
     * @param gripper1IO IO group for gripper 1
     * @param gripper2IO IO group for gripper 2
     */
    public GripperController(Gripper1IOGroup gripper1IO, Gripper2IOGroup gripper2IO)
    {
        this.gripper1IO = gripper1IO;
        this.gripper2IO = gripper2IO;
    }

    public void openGripper1()
    {
        gripper1IO.setClose(false);
        gripper1IO.setOpen(true);
    }

    public void closeGripper1()
    {
        gripper1IO.setOpen(false);
        gripper1IO.setClose(true);
    }

    public void openGripper2()
    {
        gripper2IO.setClose(false);
        gripper2IO.setOpen(true);
    }

    public void closeGripper2()
    {
        gripper2IO.setOpen(false);
        gripper2IO.setClose(true);
    }

    public boolean waitForGripper1Open(long timeoutMs)
    {
        long startTime = System.currentTimeMillis();
        while (!gripper1IO.getIsOpen())
        {
            try
            {
                Thread.sleep(50);
            } catch (InterruptedException e)
            {
                return false;
            }
            if (System.currentTimeMillis() - startTime > timeoutMs)
            {
                return false;
            }
        }
        return true;
    }

    public boolean waitForGripper1Close(long timeoutMs)
    {
        long startTime = System.currentTimeMillis();
        while (!gripper1IO.getIsClosed())
        {
            try
            {
                Thread.sleep(50);
            } catch (InterruptedException e)
            {
                return false;
            }
            if (System.currentTimeMillis() - startTime > timeoutMs)
            {
                return false;
            }
        }
        return true;
    }

    public boolean waitForGripper2Open(long timeoutMs)
    {
        long startTime = System.currentTimeMillis();
        while (!gripper2IO.getIsOpen())
        {
            try
            {
                Thread.sleep(50);
            } catch (InterruptedException e)
            {
                return false;
            }
            if (System.currentTimeMillis() - startTime > timeoutMs)
            {
                return false;
            }
        }
        return true;
    }

    public boolean waitForGripper2Close(long timeoutMs)
    {
        long startTime = System.currentTimeMillis();
        while (!gripper2IO.getIsClosed())
        {
            try
            {
                Thread.sleep(50);
            } catch (InterruptedException e)
            {
                return false;
            }
            if (System.currentTimeMillis() - startTime > timeoutMs)
            {
                return false;
            }
        }
        return true;
    }

    public boolean isGripper1Open()
    {
        return gripper1IO.getIsOpen();
    }

    public boolean isGripper1Closed()
    {
        return gripper1IO.getIsClosed();
    }

    public boolean isGripper2Open()
    {
        return gripper2IO.getIsOpen();
    }

    public boolean isGripper2Closed()
    {
        return gripper2IO.getIsClosed();
    }
}
