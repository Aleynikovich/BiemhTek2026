package com.kuka.generated.ioAccess;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>RobotSafety</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class RobotSafetyIOGroup extends AbstractIOGroup
{
	/**
	 * Constructor to create an instance of class 'RobotSafety'.<br>
	 * <i>This constructor is automatically generated. Please, do not modify!</i>
	 *
	 * @param controller
	 *            the controller, which has access to the I/O group 'RobotSafety'
	 */
	@Inject
	public RobotSafetyIOGroup(Controller controller)
	{
		super(controller, "RobotSafety");

		addDigitalOutput("IsLocalEStop", IOTypes.BOOLEAN, 1);
		addDigitalOutput("IsExternalEStop", IOTypes.BOOLEAN, 1);
		addDigitalOutput("IsOperatorSafety", IOTypes.BOOLEAN, 1);
		addDigitalOutput("IsSafetyStopActive", IOTypes.BOOLEAN, 1);
		addDigitalOutput("ModeT1", IOTypes.BOOLEAN, 1);
		addDigitalOutput("ModeAut", IOTypes.BOOLEAN, 1);
		addDigitalOutput("MoveEnable", IOTypes.BOOLEAN, 1);
	}

	/**
	 * Gets the value of the <b>digital output '<i>IsLocalEStop</i>'</b>.<br>
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
	 * @return current value of the digital output 'IsLocalEStop'
	 */
	public boolean getIsLocalEStop()
	{
		return getBooleanIOValue("IsLocalEStop", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>IsLocalEStop</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'IsLocalEStop'
	 */
	public void setIsLocalEStop(java.lang.Boolean value)
	{
		setDigitalOutput("IsLocalEStop", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>IsExternalEStop</i>'</b>.<br>
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
	 * @return current value of the digital output 'IsExternalEStop'
	 */
	public boolean getIsExternalEStop()
	{
		return getBooleanIOValue("IsExternalEStop", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>IsExternalEStop</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'IsExternalEStop'
	 */
	public void setIsExternalEStop(java.lang.Boolean value)
	{
		setDigitalOutput("IsExternalEStop", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>IsOperatorSafety</i>'</b>.<br>
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
	 * @return current value of the digital output 'IsOperatorSafety'
	 */
	public boolean getIsOperatorSafety()
	{
		return getBooleanIOValue("IsOperatorSafety", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>IsOperatorSafety</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'IsOperatorSafety'
	 */
	public void setIsOperatorSafety(java.lang.Boolean value)
	{
		setDigitalOutput("IsOperatorSafety", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>IsSafetyStopActive</i>'</b>.<br>
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
	 * @return current value of the digital output 'IsSafetyStopActive'
	 */
	public boolean getIsSafetyStopActive()
	{
		return getBooleanIOValue("IsSafetyStopActive", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>IsSafetyStopActive</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'IsSafetyStopActive'
	 */
	public void setIsSafetyStopActive(java.lang.Boolean value)
	{
		setDigitalOutput("IsSafetyStopActive", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>ModeT1</i>'</b>.<br>
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
	 * @return current value of the digital output 'ModeT1'
	 */
	public boolean getModeT1()
	{
		return getBooleanIOValue("ModeT1", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>ModeT1</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'ModeT1'
	 */
	public void setModeT1(java.lang.Boolean value)
	{
		setDigitalOutput("ModeT1", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>ModeAut</i>'</b>.<br>
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
	 * @return current value of the digital output 'ModeAut'
	 */
	public boolean getModeAut()
	{
		return getBooleanIOValue("ModeAut", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>ModeAut</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'ModeAut'
	 */
	public void setModeAut(java.lang.Boolean value)
	{
		setDigitalOutput("ModeAut", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>MoveEnable</i>'</b>.<br>
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
	 * @return current value of the digital output 'MoveEnable'
	 */
	public boolean getMoveEnable()
	{
		return getBooleanIOValue("MoveEnable", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>MoveEnable</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'MoveEnable'
	 */
	public void setMoveEnable(java.lang.Boolean value)
	{
		setDigitalOutput("MoveEnable", value);
	}

}
