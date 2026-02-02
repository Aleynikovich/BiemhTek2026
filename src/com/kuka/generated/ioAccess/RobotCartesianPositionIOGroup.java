package com.kuka.generated.ioAccess;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>RobotCartesianPosition</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class RobotCartesianPositionIOGroup extends AbstractIOGroup
{
	/**
	 * Constructor to create an instance of class 'RobotCartesianPosition'.<br>
	 * <i>This constructor is automatically generated. Please, do not modify!</i>
	 *
	 * @param controller
	 *            the controller, which has access to the I/O group 'RobotCartesianPosition'
	 */
	@Inject
	public RobotCartesianPositionIOGroup(Controller controller)
	{
		super(controller, "RobotCartesianPosition");

		addDigitalOutput("X", IOTypes.UNSIGNED_INTEGER, 64);
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
	 * [0; ?]
	 *
	 * @return current value of the digital output 'X'
	 */
	public java.lang.Long getX()
	{
		return getNumberIOValue("X", true).longValue();
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
	 * [0; ?]
	 *
	 * @param value
	 *            the value, which has to be written to the digital output 'X'
	 */
	public void setX(java.lang.Long value)
	{
		setDigitalOutput("X", value);
	}

}
