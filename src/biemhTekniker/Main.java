package biemhTekniker;

import biemhTekniker.logger.LogCollector;
import biemhTekniker.logger.LogManager;
import biemhTekniker.logger.LogPublisher;
import biemhTekniker.logger.Logger;
import biemhTekniker.vision.VisionGateway;
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
            // Example: Ask for vision every 5 seconds
            VisionGateway.send("15;BIEMH26_105055");

            // Wait a tiny bit for the background task to do its work
            ThreadUtil.milliSleep(5000);

            // Check if the response arrived
            String response = VisionGateway.getResponse();
            if (response != null) {
                log.info("Robot received: " + response);
                // Logic to move the robot based on 'response' goes here
            }

            ThreadUtil.milliSleep(4900);
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