package IOPolling;


import com.kuka.generated.ioAccess.RobotSafetyIOGroup;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.controllerModel.sunrise.ISafetyState;
import com.kuka.roboticsAPI.controllerModel.sunrise.SunriseSafetyState;
import com.kuka.roboticsAPI.deviceModel.LBR;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class RobotSafetyPolling extends RoboticsAPICyclicBackgroundTask
{
    boolean motionReady, activeMotion;
    @Inject private Controller         sunrise;
    @Inject private RobotSafetyIOGroup safetyIO;
    @Inject private LBR                iiwa;
    ISafetyState safetyState = iiwa.getSafetyState();

    @Override public void initialize()
    {
        initializeCyclic(0, 500, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
    }

    @Override public void runCyclic()
    {
        safetyIO.setIsExternalEStop(safetyState.getEmergencyStopEx() == SunriseSafetyState.EmergencyStop.ACTIVE);
        safetyIO.setIsOperatorSafety(safetyState.getOperatorSafetyState() == SunriseSafetyState.OperatorSafety.OPERATOR_SAFETY_CLOSED );
    }
}