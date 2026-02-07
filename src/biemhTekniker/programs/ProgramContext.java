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
public record ProgramContext(RoboticsAPIApplication application, LBR iiwa, Tool gripper, MediaFlangeIOGroup gripperIO, WorkpieceData workpieceData, SmartPickingProtocol visionProtocol)
{
}
