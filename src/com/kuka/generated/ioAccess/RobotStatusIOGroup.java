package com.kuka.generated.ioAccess;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>RobotStatus</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class RobotStatusIOGroup extends AbstractIOGroup
{
    /**
     * Constructor to create an instance of class 'RobotStatus'.<br>
     * <i>This constructor is automatically generated. Please, do not modify!</i>
     *
     * @param controller the controller, which has access to the I/O group 'RobotStatus'
     */
    @Inject
    public RobotStatusIOGroup(Controller controller)
    {
        super(controller, "RobotStatus");

        addDigitalOutput("Moving", IOTypes.BOOLEAN, 1);
        addDigitalOutput("Homed", IOTypes.BOOLEAN, 1);
        addDigitalOutput("Stopped", IOTypes.BOOLEAN, 1);
        addDigitalOutput("T1", IOTypes.BOOLEAN, 1);
        addDigitalOutput("Aut", IOTypes.BOOLEAN, 1);
        addDigitalOutput("ZRes1", IOTypes.BOOLEAN, 1);
        addDigitalOutput("ZRes2", IOTypes.BOOLEAN, 1);
        addDigitalOutput("ZRes3", IOTypes.BOOLEAN, 1);
    }

    /**
     * Gets the value of the <b>digital output '<i>Moving</i>'</b>.<br>
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
     * @return current value of the digital output 'Moving'
     */
    public boolean getMoving()
    {
        return getBooleanIOValue("Moving", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>Moving</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'Moving'
     */
    public void setMoving(java.lang.Boolean value)
    {
        setDigitalOutput("Moving", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>Homed</i>'</b>.<br>
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
     * @return current value of the digital output 'Homed'
     */
    public boolean getHomed()
    {
        return getBooleanIOValue("Homed", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>Homed</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'Homed'
     */
    public void setHomed(java.lang.Boolean value)
    {
        setDigitalOutput("Homed", value);
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
     * Gets the value of the <b>digital output '<i>T1</i>'</b>.<br>
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
     * @return current value of the digital output 'T1'
     */
    public boolean getT1()
    {
        return getBooleanIOValue("T1", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>T1</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'T1'
     */
    public void setT1(java.lang.Boolean value)
    {
        setDigitalOutput("T1", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>Aut</i>'</b>.<br>
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
     * @return current value of the digital output 'Aut'
     */
    public boolean getAut()
    {
        return getBooleanIOValue("Aut", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>Aut</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'Aut'
     */
    public void setAut(java.lang.Boolean value)
    {
        setDigitalOutput("Aut", value);
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

}
