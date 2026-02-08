package com.kuka.generated.ioAccess;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>RobotJointPosition</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class RobotJointPositionIOGroup extends AbstractIOGroup
{
    /**
     * Constructor to create an instance of class 'RobotJointPosition'.<br>
     * <i>This constructor is automatically generated. Please, do not modify!</i>
     *
     * @param controller the controller, which has access to the I/O group 'RobotJointPosition'
     */
    @Inject
    public RobotJointPositionIOGroup(Controller controller)
    {
        super(controller, "RobotJointPosition");

        addDigitalOutput("A1", IOTypes.INTEGER, 16);
        addDigitalOutput("A2", IOTypes.INTEGER, 16);
        addDigitalOutput("A3", IOTypes.INTEGER, 16);
        addDigitalOutput("A4", IOTypes.INTEGER, 16);
        addDigitalOutput("A5", IOTypes.INTEGER, 16);
        addDigitalOutput("A6", IOTypes.INTEGER, 16);
        addDigitalOutput("A7", IOTypes.INTEGER, 16);
    }

    /**
     * Gets the value of the <b>digital output '<i>A1</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [-32768; 32767]
     *
     * @return current value of the digital output 'A1'
     */
    public java.lang.Integer getA1()
    {
        return getNumberIOValue("A1", true).intValue();
    }

    /**
     * Sets the value of the <b>digital output '<i>A1</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [-32768; 32767]
     *
     * @param value the value, which has to be written to the digital output 'A1'
     */
    public void setA1(java.lang.Integer value)
    {
        setDigitalOutput("A1", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>A2</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [-32768; 32767]
     *
     * @return current value of the digital output 'A2'
     */
    public java.lang.Integer getA2()
    {
        return getNumberIOValue("A2", true).intValue();
    }

    /**
     * Sets the value of the <b>digital output '<i>A2</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [-32768; 32767]
     *
     * @param value the value, which has to be written to the digital output 'A2'
     */
    public void setA2(java.lang.Integer value)
    {
        setDigitalOutput("A2", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>A3</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [-32768; 32767]
     *
     * @return current value of the digital output 'A3'
     */
    public java.lang.Integer getA3()
    {
        return getNumberIOValue("A3", true).intValue();
    }

    /**
     * Sets the value of the <b>digital output '<i>A3</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [-32768; 32767]
     *
     * @param value the value, which has to be written to the digital output 'A3'
     */
    public void setA3(java.lang.Integer value)
    {
        setDigitalOutput("A3", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>A4</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [-32768; 32767]
     *
     * @return current value of the digital output 'A4'
     */
    public java.lang.Integer getA4()
    {
        return getNumberIOValue("A4", true).intValue();
    }

    /**
     * Sets the value of the <b>digital output '<i>A4</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [-32768; 32767]
     *
     * @param value the value, which has to be written to the digital output 'A4'
     */
    public void setA4(java.lang.Integer value)
    {
        setDigitalOutput("A4", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>A5</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [-32768; 32767]
     *
     * @return current value of the digital output 'A5'
     */
    public java.lang.Integer getA5()
    {
        return getNumberIOValue("A5", true).intValue();
    }

    /**
     * Sets the value of the <b>digital output '<i>A5</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [-32768; 32767]
     *
     * @param value the value, which has to be written to the digital output 'A5'
     */
    public void setA5(java.lang.Integer value)
    {
        setDigitalOutput("A5", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>A6</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [-32768; 32767]
     *
     * @return current value of the digital output 'A6'
     */
    public java.lang.Integer getA6()
    {
        return getNumberIOValue("A6", true).intValue();
    }

    /**
     * Sets the value of the <b>digital output '<i>A6</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [-32768; 32767]
     *
     * @param value the value, which has to be written to the digital output 'A6'
     */
    public void setA6(java.lang.Integer value)
    {
        setDigitalOutput("A6", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>A7</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [-32768; 32767]
     *
     * @return current value of the digital output 'A7'
     */
    public java.lang.Integer getA7()
    {
        return getNumberIOValue("A7", true).intValue();
    }

    /**
     * Sets the value of the <b>digital output '<i>A7</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital output
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [-32768; 32767]
     *
     * @param value the value, which has to be written to the digital output 'A7'
     */
    public void setA7(java.lang.Integer value)
    {
        setDigitalOutput("A7", value);
    }

}
