package com.kuka.generated.ioAccess;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;
import com.kuka.roboticsAPI.ioModel.OutputReservedException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>AutExt</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton public class AutExtIOGroup extends AbstractIOGroup
{
    /**
     * Constructor to create an instance of class 'AutExt'.<br>
     * <i>This constructor is automatically generated. Please, do not modify!</i>
     *
     * @param controller the controller, which has access to the I/O group 'AutExt'
     */
    @Inject public AutExtIOGroup(Controller controller)
    {
        super(controller, "AutExt");

        addInput("ExtStart", IOTypes.BOOLEAN, 1);
        addInput("MoveEnable", IOTypes.BOOLEAN, 1);
        addMockedDigitalOutput("AutExtActive", IOTypes.BOOLEAN, 1);
        addMockedDigitalOutput("AutExtReady", IOTypes.BOOLEAN, 1);
        addMockedDigitalOutput("DefaultAppError", IOTypes.BOOLEAN, 1);
        addMockedDigitalOutput("StationError", IOTypes.BOOLEAN, 1);
    }

    /**
     * Gets the value of the <b>digital input '<i>ExtStart</i>'</b>.<br>
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
     * @return current value of the digital input 'ExtStart'
     */
    public boolean getExtStart()
    {
        return getBooleanIOValue("ExtStart", false);
    }

    /**
     * Gets the value of the <b>digital input '<i>MoveEnable</i>'</b>.<br>
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
     * @return current value of the digital input 'MoveEnable'
     */
    public boolean getMoveEnable()
    {
        return getBooleanIOValue("MoveEnable", false);
    }

    /**
     * Gets the value of the <b>digital output '<i>AutExtActive</i>'</b>.<br>
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
     * @return current value of the digital output 'AutExtActive'
     * @deprecated The output 'AutExtActive' is currently used as station state output in the Sunrise project properties.
     */
    @Deprecated public boolean getAutExtActive()
    {
        return getBooleanIOValue("AutExtActive", true);
    }

    /**
     * Always throws an {@code OutputReservedException}, because the <b>digital output '<i>AutExtActive</i>'</b> is currently used as station state output in the Sunrise project properties.
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
     * @param value the value, which has to be written to the digital output 'AutExtActive'
     * @throws OutputReservedException Always thrown, because this output is currently used as station state output in the Sunrise project properties.
     * @deprecated The output 'AutExtActive' is currently used as station state output in the Sunrise project properties.
     */
    @Deprecated public void setAutExtActive(java.lang.Boolean value) throws OutputReservedException
    {
        throw new OutputReservedException("The output 'AutExtActive' must not be set because it is currently used as station state output in the Sunrise project properties.");
    }

    /**
     * Gets the value of the <b>digital output '<i>AutExtReady</i>'</b>.<br>
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
     * @return current value of the digital output 'AutExtReady'
     * @deprecated The output 'AutExtReady' is currently used as station state output in the Sunrise project properties.
     */
    @Deprecated public boolean getAutExtReady()
    {
        return getBooleanIOValue("AutExtReady", true);
    }

    /**
     * Always throws an {@code OutputReservedException}, because the <b>digital output '<i>AutExtReady</i>'</b> is currently used as station state output in the Sunrise project properties.
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
     * @param value the value, which has to be written to the digital output 'AutExtReady'
     * @throws OutputReservedException Always thrown, because this output is currently used as station state output in the Sunrise project properties.
     * @deprecated The output 'AutExtReady' is currently used as station state output in the Sunrise project properties.
     */
    @Deprecated public void setAutExtReady(java.lang.Boolean value) throws OutputReservedException
    {
        throw new OutputReservedException("The output 'AutExtReady' must not be set because it is currently used as station state output in the Sunrise project properties.");
    }

    /**
     * Gets the value of the <b>digital output '<i>DefaultAppError</i>'</b>.<br>
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
     * @return current value of the digital output 'DefaultAppError'
     * @deprecated The output 'DefaultAppError' is currently used as station state output in the Sunrise project properties.
     */
    @Deprecated public boolean getDefaultAppError()
    {
        return getBooleanIOValue("DefaultAppError", true);
    }

    /**
     * Always throws an {@code OutputReservedException}, because the <b>digital output '<i>DefaultAppError</i>'</b> is currently used as station state output in the Sunrise project properties.
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
     * @param value the value, which has to be written to the digital output 'DefaultAppError'
     * @throws OutputReservedException Always thrown, because this output is currently used as station state output in the Sunrise project properties.
     * @deprecated The output 'DefaultAppError' is currently used as station state output in the Sunrise project properties.
     */
    @Deprecated public void setDefaultAppError(java.lang.Boolean value) throws OutputReservedException
    {
        throw new OutputReservedException("The output 'DefaultAppError' must not be set because it is currently used as station state output in the Sunrise project properties.");
    }

    /**
     * Gets the value of the <b>digital output '<i>StationError</i>'</b>.<br>
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
     * @return current value of the digital output 'StationError'
     * @deprecated The output 'StationError' is currently used as station state output in the Sunrise project properties.
     */
    @Deprecated public boolean getStationError()
    {
        return getBooleanIOValue("StationError", true);
    }

    /**
     * Always throws an {@code OutputReservedException}, because the <b>digital output '<i>StationError</i>'</b> is currently used as station state output in the Sunrise project properties.
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
     * @param value the value, which has to be written to the digital output 'StationError'
     * @throws OutputReservedException Always thrown, because this output is currently used as station state output in the Sunrise project properties.
     * @deprecated The output 'StationError' is currently used as station state output in the Sunrise project properties.
     */
    @Deprecated public void setStationError(java.lang.Boolean value) throws OutputReservedException
    {
        throw new OutputReservedException("The output 'StationError' must not be set because it is currently used as station state output in the Sunrise project properties.");
    }

}
