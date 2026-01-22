package com.kuka.generated.ioAccess;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>Vision</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class VisionIOGroup extends AbstractIOGroup
{
	/**
	 * Constructor to create an instance of class 'Vision'.<br>
	 * <i>This constructor is automatically generated. Please, do not modify!</i>
	 *
	 * @param controller
	 *            the controller, which has access to the I/O group 'Vision'
	 */
	@Inject
	public VisionIOGroup(Controller controller)
	{
		super(controller, "Vision");

		addInput("TriggerRequest", IOTypes.BOOLEAN, 1);
		addDigitalOutput("TriggerSent", IOTypes.BOOLEAN, 1);
		addDigitalOutput("PickPositionReady", IOTypes.BOOLEAN, 1);
	}

	/**
	 * Gets the value of the <b>digital input '<i>TriggerRequest</i>'</b>.<br>
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
	 * @return current value of the digital input 'TriggerRequest'
	 */
	public boolean getTriggerRequest()
	{
		return getBooleanIOValue("TriggerRequest", false);
	}

	/**
	 * Gets the value of the <b>digital output '<i>TriggerSent</i>'</b>.<br>
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
	 * @return current value of the digital output 'TriggerSent'
	 */
	public boolean getTriggerSent()
	{
		return getBooleanIOValue("TriggerSent", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>TriggerSent</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'TriggerSent'
	 */
	public void setTriggerSent(java.lang.Boolean value)
	{
		setDigitalOutput("TriggerSent", value);
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

}
