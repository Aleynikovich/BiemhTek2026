package com.kuka.generated.ioAccess;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>AutomaticExchange</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class AutomaticExchangeIOGroup extends AbstractIOGroup
{
    /**
     * Constructor to create an instance of class 'AutomaticExchange'.<br>
     * <i>This constructor is automatically generated. Please, do not modify!</i>
     *
     * @param controller the controller, which has access to the I/O group 'AutomaticExchange'
     */
    @Inject
    public AutomaticExchangeIOGroup(Controller controller)
    {
        super(controller, "AutomaticExchange");

        addInput("ExternalStart", IOTypes.BOOLEAN, 1);
        addInput("ProgramNumber", IOTypes.UNSIGNED_INTEGER, 16);
        addInput("ExternalStop", IOTypes.BOOLEAN, 1);
        addInput("ExternalReset", IOTypes.BOOLEAN, 1);
        addDigitalOutput("RobotMoving", IOTypes.BOOLEAN, 1);
        addDigitalOutput("RobotHomed", IOTypes.BOOLEAN, 1);
        addDigitalOutput("RobotStopped", IOTypes.BOOLEAN, 1);
        addDigitalOutput("RobotProgramRunning", IOTypes.BOOLEAN, 1);
        addDigitalOutput("RobotT1", IOTypes.BOOLEAN, 1);
        addDigitalOutput("RobotAut", IOTypes.BOOLEAN, 1);
        addDigitalOutput("RobotProgramCompleted", IOTypes.BOOLEAN, 1);
    }

    /**
     * Gets the value of the <b>digital input '<i>ExternalStart</i>'</b>.<br>
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
     * @return current value of the digital input 'ExternalStart'
     */
    public boolean getExternalStart()
    {
        return getBooleanIOValue("ExternalStart", false);
    }

    /**
     * Gets the value of the <b>digital input '<i>ProgramNumber</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital input
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [0; 65535]
     *
     * @return current value of the digital input 'ProgramNumber'
     */
    public java.lang.Integer getProgramNumber()
    {
        return getNumberIOValue("ProgramNumber", false).intValue();
    }

    /**
     * Gets the value of the <b>digital input '<i>ExternalStop</i>'</b>.<br>
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
     * @return current value of the digital input 'ExternalStop'
     */
    public boolean getExternalStop()
    {
        return getBooleanIOValue("ExternalStop", false);
    }

    /**
     * Gets the value of the <b>digital input '<i>ExternalReset</i>'</b>.<br>
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
     * @return current value of the digital input 'ExternalReset'
     */
    public boolean getExternalReset()
    {
        return getBooleanIOValue("ExternalReset", false);
    }

    /**
     * Gets the value of the <b>digital output '<i>RobotMoving</i>'</b>.<br>
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
     * @return current value of the digital output 'RobotMoving'
     */
    public boolean getRobotMoving()
    {
        return getBooleanIOValue("RobotMoving", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>RobotMoving</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'RobotMoving'
     */
    public void setRobotMoving(java.lang.Boolean value)
    {
        setDigitalOutput("RobotMoving", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>RobotHomed</i>'</b>.<br>
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
     * @return current value of the digital output 'RobotHomed'
     */
    public boolean getRobotHomed()
    {
        return getBooleanIOValue("RobotHomed", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>RobotHomed</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'RobotHomed'
     */
    public void setRobotHomed(java.lang.Boolean value)
    {
        setDigitalOutput("RobotHomed", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>RobotStopped</i>'</b>.<br>
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
     * @return current value of the digital output 'RobotStopped'
     */
    public boolean getRobotStopped()
    {
        return getBooleanIOValue("RobotStopped", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>RobotStopped</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'RobotStopped'
     */
    public void setRobotStopped(java.lang.Boolean value)
    {
        setDigitalOutput("RobotStopped", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>RobotProgramRunning</i>'</b>.<br>
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
     * @return current value of the digital output 'RobotProgramRunning'
     */
    public boolean getRobotProgramRunning()
    {
        return getBooleanIOValue("RobotProgramRunning", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>RobotProgramRunning</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'RobotProgramRunning'
     */
    public void setRobotProgramRunning(java.lang.Boolean value)
    {
        setDigitalOutput("RobotProgramRunning", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>RobotT1</i>'</b>.<br>
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
     * @return current value of the digital output 'RobotT1'
     */
    public boolean getRobotT1()
    {
        return getBooleanIOValue("RobotT1", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>RobotT1</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'RobotT1'
     */
    public void setRobotT1(java.lang.Boolean value)
    {
        setDigitalOutput("RobotT1", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>RobotAut</i>'</b>.<br>
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
     * @return current value of the digital output 'RobotAut'
     */
    public boolean getRobotAut()
    {
        return getBooleanIOValue("RobotAut", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>RobotAut</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'RobotAut'
     */
    public void setRobotAut(java.lang.Boolean value)
    {
        setDigitalOutput("RobotAut", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>RobotProgramCompleted</i>'</b>.<br>
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
     * @return current value of the digital output 'RobotProgramCompleted'
     */
    public boolean getRobotProgramCompleted()
    {
        return getBooleanIOValue("RobotProgramCompleted", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>RobotProgramCompleted</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'RobotProgramCompleted'
     */
    public void setRobotProgramCompleted(java.lang.Boolean value)
    {
        setDigitalOutput("RobotProgramCompleted", value);
    }

}
