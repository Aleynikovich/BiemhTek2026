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
public class CartesianPositionPolling extends RoboticsAPICyclicBackgroundTask {
	@Inject
	private Controller sunrise;
    @Inject
    private LBR iiwa;
    @Inject
    private RobotCartesianPositionIOGroup currentCartesianPosition;
	

	@Override
	public void initialize() {
		// initialize your task here
		initializeCyclic(0, 500, TimeUnit.MILLISECONDS,
				CycleBehavior.BestEffort);
	}

	@Override
	public void runCyclic() {
		
		currentCartesianPosition.setX((int) iiwa.getCurrentCartesianPosition(iiwa.getFlange()).getX()*100);
		currentCartesianPosition.setY((int) iiwa.getCurrentCartesianPosition(iiwa.getFlange()).getY()*100);
		currentCartesianPosition.setZ((int) iiwa.getCurrentCartesianPosition(iiwa.getFlange()).getZ()*100);
		currentCartesianPosition.setA((int) Math.toDegrees(iiwa.getCurrentCartesianPosition(iiwa.getFlange()).getAlphaRad())*100);
		currentCartesianPosition.setB((int) Math.toDegrees(iiwa.getCurrentCartesianPosition(iiwa.getFlange()).getBetaRad())*100);
		currentCartesianPosition.setC((int) Math.toDegrees(iiwa.getCurrentCartesianPosition(iiwa.getFlange()).getGammaRad())*100);

	}
}