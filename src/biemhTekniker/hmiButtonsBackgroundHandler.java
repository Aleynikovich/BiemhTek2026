package biemhTekniker;

import biemhTekniker.logger.Logger;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.generated.ioAccess.RobotStateIOGroup;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPIBackgroundTask;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.uiModel.userKeys.IUserKeyBar;

import javax.inject.Inject;

public class hmiButtonsBackgroundHandler extends RoboticsAPIBackgroundTask
{
    private static final Logger log = Logger.getLogger(hmiButtonsBackgroundHandler.class);
    @Inject
    RobotStateIOGroup robotStateIOGroup;
    @Inject
    LBR iiwa;
    @Inject
    Tool gripper;
    @Inject
    MediaFlangeIOGroup gripperIO;
    private IUserKeyBar hmiKeyBar;

    @Override
    public void initialize()
    {
        // Initialize HMI Buttons
        initializeHmiButtons();

    }

    @Override
    public void run() throws Exception
    {
        log.debug("hmiButtonBackgroundHandler running");
    }

    /**
     * Initializes the HMI programmable buttons on the SmartPad.
     */
    private void initializeHmiButtons()
    {
        try
        {
            log.info("Initializing HMI programmable buttons...");
            hmiKeyBar = getApplicationUI().createUserKeyBar("BiemhTek_HMI");

            biemhTekniker.hmi.HmiButtonHandler buttonHandler = new biemhTekniker.hmi.HmiButtonHandler(iiwa, gripper, gripperIO);

            buttonHandler.registerUserKeys(hmiKeyBar);
            log.info("HMI programmable buttons initialized successfully");
        } catch (Exception e)
        {
            log.error("Failed to initialize HMI buttons: " + e.getMessage(), e);
        }
    }
}
