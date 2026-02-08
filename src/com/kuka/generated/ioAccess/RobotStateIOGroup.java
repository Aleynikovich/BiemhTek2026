package com.kuka.generated.ioAccess;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>RobotState</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class RobotStateIOGroup extends AbstractIOGroup
{
    /**
     * Constructor to create an instance of class 'RobotState'.<br>
     * <i>This constructor is automatically generated. Please, do not modify!</i>
     *
     * @param controller the controller, which has access to the I/O group 'RobotState'
     */
    @Inject
    public RobotStateIOGroup(Controller controller)
    {
        super(controller, "RobotState");

        addDigitalOutput("IsInHome", IOTypes.BOOLEAN, 1);
        addDigitalOutput("IsMastered", IOTypes.BOOLEAN, 1);
        addDigitalOutput("IsReadyToMove", IOTypes.BOOLEAN, 1);
        addDigitalOutput("HasActiveMotion", IOTypes.BOOLEAN, 1);
        addDigitalOutput("IsReferenced", IOTypes.BOOLEAN, 1);
        addDigitalOutput("IsGMSReferenced", IOTypes.BOOLEAN, 1);
    }

    /**
     * Gets the value of the <b>digital output '<i>IsInHome</i>'</b>.<br>
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
     * @return current value of the digital output 'IsInHome'
     */
    public boolean getIsInHome()
    {
        return getBooleanIOValue("IsInHome", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>IsInHome</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'IsInHome'
     */
    public void setIsInHome(java.lang.Boolean value)
    {
        setDigitalOutput("IsInHome", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>IsMastered</i>'</b>.<br>
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
     * @return current value of the digital output 'IsMastered'
     */
    public boolean getIsMastered()
    {
        return getBooleanIOValue("IsMastered", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>IsMastered</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'IsMastered'
     */
    public void setIsMastered(java.lang.Boolean value)
    {
        setDigitalOutput("IsMastered", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>IsReadyToMove</i>'</b>.<br>
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
     * @return current value of the digital output 'IsReadyToMove'
     */
    public boolean getIsReadyToMove()
    {
        return getBooleanIOValue("IsReadyToMove", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>IsReadyToMove</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'IsReadyToMove'
     */
    public void setIsReadyToMove(java.lang.Boolean value)
    {
        setDigitalOutput("IsReadyToMove", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>HasActiveMotion</i>'</b>.<br>
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
     * @return current value of the digital output 'HasActiveMotion'
     */
    public boolean getHasActiveMotion()
    {
        return getBooleanIOValue("HasActiveMotion", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>HasActiveMotion</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'HasActiveMotion'
     */
    public void setHasActiveMotion(java.lang.Boolean value)
    {
        setDigitalOutput("HasActiveMotion", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>IsReferenced</i>'</b>.<br>
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
     * @return current value of the digital output 'IsReferenced'
     */
    public boolean getIsReferenced()
    {
        return getBooleanIOValue("IsReferenced", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>IsReferenced</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'IsReferenced'
     */
    public void setIsReferenced(java.lang.Boolean value)
    {
        setDigitalOutput("IsReferenced", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>IsGMSReferenced</i>'</b>.<br>
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
     * @return current value of the digital output 'IsGMSReferenced'
     */
    public boolean getIsGMSReferenced()
    {
        return getBooleanIOValue("IsGMSReferenced", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>IsGMSReferenced</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'IsGMSReferenced'
     */
    public void setIsGMSReferenced(java.lang.Boolean value)
    {
        setDigitalOutput("IsGMSReferenced", value);
    }

}
