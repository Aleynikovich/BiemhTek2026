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
        initializeCyclic(0, 1, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
    }

    @Override public void runCyclic()
    {
        ISafetyState safetyState = iiwa.getSafetyState();
        safetyIO.setIsExternalEStop(safetyState.getEmergencyStopEx() == SunriseSafetyState.EmergencyStop.ACTIVE);
        safetyIO.setIsOperatorSafety(safetyState.getOperatorSafetyState() == SunriseSafetyState.OperatorSafety.OPERATOR_SAFETY_CLOSED );
        safetyIO.setModeT1(safetyState.getOperationMode() == OperationMode.T1);
        safetyIO.setModeT2(safetyState.getOperationMode() == OperationMode.AUT);
        safetyIO.setMoveEnable(safetyState.getEnablingDeviceState() == SunriseSafetyState.EnablingDeviceState.NORMAL);
        safetyIO.setIsLocalEStop(safetyState.getEmergencyStopInt() == SunriseSafetyState.EmergencyStop.ACTIVE);
        safetyIO.setIsExternalEStop(safetyState.getEmergencyStopEx() == SunriseSafetyState.EmergencyStop.ACTIVE);
    }
}