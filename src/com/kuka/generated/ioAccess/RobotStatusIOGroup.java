package com.kuka.generated.ioAccess;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RobotStatusIOGroup extends AbstractIOGroup {
    @Inject
    public RobotStatusIOGroup(Controller controller) {
        super(controller, "RobotStatus");
        addDigitalOutput("IsInHome", IOTypes.BOOLEAN, 1);
        addDigitalOutput("IsMastered", IOTypes.BOOLEAN, 1);
        addDigitalOutput("IsReadyToMove", IOTypes.BOOLEAN, 1);
        addDigitalOutput("IsActive", IOTypes.BOOLEAN, 1);
        addDigitalOutput("IsReferenced", IOTypes.BOOLEAN, 1);
    }

    public void setIsInHome(boolean value) { setDigitalOutput("IsInHome", value); }
    public void setIsMastered(boolean value) { setDigitalOutput("IsMastered", value); }
    public void setIsReadyToMove(boolean value) { setDigitalOutput("IsReadyToMove", value); }
    public void setIsActive(boolean value) { setDigitalOutput("IsActive", value); }
    public void setIsReferenced(boolean value) { setDigitalOutput("IsReferenced", value); }
}