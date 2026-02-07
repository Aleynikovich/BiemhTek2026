package IOPolling;

import com.kuka.generated.ioAccess.RobotStateIOGroup;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.controllerModel.sunrise.ISafetyState;
import com.kuka.roboticsAPI.deviceModel.LBR;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class RobotStatePolling extends RoboticsAPICyclicBackgroundTask
{
    @Inject RobotStateIOGroup robotStateIOGroup;
    boolean motionReady, activeMotion;
    @Inject private Controller sunrise;
    @Inject private LBR        iiwa;

    @Override public void initialize()
    {
        initializeCyclic(0, 2000, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);

    }

    @Override public void runCyclic()
    {
        ISafetyState safetyState = iiwa.getSafetyState();
        robotStateIOGroup.setHasActiveMotion(iiwa.hasActiveMotionCommand());
        robotStateIOGroup.setIsInHome(iiwa.isInHome());
        robotStateIOGroup.setIsMastered(iiwa.isMastered());
        robotStateIOGroup.setIsReadyToMove(iiwa.isReadyToMove());
        robotStateIOGroup.setIsGMSReferenced(iiwa.getSafetyState().areAllAxesGMSReferenced());
        robotStateIOGroup.setIsReferenced(iiwa.getSafetyState().areAllAxesPositionReferenced());
    }
}