package IOPolling;

import com.kuka.generated.ioAccess.RobotJointPositionIOGroup;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.deviceModel.LBR;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class AxisPositionPolling extends RoboticsAPICyclicBackgroundTask
{
    private final   int                       decimalMultiplier = 10;
    @Inject private Controller                sunrise;
    @Inject private LBR                       iiwa;
    @Inject private RobotJointPositionIOGroup currentAxisPosition;

    // Cache the setters here so they are only created once
    private List<Consumer<Integer>> axisSetters;

    @Override public void initialize()
    {
        axisSetters = Arrays.asList(currentAxisPosition::setA1, currentAxisPosition::setA2, currentAxisPosition::setA3, currentAxisPosition::setA4, currentAxisPosition::setA5, currentAxisPosition::setA6, currentAxisPosition::setA7);
        initializeCyclic(0, 500, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
    }

    @Override public void runCyclic()
    {
        try
        {
            var jointPos = iiwa.getCurrentJointPosition();

            for (int i = 0; i < axisSetters.size(); i++)
            {
                int scaledValue = (int) Math.round(Math.toDegrees(jointPos.get(i)) * decimalMultiplier);
                axisSetters.get(i).accept(scaledValue);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}