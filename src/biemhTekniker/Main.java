package biemhTekniker;

import biemhTekniker.logger.LogCollector;
import biemhTekniker.logger.LogManager;
import biemhTekniker.logger.LogPublisher;
import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import javax.inject.Inject;


@SuppressWarnings("unused")
public class Main extends RoboticsAPIApplication
{
    @Inject
    private LBR iiwa;

    private LogCollector _logCollector;
    private LogPublisher _logPublisher;

    @Override
    public void initialize()
    {
        _logCollector = new LogCollector();
        LogManager.register(_logCollector);

        _logPublisher = new LogPublisher(_logCollector);
        _logPublisher.start();

    }

    @Override
    public void run()
    {
        while (true)
        {
            ThreadUtil.milliSleep(1000);
        }
    }

    @Override
    public void dispose()
    {
        if (_logPublisher != null) _logPublisher.stop();
        super.dispose();
    }

}