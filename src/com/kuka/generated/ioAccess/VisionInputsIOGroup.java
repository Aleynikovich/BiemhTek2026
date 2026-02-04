package com.kuka.generated.ioAccess;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>VisionInputs</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class VisionInputsIOGroup extends AbstractIOGroup
{
	/**
	 * Constructor to create an instance of class 'VisionInputs'.<br>
	 * <i>This constructor is automatically generated. Please, do not modify!</i>
	 *
	 * @param controller
	 *            the controller, which has access to the I/O group 'VisionInputs'
	 */
	@Inject
	public VisionInputsIOGroup(Controller controller)
	{
		super(controller, "VisionInputs");

		addMockedInput("RunMode", IOTypes.BOOLEAN, 1, false);
		addMockedInput("CalibrationMode", IOTypes.BOOLEAN, 1, false);
		addMockedInput("DataRequest", IOTypes.BOOLEAN, 1, false);
		addMockedInput("CalibrationRequest", IOTypes.BOOLEAN, 1, false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>RunMode</i>'</b>.<br>
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
	 * @return current value of the digital input 'RunMode'
	* 
	 * @deprecated The output 'RunMode' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
	 */
	@Deprecated
	public boolean getRunMode()
	{
		return getBooleanIOValue("RunMode", false);
	}

	/**
	 * Sets the value of the <b>mocked digital input '<i>RunMode</i>'</b>.<br>
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
	 * @param value
	 *            the value, which has to be written to the mocked digital input 'RunMode'
	* 
	 * @deprecated The output 'RunMode' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
	 */
	@Deprecated
	public void setMockedRunModeValue(java.lang.Boolean value)
	{
		setMockedInput("RunMode", value);
	}

	/**
	 * Gets the value of the <b>digital input '<i>CalibrationMode</i>'</b>.<br>
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
	 * @return current value of the digital input 'CalibrationMode'
	* 
	 * @deprecated The output 'CalibrationMode' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
	 */
	@Deprecated
	public boolean getCalibrationMode()
	{
		return getBooleanIOValue("CalibrationMode", false);
	}

	/**
	 * Sets the value of the <b>mocked digital input '<i>CalibrationMode</i>'</b>.<br>
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
	 * @param value
	 *            the value, which has to be written to the mocked digital input 'CalibrationMode'
	* 
	 * @deprecated The output 'CalibrationMode' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
	 */
	@Deprecated
	public void setMockedCalibrationModeValue(java.lang.Boolean value)
	{
		setMockedInput("CalibrationMode", value);
	}

	/**
	 * Gets the value of the <b>digital input '<i>DataRequest</i>'</b>.<br>
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
	 * @return current value of the digital input 'DataRequest'
	* 
	 * @deprecated The output 'DataRequest' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
	 */
	@Deprecated
	public boolean getDataRequest()
	{
		return getBooleanIOValue("DataRequest", false);
	}

	/**
	 * Sets the value of the <b>mocked digital input '<i>DataRequest</i>'</b>.<br>
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
	 * @param value
	 *            the value, which has to be written to the mocked digital input 'DataRequest'
	* 
	 * @deprecated The output 'DataRequest' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
	 */
	@Deprecated
	public void setMockedDataRequestValue(java.lang.Boolean value)
	{
		setMockedInput("DataRequest", value);
	}

	/**
	 * Gets the value of the <b>digital input '<i>CalibrationRequest</i>'</b>.<br>
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
	 * @return current value of the digital input 'CalibrationRequest'
	* 
	 * @deprecated The output 'CalibrationRequest' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
	 */
	@Deprecated
	public boolean getCalibrationRequest()
	{
		return getBooleanIOValue("CalibrationRequest", false);
	}

	/**
	 * Sets the value of the <b>mocked digital input '<i>CalibrationRequest</i>'</b>.<br>
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
	 * @param value
	 *            the value, which has to be written to the mocked digital input 'CalibrationRequest'
	* 
	 * @deprecated The output 'CalibrationRequest' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
	 */
	@Deprecated
	public void setMockedCalibrationRequestValue(java.lang.Boolean value)
	{
		setMockedInput("CalibrationRequest", value);
	}

}
