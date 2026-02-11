package biemhTekniker.iopolling;

import biemhTekniker.lib.logger.Logger;
import com.kuka.generated.ioAccess.RobotStateIOGroup;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;
import com.kuka.roboticsAPI.deviceModel.LBR;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class RobotStatePolling extends RoboticsAPICyclicBackgroundTask
{
    private static final Logger log = Logger.getLogger(RobotStatePolling.class);
    @Inject
    RobotStateIOGroup robotStateIOGroup;
    @Inject
    LBR iiwa;

    @Override
    public void initialize()
    {
        initializeCyclic(0, 10, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
    }

    @Override
    public void runCyclic()
    {
        try
        {
            robotStateIOGroup.setHasActiveMotion(iiwa.hasActiveMotionCommand());
            robotStateIOGroup.setIsInHome(iiwa.isInHome());
            robotStateIOGroup.setIsMastered(iiwa.isMastered());
            robotStateIOGroup.setIsReadyToMove(iiwa.isReadyToMove());
            robotStateIOGroup.setIsGMSReferenced(iiwa.getSafetyState().areAllAxesGMSReferenced());
            robotStateIOGroup.setIsReferenced(iiwa.getSafetyState().areAllAxesPositionReferenced());
        } catch (Exception e)
        {
            // Log error but continue running - PLC may be in STOP mode
            log.error("Failed to update robot state: " + e.getMessage());
        }
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

}
