package com.kuka.generated.ioAccess;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>PlcRequestsGrippers</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class PlcRequestsGrippersIOGroup extends AbstractIOGroup
{
    /**
     * Constructor to create an instance of class 'PlcRequestsGrippers'.<br>
     * <i>This constructor is automatically generated. Please, do not modify!</i>
     *
     * @param controller the controller, which has access to the I/O group 'PlcRequestsGrippers'
     */
    @Inject
    public PlcRequestsGrippersIOGroup(Controller controller)
    {
        super(controller, "PlcRequestsGrippers");

        addMockedInput("Gripper1Open", IOTypes.BOOLEAN, 1, false);
        addMockedInput("Gripper1Close", IOTypes.BOOLEAN, 1, false);
        addMockedInput("Gripper2Open", IOTypes.BOOLEAN, 1, false);
        addMockedInput("Gripper2Close", IOTypes.BOOLEAN, 1, false);
    }

    /**
     * Gets the value of the <b>digital input '<i>Gripper1Open</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital input
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @return current value of the digital input 'Gripper1Open'
     * @deprecated The output 'Gripper1Open' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
     */
    @Deprecated
    public boolean getGripper1Open()
    {
        return getBooleanIOValue("Gripper1Open", false);
    }

    /**
     * Sets the value of the <b>mocked digital input '<i>Gripper1Open</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital input
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @param value the value, which has to be written to the mocked digital input 'Gripper1Open'
     * @deprecated The output 'Gripper1Open' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
     */
    @Deprecated
    public void setMockedGripper1OpenValue(java.lang.Boolean value)
    {
        setMockedInput("Gripper1Open", value);
    }

    /**
     * Gets the value of the <b>digital input '<i>Gripper1Close</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital input
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @return current value of the digital input 'Gripper1Close'
     * @deprecated The output 'Gripper1Close' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
     */
    @Deprecated
    public boolean getGripper1Close()
    {
        return getBooleanIOValue("Gripper1Close", false);
    }

    /**
     * Sets the value of the <b>mocked digital input '<i>Gripper1Close</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital input
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @param value the value, which has to be written to the mocked digital input 'Gripper1Close'
     * @deprecated The output 'Gripper1Close' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
     */
    @Deprecated
    public void setMockedGripper1CloseValue(java.lang.Boolean value)
    {
        setMockedInput("Gripper1Close", value);
    }

    /**
     * Gets the value of the <b>digital input '<i>Gripper2Open</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital input
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @return current value of the digital input 'Gripper2Open'
     * @deprecated The output 'Gripper2Open' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
     */
    @Deprecated
    public boolean getGripper2Open()
    {
        return getBooleanIOValue("Gripper2Open", false);
    }

    /**
     * Sets the value of the <b>mocked digital input '<i>Gripper2Open</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital input
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @param value the value, which has to be written to the mocked digital input 'Gripper2Open'
     * @deprecated The output 'Gripper2Open' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
     */
    @Deprecated
    public void setMockedGripper2OpenValue(java.lang.Boolean value)
    {
        setMockedInput("Gripper2Open", value);
    }

    /**
     * Gets the value of the <b>digital input '<i>Gripper2Close</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital input
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @return current value of the digital input 'Gripper2Close'
     * @deprecated The output 'Gripper2Close' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
     */
    @Deprecated
    public boolean getGripper2Close()
    {
        return getBooleanIOValue("Gripper2Close", false);
    }

    /**
     * Sets the value of the <b>mocked digital input '<i>Gripper2Close</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital input
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @param value the value, which has to be written to the mocked digital input 'Gripper2Close'
     * @deprecated The output 'Gripper2Close' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
     */
    @Deprecated
    public void setMockedGripper2CloseValue(java.lang.Boolean value)
    {
        setMockedInput("Gripper2Close", value);
    }

}
