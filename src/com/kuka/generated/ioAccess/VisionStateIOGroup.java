package com.kuka.generated.ioAccess;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>VisionState</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton public class VisionStateIOGroup extends AbstractIOGroup
{
    /**
     * Constructor to create an instance of class 'VisionState'.<br>
     * <i>This constructor is automatically generated. Please, do not modify!</i>
     *
     * @param controller the controller, which has access to the I/O group 'VisionState'
     */
    @Inject public VisionStateIOGroup(Controller controller)
    {
        super(controller, "VisionState");

        addDigitalOutput("VisionServerOnline", IOTypes.BOOLEAN, 1);
        addDigitalOutput("ReferencesLoaded", IOTypes.BOOLEAN, 1);
        addDigitalOutput("NewWorkpieceFound", IOTypes.BOOLEAN, 1);
        addDigitalOutput("VisionServerBusy", IOTypes.BOOLEAN, 1);
        addDigitalOutput("CameraModeRun", IOTypes.BOOLEAN, 1);
        addDigitalOutput("CameraModeCalibration", IOTypes.BOOLEAN, 1);
    }

    /**
     * Gets the value of the <b>digital output '<i>VisionServerOnline</i>'</b>.<br>
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
     * @return current value of the digital output 'VisionServerOnline'
     */
    public boolean getVisionServerOnline()
    {
        return getBooleanIOValue("VisionServerOnline", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>VisionServerOnline</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'VisionServerOnline'
     */
    public void setVisionServerOnline(java.lang.Boolean value)
    {
        setDigitalOutput("VisionServerOnline", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>ReferencesLoaded</i>'</b>.<br>
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
     * @return current value of the digital output 'ReferencesLoaded'
     */
    public boolean getReferencesLoaded()
    {
        return getBooleanIOValue("ReferencesLoaded", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>ReferencesLoaded</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'ReferencesLoaded'
     */
    public void setReferencesLoaded(java.lang.Boolean value)
    {
        setDigitalOutput("ReferencesLoaded", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>NewWorkpieceFound</i>'</b>.<br>
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
     * @return current value of the digital output 'NewWorkpieceFound'
     */
    public boolean getNewWorkpieceFound()
    {
        return getBooleanIOValue("NewWorkpieceFound", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>NewWorkpieceFound</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'NewWorkpieceFound'
     */
    public void setNewWorkpieceFound(java.lang.Boolean value)
    {
        setDigitalOutput("NewWorkpieceFound", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>VisionServerBusy</i>'</b>.<br>
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
     * @return current value of the digital output 'VisionServerBusy'
     */
    public boolean getVisionServerBusy()
    {
        return getBooleanIOValue("VisionServerBusy", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>VisionServerBusy</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'VisionServerBusy'
     */
    public void setVisionServerBusy(java.lang.Boolean value)
    {
        setDigitalOutput("VisionServerBusy", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>CameraModeRun</i>'</b>.<br>
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
     * @return current value of the digital output 'CameraModeRun'
     */
    public boolean getCameraModeRun()
    {
        return getBooleanIOValue("CameraModeRun", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>CameraModeRun</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'CameraModeRun'
     */
    public void setCameraModeRun(java.lang.Boolean value)
    {
        setDigitalOutput("CameraModeRun", value);
    }

    /**
     * Gets the value of the <b>digital output '<i>CameraModeCalibration</i>'</b>.<br>
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
     * @return current value of the digital output 'CameraModeCalibration'
     */
    public boolean getCameraModeCalibration()
    {
        return getBooleanIOValue("CameraModeCalibration", true);
    }

    /**
     * Sets the value of the <b>digital output '<i>CameraModeCalibration</i>'</b>.<br>
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
     * @param value the value, which has to be written to the digital output 'CameraModeCalibration'
     */
    public void setCameraModeCalibration(java.lang.Boolean value)
    {
        setDigitalOutput("CameraModeCalibration", value);
    }

}
