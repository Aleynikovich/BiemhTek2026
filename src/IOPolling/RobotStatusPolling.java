package IOPolling;


import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.controllerModel.DispatchedEventData;
import com.kuka.roboticsAPI.controllerModel.IControllerStateListener;
import com.kuka.roboticsAPI.controllerModel.StatePortData;
import com.kuka.roboticsAPI.deviceModel.Device;
import com.kuka.roboticsAPI.deviceModel.LBR;

/**
 * Implementation of a cyclic background task.
 * <p>
 * It provides the {@link RoboticsAPICyclicBackgroundTask#runCyclic} method
 * which will be called cyclically with the specified period.<br>
 * Cycle period and initial delay can be set by calling
 * {@link RoboticsAPICyclicBackgroundTask#initializeCyclic} method in the
 * {@link RoboticsAPIBackgroundTask#initialize()} method of the inheriting
 * class.<br>
 * The cyclic background task can be terminated via
 * {@link RoboticsAPICyclicBackgroundTask#getCyclicFuture()#cancel()} method or
 * stopping of the task.
 * @see UseRoboticsAPIContext
 *
 */
public class RobotStatusPolling extends RoboticsAPICyclicBackgroundTask {
    @Inject
    private Controller sunrise;
    @Inject
    private LBR iiwa;
    @Inject



    @Override
    public void initialize()
    {
        initializeCyclic(0, 500, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);

        sunrise.addControllerListener(new IControllerStateListener()
        {
            @Override
            public void onShutdown(Controller controller)
            {

            }

            @Override
            public void onStatePortChangeReceived(Controller controller, StatePortData statePortData)
            {

            }

            @Override
            public void onIsReadyToMoveChanged(Device device, boolean b)
            {

            }

            @Override
            public void onFieldBusDeviceConfigurationChangeReceived(String s, DispatchedEventData dispatchedEventData)
            {

            }

            @Override
            public void onFieldBusDeviceIdentificationRequestReceived(String s, DispatchedEventData dispatchedEventData)
            {

            }
        });
    }

    @Override
    public void runCyclic()
    {

    }
}