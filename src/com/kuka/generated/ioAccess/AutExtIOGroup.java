package com.kuka.generated.ioAccess;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;
import com.kuka.roboticsAPI.ioModel.OutputReservedException;

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
		addMockedDigitalOutput("AutExtActive", IOTypes.BOOLEAN, 1);
		addMockedDigitalOutput("AutExtReady", IOTypes.BOOLEAN, 1);
		addMockedDigitalOutput("DefaultAppError", IOTypes.BOOLEAN, 1);
		addMockedDigitalOutput("StationError", IOTypes.BOOLEAN, 1);
		addDigitalOutput("ProgramNumberRequest", IOTypes.BOOLEAN, 1);
		addInput("ProgramNumberIN", IOTypes.UNSIGNED_INTEGER, 8);
		addDigitalOutput("CurrentProgramNumber", IOTypes.UNSIGNED_INTEGER, 8);
		addDigitalOutput("Part_Loaded", IOTypes.BOOLEAN, 1);
		addDigitalOutput("Part_Unloaded", IOTypes.BOOLEAN, 1);
		addDigitalOutput("Robot_Out_Zeiss_Zone", IOTypes.BOOLEAN, 1);
		addInput("Zeiss_Ready", IOTypes.BOOLEAN, 1);
		addInput("Zeiss_Measure_Running", IOTypes.BOOLEAN, 1);
		addInput("Zeiss_Load_Resquest", IOTypes.BOOLEAN, 1);
		addInput("Zeiss_Unload_Request", IOTypes.BOOLEAN, 1);
		addDigitalOutput("Zeiss_Part_Type_Loaded", IOTypes.UNSIGNED_INTEGER, 1);
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
	* 
	 * @deprecated The output 'AutExtActive' is currently used as station state output in the Sunrise project properties.
	 */
	@Deprecated
	public boolean getAutExtActive()
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
	 * @param value
	 *            the value, which has to be written to the digital output 'AutExtActive'
	 * @throws OutputReservedException
	 *            Always thrown, because this output is currently used as station state output in the Sunrise project properties.
	* 
	 * @deprecated The output 'AutExtActive' is currently used as station state output in the Sunrise project properties.
	 */
	@Deprecated
	public void setAutExtActive(java.lang.Boolean value) throws OutputReservedException
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
	* 
	 * @deprecated The output 'AutExtReady' is currently used as station state output in the Sunrise project properties.
	 */
	@Deprecated
	public boolean getAutExtReady()
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
	 * @param value
	 *            the value, which has to be written to the digital output 'AutExtReady'
	 * @throws OutputReservedException
	 *            Always thrown, because this output is currently used as station state output in the Sunrise project properties.
	* 
	 * @deprecated The output 'AutExtReady' is currently used as station state output in the Sunrise project properties.
	 */
	@Deprecated
	public void setAutExtReady(java.lang.Boolean value) throws OutputReservedException
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
	* 
	 * @deprecated The output 'DefaultAppError' is currently used as station state output in the Sunrise project properties.
	 */
	@Deprecated
	public boolean getDefaultAppError()
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
	 * @param value
	 *            the value, which has to be written to the digital output 'DefaultAppError'
	 * @throws OutputReservedException
	 *            Always thrown, because this output is currently used as station state output in the Sunrise project properties.
	* 
	 * @deprecated The output 'DefaultAppError' is currently used as station state output in the Sunrise project properties.
	 */
	@Deprecated
	public void setDefaultAppError(java.lang.Boolean value) throws OutputReservedException
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
	* 
	 * @deprecated The output 'StationError' is currently used as station state output in the Sunrise project properties.
	 */
	@Deprecated
	public boolean getStationError()
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
	 * @param value
	 *            the value, which has to be written to the digital output 'StationError'
	 * @throws OutputReservedException
	 *            Always thrown, because this output is currently used as station state output in the Sunrise project properties.
	* 
	 * @deprecated The output 'StationError' is currently used as station state output in the Sunrise project properties.
	 */
	@Deprecated
	public void setStationError(java.lang.Boolean value) throws OutputReservedException
	{
		throw new OutputReservedException("The output 'StationError' must not be set because it is currently used as station state output in the Sunrise project properties.");
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

	/**
	 * Gets the value of the <b>digital output '<i>Part_Loaded</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * Part Loaded in Zeiss
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital output 'Part_Loaded'
	 */
	public boolean getPart_Loaded()
	{
		return getBooleanIOValue("Part_Loaded", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Part_Loaded</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * Part Loaded in Zeiss
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @param value
	 *            the value, which has to be written to the digital output 'Part_Loaded'
	 */
	public void setPart_Loaded(java.lang.Boolean value)
	{
		setDigitalOutput("Part_Loaded", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Part_Unloaded</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * Part_Unloaded_from_Zeiss
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital output 'Part_Unloaded'
	 */
	public boolean getPart_Unloaded()
	{
		return getBooleanIOValue("Part_Unloaded", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Part_Unloaded</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * Part_Unloaded_from_Zeiss
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @param value
	 *            the value, which has to be written to the digital output 'Part_Unloaded'
	 */
	public void setPart_Unloaded(java.lang.Boolean value)
	{
		setDigitalOutput("Part_Unloaded", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Robot_Out_Zeiss_Zone</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * Robot_Out_Zeiss_Zone
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital output 'Robot_Out_Zeiss_Zone'
	 */
	public boolean getRobot_Out_Zeiss_Zone()
	{
		return getBooleanIOValue("Robot_Out_Zeiss_Zone", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Robot_Out_Zeiss_Zone</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * Robot_Out_Zeiss_Zone
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @param value
	 *            the value, which has to be written to the digital output 'Robot_Out_Zeiss_Zone'
	 */
	public void setRobot_Out_Zeiss_Zone(java.lang.Boolean value)
	{
		setDigitalOutput("Robot_Out_Zeiss_Zone", value);
	}

	/**
	 * Gets the value of the <b>digital input '<i>Zeiss_Ready</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital input
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * Zeiss_Ready
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital input 'Zeiss_Ready'
	 */
	public boolean getZeiss_Ready()
	{
		return getBooleanIOValue("Zeiss_Ready", false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>Zeiss_Measure_Running</i>'</b>.<br>
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
	 * @return current value of the digital input 'Zeiss_Measure_Running'
	 */
	public boolean getZeiss_Measure_Running()
	{
		return getBooleanIOValue("Zeiss_Measure_Running", false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>Zeiss_Load_Resquest</i>'</b>.<br>
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
	 * @return current value of the digital input 'Zeiss_Load_Resquest'
	 */
	public boolean getZeiss_Load_Resquest()
	{
		return getBooleanIOValue("Zeiss_Load_Resquest", false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>Zeiss_Unload_Request</i>'</b>.<br>
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
	 * @return current value of the digital input 'Zeiss_Unload_Request'
	 */
	public boolean getZeiss_Unload_Request()
	{
		return getBooleanIOValue("Zeiss_Unload_Request", false);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Zeiss_Part_Type_Loaded</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [0; 1]
	 *
	 * @return current value of the digital output 'Zeiss_Part_Type_Loaded'
	 */
	public java.lang.Integer getZeiss_Part_Type_Loaded()
	{
		return getNumberIOValue("Zeiss_Part_Type_Loaded", true).intValue();
	}

	/**
	 * Sets the value of the <b>digital output '<i>Zeiss_Part_Type_Loaded</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [0; 1]
	 *
	 * @param value
	 *            the value, which has to be written to the digital output 'Zeiss_Part_Type_Loaded'
	 */
	public void setZeiss_Part_Type_Loaded(java.lang.Integer value)
	{
		setDigitalOutput("Zeiss_Part_Type_Loaded", value);
	}

}
