package com.kuka.generated.ioAccess;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>EK1100</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class EK1100IOGroup extends AbstractIOGroup
{
	/**
	 * Constructor to create an instance of class 'EK1100'.<br>
	 * <i>This constructor is automatically generated. Please, do not modify!</i>
	 *
	 * @param controller
	 *            the controller, which has access to the I/O group 'EK1100'
	 */
	@Inject
	public EK1100IOGroup(Controller controller)
	{
		super(controller, "EK1100");

		addDigitalOutput("LED_RED", IOTypes.BOOLEAN, 1);
		addDigitalOutput("LED_GREEN", IOTypes.BOOLEAN, 1);
		addDigitalOutput("LED_YELLOW", IOTypes.BOOLEAN, 1);
	}

	/**
	 * Gets the value of the <b>digital output '<i>LED_RED</i>'</b>.<br>
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
	 * @return current value of the digital output 'LED_RED'
	 */
	public boolean getLED_RED()
	{
		return getBooleanIOValue("LED_RED", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>LED_RED</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'LED_RED'
	 */
	public void setLED_RED(java.lang.Boolean value)
	{
		setDigitalOutput("LED_RED", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>LED_GREEN</i>'</b>.<br>
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
	 * @return current value of the digital output 'LED_GREEN'
	 */
	public boolean getLED_GREEN()
	{
		return getBooleanIOValue("LED_GREEN", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>LED_GREEN</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'LED_GREEN'
	 */
	public void setLED_GREEN(java.lang.Boolean value)
	{
		setDigitalOutput("LED_GREEN", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>LED_YELLOW</i>'</b>.<br>
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
	 * @return current value of the digital output 'LED_YELLOW'
	 */
	public boolean getLED_YELLOW()
	{
		return getBooleanIOValue("LED_YELLOW", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>LED_YELLOW</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'LED_YELLOW'
	 */
	public void setLED_YELLOW(java.lang.Boolean value)
	{
		setDigitalOutput("LED_YELLOW", value);
	}

}
