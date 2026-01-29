package biemhTekniker;

import biemhTekniker.calibration.CalibrationRoutine;
import biemhTekniker.logger.LogCollector;
import biemhTekniker.logger.LogManager;
import biemhTekniker.logger.LogPublisher;
import biemhTekniker.logger.Logger;
import biemhTekniker.vision.SmartPickingProtocol;
import biemhTekniker.vision.VisionSocketClient;
import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import javax.inject.Inject;

@SuppressWarnings("unused")
public class Main extends RoboticsAPIApplication
{
    @Inject
    private LBR iiwa;

    private String VisionServerIP = "172.31.1.69";
    private int VisionServerPort = 59002;

    private boolean calibrationSuccess = false;

    private LogPublisher _logPublisher;
    private static final Logger log = Logger.getLogger(Main.class);

    private int programNumber = 0;
    private VisionDataBridge _visionDataBridge;

    @Override
    public void initialize()
    {
        initializeLogging();
        getApplicationControl().setApplicationOverride(0.5);
        getApplicationControl().clipManualOverride(0.00);
    }

    @Override
    public void run()
    {
        boolean requestProgramFromPLC = true, programRequested = false;

        if(!programRequested){
            log.info("Main Application Running, requesting program from PLC");
            requestProgramFromPLC = true;
        }

        while (true)
        {
            switch (programNumber)
            {
                case 0:
                    //Program 0
                    break;
                case 1:
                    //Program 1
                case 2:
                    calibrationSuccess = executeCalibration(VisionServerIP, VisionServerPort);
                    break;
                //So on...
                default:
                    break;

            }
            ThreadUtil.milliSleep(200);
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
            LogCollector _logCollector = new LogCollector();
            LogManager.register(_logCollector);

            _logPublisher = new LogPublisher(_logCollector);
            _logPublisher.start();

            log.info("Logging initialized");

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes the calibration routine for the vision system.
     * This method can be called separately to perform calibration.
     *
     * @param visionServerIP   IP address of the vision system server
     * @param visionServerPort Port number of the vision system server
     * @return true if calibration completed successfully, false otherwise
     */
    public boolean executeCalibration(String visionServerIP, int visionServerPort)
    {
        log.info("Starting calibration sequence...");

        // Create vision client and protocol
        VisionSocketClient visionClient = new VisionSocketClient(visionServerIP, visionServerPort);
        if (!visionClient.connect())
        {
            log.error("Failed to connect to vision server");
            return false;
        }


        SmartPickingProtocol protocol = new SmartPickingProtocol(visionClient);

        // Create calibration routine
        CalibrationRoutine calibration = new CalibrationRoutine(
                this,
                iiwa,
                protocol,
                iiwa.getFlange()
        );

        // Execute calibration
        // Note: Pass null for test frame if not defined in RoboticsAPI.data.xml
        // To use a test frame, define it in the XML (e.g., "/CalibrationPoints/Test")
        boolean success = calibration.executeCalibration(
                "/CalibrationPoints",
                "/CalibrationPoints/P16"
        );

        // Clean up
        visionClient.close();

        if (success)
        {
            log.info("Calibration completed successfully");
        } else
        {
            log.error("Calibration failed");
        }

        return success;
    }
}