package com.kuka.generated.ioAccess;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>VisionOutputs</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class VisionOutputsIOGroup extends AbstractIOGroup
{
	/**
	 * Constructor to create an instance of class 'VisionOutputs'.<br>
	 * <i>This constructor is automatically generated. Please, do not modify!</i>
	 *
	 * @param controller
	 *            the controller, which has access to the I/O group 'VisionOutputs'
	 */
	@Inject
	public VisionOutputsIOGroup(Controller controller)
	{
		super(controller, "VisionOutputs");

		addDigitalOutput("DataRequestSent", IOTypes.BOOLEAN, 1);
		addDigitalOutput("PickPositionReady", IOTypes.BOOLEAN, 1);
		addDigitalOutput("CalibrationComplete", IOTypes.BOOLEAN, 1);
	}

	/**
	 * Gets the value of the <b>digital output '<i>DataRequestSent</i>'</b>.<br>
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
	 * @return current value of the digital output 'DataRequestSent'
	 */
	public boolean getDataRequestSent()
	{
		return getBooleanIOValue("DataRequestSent", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>DataRequestSent</i>'</b>.<br>
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
	 * @param value
	 *            the value, which has to be written to the digital output 'DataRequestSent'
	 */
	public void setDataRequestSent(java.lang.Boolean value)
	{
		setDigitalOutput("DataRequestSent", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>PickPositionReady</i>'</b>.<br>
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
	 * @return current value of the digital output 'PickPositionReady'
	 */
	public boolean getPickPositionReady()
	{
		return getBooleanIOValue("PickPositionReady", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>PickPositionReady</i>'</b>.<br>
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
	 * @param value
	 *            the value, which has to be written to the digital output 'PickPositionReady'
	 */
	public void setPickPositionReady(java.lang.Boolean value)
	{
		setDigitalOutput("PickPositionReady", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>CalibrationComplete</i>'</b>.<br>
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
	 * @return current value of the digital output 'CalibrationComplete'
	 */
	public boolean getCalibrationComplete()
	{
		return getBooleanIOValue("CalibrationComplete", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>CalibrationComplete</i>'</b>.<br>
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
	 * @param value
	 *            the value, which has to be written to the digital output 'CalibrationComplete'
	 */
	public void setCalibrationComplete(java.lang.Boolean value)
	{
		setDigitalOutput("CalibrationComplete", value);
	}

}
