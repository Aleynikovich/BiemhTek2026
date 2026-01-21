package com.kuka.generated.ioAccess;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>GripperStatus</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class GripperStatusIOGroup extends AbstractIOGroup
{
    /**
     * Constructor to create an instance of class 'GripperStatus'.<br>
     * <i>This constructor is automatically generated. Please, do not modify!</i>
     *
     * @param controller the controller, which has access to the I/O group 'GripperStatus'
     */
    @Inject
    public GripperStatusIOGroup(Controller controller)
    {
        super(controller, "GripperStatus");

        addDigitalOutput("Gripper1Open", IOTypes.BOOLEAN, 1);
        addDigitalOutput("Gripper1Closed", IOTypes.BOOLEAN, 1);
        addDigitalOutput("Gripper2Closed", IOTypes.BOOLEAN, 1);
        addDigitalOutput("Gripper2Open", IOTypes.BOOLEAN, 1);
        addDigitalOutput("ZRes1", IOTypes.BOOLEAN, 1);
        addDigitalOutput("ZRes2", IOTypes.BOOLEAN, 1);
        addDigitalOutput("ZRes3", IOTypes.BOOLEAN, 1);
        addDigitalOutput("ZRes4", IOTypes.BOOLEAN, 1);
    }

    /**
     * Gets the value of the <b>digital output '<i>Gripper1Open</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @return current value of the digital output 'Gripper1Open'
     */
    public boolean getGripper1Open()
    {
        return getBooleanIOValue("Gripper1Open", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>Gripper1Open</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @param value the value, which has to be written to the digital output 'Gripper1Open'
     */
    public void setGripper1Open(java.lang.Boolean value)
    {
        setDigitalOutput("Gripper1Open", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>Gripper1Closed</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @return current value of the digital output 'Gripper1Closed'
     */
    public boolean getGripper1Closed()
    {
        return getBooleanIOValue("Gripper1Closed", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>Gripper1Closed</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @param value the value, which has to be written to the digital output 'Gripper1Closed'
     */
    public void setGripper1Closed(java.lang.Boolean value)
    {
        setDigitalOutput("Gripper1Closed", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>Gripper2Closed</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @return current value of the digital output 'Gripper2Closed'
     */
    public boolean getGripper2Closed()
    {
        return getBooleanIOValue("Gripper2Closed", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>Gripper2Closed</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @param value the value, which has to be written to the digital output 'Gripper2Closed'
     */
    public void setGripper2Closed(java.lang.Boolean value)
    {
        setDigitalOutput("Gripper2Closed", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>Gripper2Open</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @return current value of the digital output 'Gripper2Open'
     */
    public boolean getGripper2Open()
    {
        return getBooleanIOValue("Gripper2Open", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>Gripper2Open</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @param value the value, which has to be written to the digital output 'Gripper2Open'
     */
    public void setGripper2Open(java.lang.Boolean value)
    {
        setDigitalOutput("Gripper2Open", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>ZRes1</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @return current value of the digital output 'ZRes1'
     */
    public boolean getZRes1()
    {
        return getBooleanIOValue("ZRes1", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>ZRes1</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @param value the value, which has to be written to the digital output 'ZRes1'
     */
    public void setZRes1(java.lang.Boolean value)
    {
        setDigitalOutput("ZRes1", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>ZRes2</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @return current value of the digital output 'ZRes2'
     */
    public boolean getZRes2()
    {
        return getBooleanIOValue("ZRes2", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>ZRes2</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @param value the value, which has to be written to the digital output 'ZRes2'
     */
    public void setZRes2(java.lang.Boolean value)
    {
        setDigitalOutput("ZRes2", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>ZRes3</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @return current value of the digital output 'ZRes3'
     */
    public boolean getZRes3()
    {
        return getBooleanIOValue("ZRes3", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>ZRes3</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @param value the value, which has to be written to the digital output 'ZRes3'
     */
    public void setZRes3(java.lang.Boolean value)
    {
        setDigitalOutput("ZRes3", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>ZRes4</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @return current value of the digital output 'ZRes4'
     */
    public boolean getZRes4()
    {
        return getBooleanIOValue("ZRes4", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>ZRes4</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [false; true]
     *
     * @param value the value, which has to be written to the digital output 'ZRes4'
     */
    public void setZRes4(java.lang.Boolean value)
    {
        setDigitalOutput("ZRes4", value);
    }

}
