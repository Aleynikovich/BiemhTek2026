package biemhTekniker.IOPolling;

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

public class RobotStatePolling extends RoboticsAPICyclicBackgroundTask
{
    private static final Logger log = Logger.getLogger(RobotStatePolling.class);
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
        initializeCyclic(0, 10, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
        // Initialize HMI Buttons
        initializeHmiButtons();

    }

    @Override
    public void runCyclic()
    {
        robotStateIOGroup.setHasActiveMotion(iiwa.hasActiveMotionCommand());
        robotStateIOGroup.setIsInHome(iiwa.isInHome());
        robotStateIOGroup.setIsMastered(iiwa.isMastered());
        robotStateIOGroup.setIsReadyToMove(iiwa.isReadyToMove());
        robotStateIOGroup.setIsGMSReferenced(iiwa.getSafetyState().areAllAxesGMSReferenced());
        robotStateIOGroup.setIsReferenced(iiwa.getSafetyState().areAllAxesPositionReferenced());
    }

    @Override
    public void dispose()
    {
        robotStateIOGroup.setHasActiveMotion(true);
        robotStateIOGroup.setIsInHome(false);
        robotStateIOGroup.setIsMastered(false);
        robotStateIOGroup.setIsReadyToMove(false);
        robotStateIOGroup.setIsGMSReferenced(false);
        robotStateIOGroup.setIsReferenced(false);
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
