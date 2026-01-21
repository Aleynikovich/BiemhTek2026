package biemhTekniker;

import biemhTekniker.logger.LogCollector;
import biemhTekniker.logger.LogManager;
import biemhTekniker.logger.LogPublisher;
import biemhTekniker.logger.Logger;
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
    private static final Logger log = Logger.getLogger(Main.class);
    @Override
    public void initialize()
    {
        initializeLogging();


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

    public void initializeLogging()
    {
        try
        {
            _logCollector = new LogCollector();
            LogManager.register(_logCollector);

            _logPublisher = new LogPublisher(_logCollector);
            _logPublisher.start();

            log.info("Logging initialized");

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

}