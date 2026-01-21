package biemhTekniker.vision;


import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Background task that manages the console command server.
 * Receives requests from external clients and handles them locally
 **/
public class SmartPickingClient extends RoboticsAPICyclicBackgroundTask
{
    @Override
    public void initialize()
    {
        initializeCyclic(0, 1000, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);

    }

    @Override
    public void runCyclic()
    {

    }

    @Override
    public void dispose()
    {

        super.dispose();
    }
}

