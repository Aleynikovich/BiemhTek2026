package com.kuka.generated.ioAccess;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>PlcRequestsProgram</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class PlcRequestsProgramIOGroup extends AbstractIOGroup
{
    /**
     * Constructor to create an instance of class 'PlcRequestsProgram'.<br>
     * <i>This constructor is automatically generated. Please, do not modify!</i>
     *
     * @param controller the controller, which has access to the I/O group 'PlcRequestsProgram'
     */
    @Inject
    public PlcRequestsProgramIOGroup(Controller controller)
    {
        super(controller, "PlcRequestsProgram");

        addMockedInput("ProgramStop", IOTypes.BOOLEAN, 1, false);
        addMockedInput("ProgramAbort", IOTypes.BOOLEAN, 1, false);
        addMockedInput("ProgramStart", IOTypes.BOOLEAN, 1, false);
    }

    /**
     * Gets the value of the <b>digital input '<i>ProgramStop</i>'</b>.<br>
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
     * @return current value of the digital input 'ProgramStop'
     * @deprecated The output 'ProgramStop' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
     */
    @Deprecated
    public boolean getProgramStop()
    {
        return getBooleanIOValue("ProgramStop", false);
    }

    /**
     * Sets the value of the <b>mocked digital input '<i>ProgramStop</i>'</b>.<br>
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
     * @param value the value, which has to be written to the mocked digital input 'ProgramStop'
     * @deprecated The output 'ProgramStop' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
     */
    @Deprecated
    public void setMockedProgramStopValue(java.lang.Boolean value)
    {
        setMockedInput("ProgramStop", value);
    }

    /**
     * Gets the value of the <b>digital input '<i>ProgramAbort</i>'</b>.<br>
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
     * @return current value of the digital input 'ProgramAbort'
     * @deprecated The output 'ProgramAbort' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
     */
    @Deprecated
    public boolean getProgramAbort()
    {
        return getBooleanIOValue("ProgramAbort", false);
    }

    /**
     * Sets the value of the <b>mocked digital input '<i>ProgramAbort</i>'</b>.<br>
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
     * @param value the value, which has to be written to the mocked digital input 'ProgramAbort'
     * @deprecated The output 'ProgramAbort' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
     */
    @Deprecated
    public void setMockedProgramAbortValue(java.lang.Boolean value)
    {
        setMockedInput("ProgramAbort", value);
    }

    /**
     * Gets the value of the <b>digital input '<i>ProgramStart</i>'</b>.<br>
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
     * @return current value of the digital input 'ProgramStart'
     * @deprecated The output 'ProgramStart' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
     */
    @Deprecated
    public boolean getProgramStart()
    {
        return getBooleanIOValue("ProgramStart", false);
    }

    /**
     * Sets the value of the <b>mocked digital input '<i>ProgramStart</i>'</b>.<br>
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
     * @param value the value, which has to be written to the mocked digital input 'ProgramStart'
     * @deprecated The output 'ProgramStart' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
     */
    @Deprecated
    public void setMockedProgramStartValue(java.lang.Boolean value)
    {
        setMockedInput("ProgramStart", value);
    }

}
