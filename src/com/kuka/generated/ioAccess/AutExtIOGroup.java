package com.kuka.generated.ioAccess;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>AutExt</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class AutExtIOGroup extends AbstractIOGroup
{
	/**
	 * Constructor to create an instance of class 'AutExt'.<br>
	 * <i>This constructor is automatically generated. Please, do not modify!</i>
	 *
	 * @param controller
	 *            the controller, which has access to the I/O group 'AutExt'
	 */
	@Inject
	public AutExtIOGroup(Controller controller)
	{
		super(controller, "AutExt");

		addInput("ExtStart", IOTypes.BOOLEAN, 1);
		addInput("MoveEnable", IOTypes.BOOLEAN, 1);
		addDigitalOutput("AutExtActive", IOTypes.BOOLEAN, 1);
		addDigitalOutput("AutExtReady", IOTypes.BOOLEAN, 1);
		addDigitalOutput("DefaultAppError", IOTypes.BOOLEAN, 1);
		addDigitalOutput("StationError", IOTypes.BOOLEAN, 1);
		addDigitalOutput("ProgramNumberRequest", IOTypes.BOOLEAN, 1);
		addInput("ProgramNumberIN", IOTypes.UNSIGNED_INTEGER, 8);
		addDigitalOutput("CurrentProgramNumber", IOTypes.UNSIGNED_INTEGER, 8);
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
	 */
	public boolean getAutExtActive()
	{
		return getBooleanIOValue("AutExtActive", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>AutExtActive</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'AutExtActive'
	 */
	public void setAutExtActive(java.lang.Boolean value)
	{
		setDigitalOutput("AutExtActive", value);
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
	 */
	public boolean getAutExtReady()
	{
		return getBooleanIOValue("AutExtReady", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>AutExtReady</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'AutExtReady'
	 */
	public void setAutExtReady(java.lang.Boolean value)
	{
		setDigitalOutput("AutExtReady", value);
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
	 */
	public boolean getDefaultAppError()
	{
		return getBooleanIOValue("DefaultAppError", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>DefaultAppError</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'DefaultAppError'
	 */
	public void setDefaultAppError(java.lang.Boolean value)
	{
		setDigitalOutput("DefaultAppError", value);
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
	 */
	public boolean getStationError()
	{
		return getBooleanIOValue("StationError", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>StationError</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'StationError'
	 */
	public void setStationError(java.lang.Boolean value)
	{
		setDigitalOutput("StationError", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>ProgramNumberRequest</i>'</b>.<br>
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
	 * @return current value of the digital output 'ProgramNumberRequest'
	 */
	public boolean getProgramNumberRequest()
	{
		return getBooleanIOValue("ProgramNumberRequest", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>ProgramNumberRequest</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'ProgramNumberRequest'
	 */
	public void setProgramNumberRequest(java.lang.Boolean value)
	{
		setDigitalOutput("ProgramNumberRequest", value);
	}

	/**
	 * Gets the value of the <b>digital input '<i>ProgramNumberIN</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital input
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [0; 255]
	 *
	 * @return current value of the digital input 'ProgramNumberIN'
	 */
	public java.lang.Integer getProgramNumberIN()
	{
		return getNumberIOValue("ProgramNumberIN", false).intValue();
	}

	/**
	 * Gets the value of the <b>digital output '<i>CurrentProgramNumber</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [0; 255]
	 *
	 * @return current value of the digital output 'CurrentProgramNumber'
	 */
	public java.lang.Integer getCurrentProgramNumber()
	{
		return getNumberIOValue("CurrentProgramNumber", true).intValue();
	}

	/**
	 * Sets the value of the <b>digital output '<i>CurrentProgramNumber</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [0; 255]
	 *
	 * @param value
	 *            the value, which has to be written to the digital output 'CurrentProgramNumber'
	 */
	public void setCurrentProgramNumber(java.lang.Integer value)
	{
		setDigitalOutput("CurrentProgramNumber", value);
	}

}
