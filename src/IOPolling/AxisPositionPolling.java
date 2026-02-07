package IOPolling;

import com.kuka.generated.ioAccess.RobotJointPositionIOGroup;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.deviceModel.JointPosition;
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
            for (int i = 0;i < iiwa.getCurrentJointPosition().getAxisCount(); i++)
            {
                currentAxisPosition.setA1((int) Math.round(Math.toDegrees(iiwa.getCurrentJointPosition().get(i)) * decimalMultiplier));
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}