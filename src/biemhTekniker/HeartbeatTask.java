package biemhTekniker;

import com.kuka.generated.ioAccess.RobotStatusIOGroup;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPIBackgroundTask;

import javax.inject.Inject;

/**
 * Background task that toggles a Profinet heartbeat signal every 100ms.
 * NOTE: Currently uses ZRes1 signal - this reserve bit should be renamed in
 * IO configuration to "Heartbeat" or "PLCLifeBit" to reflect its purpose.
 */
public class HeartbeatTask extends RoboticsAPIBackgroundTask
{

    @Inject
    private RobotStatusIOGroup robotStatusIO;

    private int intervalMs;
    private boolean currentState;

    @Override
    public void initialize()
    {
        this.intervalMs = 100;
        this.currentState = false;
        getLogger().info("HeartbeatTask initialized (interval: " + intervalMs + "ms)");
    }

    @Override
    public void run()
    {
        getLogger().info("HeartbeatTask started");

        while (!Thread.currentThread().isInterrupted())
        {
            try
            {
                currentState = !currentState;
                robotStatusIO.setZRes1(currentState);

                Thread.sleep(intervalMs);

            } catch (InterruptedException e)
            {
                getLogger().info("HeartbeatTask interrupted, stopping");
                break;
            } catch (Exception e)
            {
                getLogger().error("HeartbeatTask error: " + e.getMessage());
            }
        }

        try
        {
            robotStatusIO.setZRes1(false);
        } catch (Exception e)
        {
            getLogger().error("Failed to reset heartbeat signal: " + e.getMessage());
        }

        getLogger().info("HeartbeatTask stopped");
    }

    @Override
    public void dispose()
    {
        super.dispose();
    }
}
