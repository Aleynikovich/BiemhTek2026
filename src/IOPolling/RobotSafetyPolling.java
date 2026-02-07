package IOPolling;


import com.kuka.generated.ioAccess.RobotSafetyIOGroup;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.controllerModel.sunrise.ISafetyState;
import com.kuka.roboticsAPI.controllerModel.sunrise.SunriseApplication;
import com.kuka.roboticsAPI.controllerModel.sunrise.SunriseSafetyState;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.deviceModel.OperationMode;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class RobotSafetyPolling extends RoboticsAPICyclicBackgroundTask
{
    boolean motionReady, activeMotion;
    @Inject private Controller         sunrise;
    @Inject private RobotSafetyIOGroup safetyIO;
    @Inject private LBR                iiwa;


    @Override public void initialize()
    {
        initializeCyclic(0, 10, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
    }

    @Override public void runCyclic()
    {
        safetyIO.setIsExternalEStop(iiwa.getSafetyState().getEmergencyStopEx() == SunriseSafetyState.EmergencyStop.ACTIVE);
        safetyIO.setIsOperatorSafety(iiwa.getSafetyState().getOperatorSafetyState() == SunriseSafetyState.OperatorSafety.OPERATOR_SAFETY_CLOSED );
        safetyIO.setModeT1(iiwa.getSafetyState().getOperationMode() == OperationMode.T1);
        safetyIO.setModeT2(iiwa.getSafetyState().getOperationMode() == OperationMode.AUT);
        safetyIO.setMoveEnable(iiwa.getSafetyState().getEnablingDeviceState() == SunriseSafetyState.EnablingDeviceState.NORMAL);
        safetyIO.setIsLocalEStop(iiwa.getSafetyState().getEmergencyStopInt() == SunriseSafetyState.EmergencyStop.ACTIVE);
        safetyIO.setIsExternalEStop(iiwa.getSafetyState().getEmergencyStopEx() == SunriseSafetyState.EmergencyStop.ACTIVE);
    }

    @Override public void dispose()
    {
        safetyIO.setIsExternalEStop(true);
        safetyIO.setIsOperatorSafety(false);
        safetyIO.setModeT1(false);
        safetyIO.setModeT2(false);
        safetyIO.setMoveEnable(false);
        safetyIO.setIsLocalEStop(true);
        safetyIO.setIsExternalEStop(true);
    }
}