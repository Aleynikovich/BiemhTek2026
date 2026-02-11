package biemhTekniker.hmi;

import biemhTekniker.logger.Logger;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.generated.ioAccess.RobotStateIOGroup;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.uiModel.userKeys.IUserKeyBar;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class HmiButtonsBackgroundHandler extends RoboticsAPICyclicBackgroundTask
{
    private static final Logger log = Logger.getLogger(HmiButtonsBackgroundHandler.class);
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
        initializeCyclic(0, 5000, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
        // Initialize HMI Buttons
        initializeHmiButtons();
    }

    @Override
    protected void runCyclic()
    {

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

            HmiButtonHandler buttonHandler = new HmiButtonHandler(iiwa, gripper, gripperIO);

            buttonHandler.registerUserKeys(hmiKeyBar);
            log.info("HMI programmable buttons initialized successfully");
        } catch (Exception e)
        {
            log.error("Failed to initialize HMI buttons: " + e.getMessage(), e);
        }
    }


}
