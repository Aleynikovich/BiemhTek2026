package biemhTekniker.programs;

import biemhTekniker.data.WorkpieceData;
import biemhTekniker.vision.SmartPickingProtocol;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;

/**
 * Context object that holds all dependencies needed by programs.
 * This is passed to factories to create program instances.
 */
public class ProgramContext
{
    private final RoboticsAPIApplication application;
    private final LBR                    iiwa;
    private final Tool                   gripper;
    private final MediaFlangeIOGroup     gripperIO;
    private final WorkpieceData          workpieceData;
    private final SmartPickingProtocol   visionProtocol;

    public ProgramContext(RoboticsAPIApplication application, LBR iiwa, Tool gripper, MediaFlangeIOGroup gripperIO, WorkpieceData workpieceData, SmartPickingProtocol visionProtocol)
    {
        this.application    = application;
        this.iiwa           = iiwa;
        this.gripper        = gripper;
        this.gripperIO      = gripperIO;
        this.workpieceData  = workpieceData;
        this.visionProtocol = visionProtocol;
    }

    public RoboticsAPIApplication getApplication()
    {
        return application;
    }

    public LBR getIiwa()
    {
        return iiwa;
    }

    public Tool getGripper()
    {
        return gripper;
    }

    public MediaFlangeIOGroup getGripperIO()
    {
        return gripperIO;
    }

    public WorkpieceData getWorkpieceData()
    {
        return workpieceData;
    }

    public SmartPickingProtocol getVisionProtocol()
    {
        return visionProtocol;
    }
}
