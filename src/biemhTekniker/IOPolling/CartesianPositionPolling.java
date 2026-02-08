package biemhTekniker.IOPolling;

import com.kuka.generated.ioAccess.RobotCartesianPositionIOGroup;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.deviceModel.LBR;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class CartesianPositionPolling extends RoboticsAPICyclicBackgroundTask
{
    private final int decimalMultiplier = 10;
    @Inject
    private Controller sunrise;
    @Inject
    private LBR iiwa;
    @Inject
    private RobotCartesianPositionIOGroup currentCartesianPosition;

    @Override
    public void initialize()
    {
        // initialize your task here
        initializeCyclic(0, 500, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
    }

    @Override
    public void runCyclic()
    {
        try
        {
            // XYZ
            currentCartesianPosition.setX((int) Math.round(iiwa.getCurrentCartesianPosition(iiwa.getFlange()).getX() * decimalMultiplier));
            currentCartesianPosition.setY((int) Math.round(iiwa.getCurrentCartesianPosition(iiwa.getFlange()).getY() * decimalMultiplier));
            currentCartesianPosition.setZ((int) Math.round(iiwa.getCurrentCartesianPosition(iiwa.getFlange()).getZ() * decimalMultiplier));
            // ABC
            currentCartesianPosition.setA((int) Math.round(Math.toDegrees(iiwa.getCurrentCartesianPosition(iiwa.getFlange()).getAlphaRad()) * decimalMultiplier));
            currentCartesianPosition.setB((int) Math.round(Math.toDegrees(iiwa.getCurrentCartesianPosition(iiwa.getFlange()).getBetaRad()) * decimalMultiplier));
            currentCartesianPosition.setC((int) Math.round(Math.toDegrees(iiwa.getCurrentCartesianPosition(iiwa.getFlange()).getGammaRad()) * decimalMultiplier));
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}