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

		addInput("RunMode", IOTypes.BOOLEAN, 1);
		addInput("CalibrationMode", IOTypes.BOOLEAN, 1);
		addInput("DataRequest", IOTypes.BOOLEAN, 1);
		addInput("CalibrationRequest", IOTypes.BOOLEAN, 1);
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
	 */
	public boolean getRunMode()
	{
		return getBooleanIOValue("RunMode", false);
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
	 */
	public boolean getCalibrationMode()
	{
		return getBooleanIOValue("CalibrationMode", false);
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
	 */
	public boolean getDataRequest()
	{
		return getBooleanIOValue("DataRequest", false);
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
	 */
	public boolean getCalibrationRequest()
	{
		return getBooleanIOValue("CalibrationRequest", false);
	}

}
