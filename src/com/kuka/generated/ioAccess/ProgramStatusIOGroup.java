package com.kuka.generated.ioAccess;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>ProgramStatus</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class ProgramStatusIOGroup extends AbstractIOGroup
{
    /**
     * Constructor to create an instance of class 'ProgramStatus'.<br>
     * <i>This constructor is automatically generated. Please, do not modify!</i>
     *
     * @param controller the controller, which has access to the I/O group 'ProgramStatus'
     */
    @Inject
    public ProgramStatusIOGroup(Controller controller)
    {
        super(controller, "ProgramStatus");

        addDigitalOutput("Running", IOTypes.BOOLEAN, 1);
        addDigitalOutput("Selected", IOTypes.BOOLEAN, 1);
        addDigitalOutput("Stopped", IOTypes.BOOLEAN, 1);
        addDigitalOutput("Crashed", IOTypes.BOOLEAN, 1);
        addDigitalOutput("ZRes1", IOTypes.BOOLEAN, 1);
        addDigitalOutput("ZRes2", IOTypes.BOOLEAN, 1);
        addDigitalOutput("ZRes3", IOTypes.BOOLEAN, 1);
        addDigitalOutput("ZRes4", IOTypes.BOOLEAN, 1);
    }

    /**
     * Gets the value of the <b>digital output '<i>Running</i>'</b>.<br>
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
     * @return current value of the digital output 'Running'
     */
    public boolean getRunning()
    {
        return getBooleanIOValue("Running", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>Running</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'Running'
     */
    public void setRunning(java.lang.Boolean value)
    {
        setDigitalOutput("Running", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>Selected</i>'</b>.<br>
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
     * @return current value of the digital output 'Selected'
     */
    public boolean getSelected()
    {
        return getBooleanIOValue("Selected", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>Selected</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'Selected'
     */
    public void setSelected(java.lang.Boolean value)
    {
        setDigitalOutput("Selected", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>Stopped</i>'</b>.<br>
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
     * @return current value of the digital output 'Stopped'
     */
    public boolean getStopped()
    {
        return getBooleanIOValue("Stopped", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>Stopped</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'Stopped'
     */
    public void setStopped(java.lang.Boolean value)
    {
        setDigitalOutput("Stopped", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>Crashed</i>'</b>.<br>
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
     * @return current value of the digital output 'Crashed'
     */
    public boolean getCrashed()
    {
        return getBooleanIOValue("Crashed", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>Crashed</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'Crashed'
     */
    public void setCrashed(java.lang.Boolean value)
    {
        setDigitalOutput("Crashed", value);
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
