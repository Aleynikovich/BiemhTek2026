package IOPolling;

import com.kuka.generated.ioAccess.RobotJointPositionIOGroup;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.deviceModel.LBR;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class AxisPositionPolling extends RoboticsAPICyclicBackgroundTask
{
    private final   int                       decimalMultiplier = 10;
    @Inject private Controller                sunrise;
    @Inject private LBR                       iiwa;
    @Inject private RobotJointPositionIOGroup currentAxisPosition;

    @Override public void initialize()
    {
        // initialize your task here
        initializeCyclic(0, 500, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
    }

    @Override public void runCyclic()
    {
        try
        {
            currentAxisPosition.setA1((int) Math.round(iiwa.getCurrentJointPosition().get(0) * decimalMultiplier));
            currentAxisPosition.setA1((int) Math.round(iiwa.getCurrentJointPosition().get(1) * decimalMultiplier));
            currentAxisPosition.setA1((int) Math.round(iiwa.getCurrentJointPosition().get(2) * decimalMultiplier));
            currentAxisPosition.setA1((int) Math.round(iiwa.getCurrentJointPosition().get(3) * decimalMultiplier));
            currentAxisPosition.setA1((int) Math.round(iiwa.getCurrentJointPosition().get(4) * decimalMultiplier));
            currentAxisPosition.setA1((int) Math.round(iiwa.getCurrentJointPosition().get(5) * decimalMultiplier));
            currentAxisPosition.setA1((int) Math.round(iiwa.getCurrentJointPosition().get(6) * decimalMultiplier));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}