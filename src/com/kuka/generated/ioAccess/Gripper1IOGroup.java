package com.kuka.generated.ioAccess;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>Gripper1</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class Gripper1IOGroup extends AbstractIOGroup
{
    /**
     * Constructor to create an instance of class 'Gripper1'.<br>
     * <i>This constructor is automatically generated. Please, do not modify!</i>
     *
     * @param controller the controller, which has access to the I/O group 'Gripper1'
     */
    @Inject
    public Gripper1IOGroup(Controller controller)
    {
        super(controller, "Gripper1");

        addDigitalOutput("Open", IOTypes.BOOLEAN, 1);
        addDigitalOutput("Close", IOTypes.BOOLEAN, 1);
        addInput("IsOpen", IOTypes.BOOLEAN, 1);
        addInput("IsClosed", IOTypes.BOOLEAN, 1);
    }

    /**
     * Gets the value of the <b>digital output '<i>Open</i>'</b>.<br>
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
     * @return current value of the digital output 'Open'
     */
    public boolean getOpen()
    {
        return getBooleanIOValue("Open", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>Open</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'Open'
     */
    public void setOpen(java.lang.Boolean value)
    {
        setDigitalOutput("Open", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>Close</i>'</b>.<br>
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
     * @return current value of the digital output 'Close'
     */
    public boolean getClose()
    {
        return getBooleanIOValue("Close", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>Close</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'Close'
     */
    public void setClose(java.lang.Boolean value)
    {
        setDigitalOutput("Close", value);
    }

    /**
     * Gets the value of the <b>digital input '<i>IsOpen</i>'</b>.<br>
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
     * @return current value of the digital input 'IsOpen'
     */
    public boolean getIsOpen()
    {
        return getBooleanIOValue("IsOpen", false);
    }

    /**
     * Gets the value of the <b>digital input '<i>IsClosed</i>'</b>.<br>
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
     * @return current value of the digital input 'IsClosed'
     */
    public boolean getIsClosed()
    {
        return getBooleanIOValue("IsClosed", false);
    }

}
