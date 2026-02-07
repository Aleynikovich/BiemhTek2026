package biemhTekniker.programs;

import biemhTekniker.data.WorkpieceData;
import biemhTekniker.vision.SmartPickingProtocol;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;

/**
 * Immutable container for shared dependencies used by robot programs.
 * Bundles all common dependencies to simplify program constructors.
 * Java 1.7 compatible - uses final fields and no modern Java features.
 */
public class ProgramContext
{
    private final RoboticsAPIApplication application;
    private final LBR                    iiwa;
    private final Tool                   gripper;
    private final MediaFlangeIOGroup     gripperIO;
    private final WorkpieceData          workpieceData;
    private final SmartPickingProtocol   protocol;

    /**
     * Creates a new immutable program context.
     *
     * @param application   The robotics API application
     * @param iiwa          The LBR robot
     * @param gripper       The gripper tool
     * @param gripperIO     The gripper I/O group
     * @param workpieceData Shared workpiece data
     * @param protocol      SmartPicking protocol for vision communication
     */
    public ProgramContext(
        RoboticsAPIApplication application,
        LBR iiwa,
        Tool gripper,
        MediaFlangeIOGroup gripperIO,
        WorkpieceData workpieceData,
        SmartPickingProtocol protocol)
    {
        this.application   = application;
        this.iiwa          = iiwa;
        this.gripper       = gripper;
        this.gripperIO     = gripperIO;
        this.workpieceData = workpieceData;
        this.protocol      = protocol;
    }

    /**
     * @return The robotics API application
     */
    public RoboticsAPIApplication getApplication()
    {
        return application;
    }

    /**
     * @return The LBR robot
     */
    public LBR getIiwa()
    {
        return iiwa;
    }

    /**
     * @return The gripper tool
     */
    public Tool getGripper()
    {
        return gripper;
    }

    /**
     * @return The gripper I/O group
     */
    public MediaFlangeIOGroup getGripperIO()
    {
        return gripperIO;
    }

    /**
     * @return Shared workpiece data
     */
    public WorkpieceData getWorkpieceData()
    {
        return workpieceData;
    }

    /**
     * @return SmartPicking protocol for vision communication
     */
    public SmartPickingProtocol getProtocol()
    {
        return protocol;
    }
}
