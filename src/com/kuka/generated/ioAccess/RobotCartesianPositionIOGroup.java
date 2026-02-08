package com.kuka.generated.ioAccess;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>RobotCartesianPosition</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton public class RobotCartesianPositionIOGroup extends AbstractIOGroup
{
    /**
     * Constructor to create an instance of class 'RobotCartesianPosition'.<br>
     * <i>This constructor is automatically generated. Please, do not modify!</i>
     *
     * @param controller the controller, which has access to the I/O group 'RobotCartesianPosition'
     */
    @Inject public RobotCartesianPositionIOGroup(Controller controller)
    {
        super(controller, "RobotCartesianPosition");

        addDigitalOutput("X", IOTypes.INTEGER, 16);
        addDigitalOutput("Y", IOTypes.INTEGER, 16);
        addDigitalOutput("Z", IOTypes.INTEGER, 16);
        addDigitalOutput("A", IOTypes.INTEGER, 16);
        addDigitalOutput("B", IOTypes.INTEGER, 16);
        addDigitalOutput("C", IOTypes.INTEGER, 16);
    }

    /**
     * Gets the value of the <b>digital output '<i>X</i>'</b>.<br>
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
     * @return current value of the digital output 'X'
     */
    public java.lang.Integer getX()
    {
        return getNumberIOValue("X", true).intValue();
    }

    /**
     * Sets the value of the <b>digital output '<i>X</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'X'
     */
    public void setX(java.lang.Integer value)
    {
        setDigitalOutput("X", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>Y</i>'</b>.<br>
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
     * @return current value of the digital output 'Y'
     */
    public java.lang.Integer getY()
    {
        return getNumberIOValue("Y", true).intValue();
    }

    /**
     * Sets the value of the <b>digital output '<i>Y</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'Y'
     */
    public void setY(java.lang.Integer value)
    {
        setDigitalOutput("Y", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>Z</i>'</b>.<br>
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
     * @return current value of the digital output 'Z'
     */
    public java.lang.Integer getZ()
    {
        return getNumberIOValue("Z", true).intValue();
    }

    /**
     * Sets the value of the <b>digital output '<i>Z</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'Z'
     */
    public void setZ(java.lang.Integer value)
    {
        setDigitalOutput("Z", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>A</i>'</b>.<br>
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
     * @return current value of the digital output 'A'
     */
    public java.lang.Integer getA()
    {
        return getNumberIOValue("A", true).intValue();
    }

    /**
     * Sets the value of the <b>digital output '<i>A</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'A'
     */
    public void setA(java.lang.Integer value)
    {
        setDigitalOutput("A", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>B</i>'</b>.<br>
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
     * @return current value of the digital output 'B'
     */
    public java.lang.Integer getB()
    {
        return getNumberIOValue("B", true).intValue();
    }

    /**
     * Sets the value of the <b>digital output '<i>B</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'B'
     */
    public void setB(java.lang.Integer value)
    {
        setDigitalOutput("B", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>C</i>'</b>.<br>
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
     * @return current value of the digital output 'C'
     */
    public java.lang.Integer getC()
    {
        return getNumberIOValue("C", true).intValue();
    }

    /**
     * Sets the value of the <b>digital output '<i>C</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'C'
     */
    public void setC(java.lang.Integer value)
    {
        setDigitalOutput("C", value);
    }

}
