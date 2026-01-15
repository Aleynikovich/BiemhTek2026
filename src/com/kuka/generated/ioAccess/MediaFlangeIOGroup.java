package com.kuka.generated.ioAccess;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>MediaFlange</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class MediaFlangeIOGroup extends AbstractIOGroup
{
    /**
     * Constructor to create an instance of class 'MediaFlange'.<br>
     * <i>This constructor is automatically generated. Please, do not modify!</i>
     *
     * @param controller the controller, which has access to the I/O group 'MediaFlange'
     */
    @Inject
    public MediaFlangeIOGroup(Controller controller)
    {
        super(controller, "MediaFlange");

        addDigitalOutput("Gripper1_Open", IOTypes.BOOLEAN, 1);
        addDigitalOutput("Gripper2_Open", IOTypes.BOOLEAN, 1);
        addDigitalOutput("Gripper1_Close", IOTypes.BOOLEAN, 1);
        addDigitalOutput("Gripper2_Close", IOTypes.BOOLEAN, 1);
        addInput("Gripper1_isOpen", IOTypes.BOOLEAN, 1);
        addInput("Gripper2_isOpen", IOTypes.BOOLEAN, 1);
        addInput("Gripper1_isClosed", IOTypes.BOOLEAN, 1);
        addInput("Gripper2_isClosed", IOTypes.BOOLEAN, 1);
    }

    /**
     * Gets the value of the <b>digital output '<i>Gripper1_Open</i>'</b>.<br>
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
     * @return current value of the digital output 'Gripper1_Open'
     */
    public boolean getGripper1_Open()
    {
        return getBooleanIOValue("Gripper1_Open", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>Gripper1_Open</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'Gripper1_Open'
     */
    public void setGripper1_Open(java.lang.Boolean value)
    {
        setDigitalOutput("Gripper1_Open", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>Gripper2_Open</i>'</b>.<br>
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
     * @return current value of the digital output 'Gripper2_Open'
     */
    public boolean getGripper2_Open()
    {
        return getBooleanIOValue("Gripper2_Open", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>Gripper2_Open</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'Gripper2_Open'
     */
    public void setGripper2_Open(java.lang.Boolean value)
    {
        setDigitalOutput("Gripper2_Open", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>Gripper1_Close</i>'</b>.<br>
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
     * @return current value of the digital output 'Gripper1_Close'
     */
    public boolean getGripper1_Close()
    {
        return getBooleanIOValue("Gripper1_Close", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>Gripper1_Close</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'Gripper1_Close'
     */
    public void setGripper1_Close(java.lang.Boolean value)
    {
        setDigitalOutput("Gripper1_Close", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>Gripper2_Close</i>'</b>.<br>
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
     * @return current value of the digital output 'Gripper2_Close'
     */
    public boolean getGripper2_Close()
    {
        return getBooleanIOValue("Gripper2_Close", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>Gripper2_Close</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'Gripper2_Close'
     */
    public void setGripper2_Close(java.lang.Boolean value)
    {
        setDigitalOutput("Gripper2_Close", value);
    }

    /**
     * Gets the value of the <b>digital input '<i>Gripper1_isOpen</i>'</b>.<br>
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
     * @return current value of the digital input 'Gripper1_isOpen'
     */
    public boolean getGripper1_isOpen()
    {
        return getBooleanIOValue("Gripper1_isOpen", false);
    }

    /**
     * Gets the value of the <b>digital input '<i>Gripper2_isOpen</i>'</b>.<br>
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
     * @return current value of the digital input 'Gripper2_isOpen'
     */
    public boolean getGripper2_isOpen()
    {
        return getBooleanIOValue("Gripper2_isOpen", false);
    }

    /**
     * Gets the value of the <b>digital input '<i>Gripper1_isClosed</i>'</b>.<br>
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
     * @return current value of the digital input 'Gripper1_isClosed'
     */
    public boolean getGripper1_isClosed()
    {
        return getBooleanIOValue("Gripper1_isClosed", false);
    }

    /**
     * Gets the value of the <b>digital input '<i>Gripper2_isClosed</i>'</b>.<br>
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
     * @return current value of the digital input 'Gripper2_isClosed'
     */
    public boolean getGripper2_isClosed()
    {
        return getBooleanIOValue("Gripper2_isClosed", false);
    }

}
