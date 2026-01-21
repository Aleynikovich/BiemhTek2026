package biemhTekniker;

import biemhTekniker.logger.LogCollector;
import biemhTekniker.logger.LogManager;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import javax.inject.Inject;


@SuppressWarnings("unused")
public class Main extends RoboticsAPIApplication
{
    @Inject
    private LBR iiwa;

    @Override
    public void initialize()
    {
        LogCollector _logCollector = new LogCollector();
        LogManager.register(_logCollector);

    }

    @Override
    public void run()
    {
        while (true)
        {

        }
    }

    @Override
    public void dispose()
    {

        super.dispose();
    }

}