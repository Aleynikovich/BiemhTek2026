package biemhTekniker.iopolling;

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
    private final int decimalMultiplier = 10;

    @Inject
    private Controller sunrise;
    @Inject
    private LBR iiwa;
    @Inject
    private RobotJointPositionIOGroup currentAxisPosition;
    private AxisSetter[] axisSetters;

    @Override
    public void initialize()
    {
        axisSetters = new AxisSetter[]{new AxisSetter()
        {
            public void set(int v)
            {
                currentAxisPosition.setA1(v);
            }
        }, new AxisSetter()
        {
            public void set(int v)
            {
                currentAxisPosition.setA2(v);
            }
        }, new AxisSetter()
        {
            public void set(int v)
            {
                currentAxisPosition.setA3(v);
            }
        }, new AxisSetter()
        {
            public void set(int v)
            {
                currentAxisPosition.setA4(v);
            }
        }, new AxisSetter()
        {
            public void set(int v)
            {
                currentAxisPosition.setA5(v);
            }
        }, new AxisSetter()
        {
            public void set(int v)
            {
                currentAxisPosition.setA6(v);
            }
        }, new AxisSetter()
        {
            public void set(int v)
            {
                currentAxisPosition.setA7(v);
            }
        }};

        initializeCyclic(0, 500, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
    }

    @Override
    public void runCyclic()
    {
        try
        {
            JointPosition jointPos = iiwa.getCurrentJointPosition();

            for (int i = 0; i < axisSetters.length; i++)
            {
                int scaledValue = (int) Math.round(Math.toDegrees(jointPos.get(i)) * decimalMultiplier);
                axisSetters[i].set(scaledValue);
                switch (i) {
                	case 1:
                		currentAxisPosition.setA1(scaledValue);
                	case 2:
                		currentAxisPosition.setA2(scaledValue);
                	case 3:
                		currentAxisPosition.setA3(scaledValue);
                	case 4:
                		currentAxisPosition.setA4(scaledValue);
                	case 5:
                		currentAxisPosition.setA5(scaledValue);
                	case 6:
                		currentAxisPosition.setA6(scaledValue);
                	case 7:
                		currentAxisPosition.setA7(scaledValue);
                }
            }
        } catch (Exception e)
        {
            getLogger().error(e.getMessage());
        }
    }

    @Override
    public void dispose()
    {

    }

    private interface AxisSetter
    {
        void set(int value);
    }
}