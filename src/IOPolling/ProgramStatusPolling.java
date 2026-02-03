package IOPolling;


import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import com.kuka.generated.ioAccess.RobotCartesianPositionIOGroup;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;
import com.kuka.roboticsAPI.controllerModel.Controller;
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
public class ProgramStatusPolling extends RoboticsAPICyclicBackgroundTask {
    @Inject
    private Controller sunrise;
    @Inject
    private LBR iiwa;
    @Inject


    @Override
    public void initialize() {
        // initialize your task here
        initializeCyclic(0, 500, TimeUnit.MILLISECONDS,
                CycleBehavior.BestEffort);
    }

    @Override
    public void runCyclic()
    {


    }
}