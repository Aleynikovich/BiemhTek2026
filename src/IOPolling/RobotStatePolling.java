package IOPolling;


import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.controllerModel.DispatchedEventData;
import com.kuka.roboticsAPI.controllerModel.IControllerStateListener;
import com.kuka.roboticsAPI.controllerModel.StatePortData;
import com.kuka.roboticsAPI.controllerModel.sunrise.ISafetyState;
import com.kuka.roboticsAPI.deviceModel.Device;
import com.kuka.roboticsAPI.deviceModel.LBR;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class RobotStatePolling extends RoboticsAPICyclicBackgroundTask
{
    @Inject private Controller sunrise;
    @Inject private LBR        iiwa;

    ISafetyState safetyState = iiwa.getSafetyState();
    boolean motionReady, activeMotion;

    @Override public void initialize()
    {
        initializeCyclic(0, 4, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
    }

    @Override public void runCyclic()
    {

    }
}