package biemhTekniker.IOPolling;

import biemhTekniker.logger.Logger;
import com.kuka.generated.ioAccess.RobotSafetyIOGroup;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;
import com.kuka.roboticsAPI.controllerModel.sunrise.SunriseSafetyState;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.deviceModel.OperationMode;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class RobotSafetyPolling extends RoboticsAPICyclicBackgroundTask
{
    private static final Logger log = Logger.getLogger(RobotSafetyPolling.class);
    @Inject
    RobotSafetyIOGroup safetyIO;
    @Inject
    LBR iiwa;

    @Override
    public void initialize()
    {
        initializeCyclic(0, 10, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);
    }

    @Override
    public void runCyclic()
    {
        try
        {
            safetyIO.setIsExternalEStop(iiwa.getSafetyState().getEmergencyStopEx() == SunriseSafetyState.EmergencyStop.ACTIVE);
            safetyIO.setIsLocalEStop(iiwa.getSafetyState().getEmergencyStopInt() == SunriseSafetyState.EmergencyStop.ACTIVE);
            safetyIO.setIsOperatorSafety(iiwa.getSafetyState().getOperatorSafetyState() == SunriseSafetyState.OperatorSafety.OPERATOR_SAFETY_CLOSED);
            safetyIO.setModeT1(iiwa.getSafetyState().getOperationMode() == OperationMode.T1);
            safetyIO.setModeAut(iiwa.getSafetyState().getOperationMode() == OperationMode.AUT);
            safetyIO.setMoveEnable(iiwa.getSafetyState().getEnablingDeviceState() == SunriseSafetyState.EnablingDeviceState.NONE);
        } catch (Exception e)
        {
            // Log error but continue running - PLC may be in STOP mode
            log.error("Failed to update safety state: " + e.getMessage());
        }
    }

    @Override
    public void dispose()
    {
        safetyIO.setIsExternalEStop(true);
        safetyIO.setIsOperatorSafety(false);
        safetyIO.setModeT1(false);
        safetyIO.setModeAut(false);
        safetyIO.setMoveEnable(false);
        safetyIO.setIsLocalEStop(true);
    }
}