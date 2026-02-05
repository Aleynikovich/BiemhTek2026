package com.kuka.generated.ioAccess;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>MediaFlange</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class MediaFlangeIOGroup extends AbstractIOGroup
{
	/**
	 * Constructor to create an instance of class 'MediaFlange'.<br>
	 * <i>This constructor is automatically generated. Please, do not modify!</i>
	 *
	 * @param controller
	 *            the controller, which has access to the I/O group 'MediaFlange'
	 */
	@Inject
	public MediaFlangeIOGroup(Controller controller)
	{
		super(controller, "MediaFlange");

		addDigitalOutput("Gripper1_Switch", IOTypes.BOOLEAN, 1);
		addDigitalOutput("Gripper2_Switch", IOTypes.BOOLEAN, 1);
		addInput("Gripper1_isOpen", IOTypes.BOOLEAN, 1);
		addInput("Gripper1_isClosed", IOTypes.BOOLEAN, 1);
		addInput("Gripper2_isOpen", IOTypes.BOOLEAN, 1);
		addInput("Gripper2_isClosed", IOTypes.BOOLEAN, 1);
		addDigitalOutput("SecuritySwitch", IOTypes.BOOLEAN, 1);
		addInput("TestIN", IOTypes.BOOLEAN, 1);
		addInput("TESTIN2", IOTypes.BOOLEAN, 1);
		addInput("TESTIN3", IOTypes.BOOLEAN, 1);
		addInput("TESTIN4", IOTypes.BOOLEAN, 1);
		addInput("TESTIN5", IOTypes.BOOLEAN, 1);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Gripper1_Switch</i>'</b>.<br>
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
	 * @return current value of the digital output 'Gripper1_Switch'
	 */
	public boolean getGripper1_Switch()
	{
		return getBooleanIOValue("Gripper1_Switch", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Gripper1_Switch</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'Gripper1_Switch'
	 */
	public void setGripper1_Switch(java.lang.Boolean value)
	{
		setDigitalOutput("Gripper1_Switch", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Gripper2_Switch</i>'</b>.<br>
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
	 * @return current value of the digital output 'Gripper2_Switch'
	 */
	public boolean getGripper2_Switch()
	{
		return getBooleanIOValue("Gripper2_Switch", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Gripper2_Switch</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'Gripper2_Switch'
	 */
	public void setGripper2_Switch(java.lang.Boolean value)
	{
		setDigitalOutput("Gripper2_Switch", value);
	}

	/**
	 * Gets the value of the <b>digital input '<i>Gripper1_isOpen</i>'</b>.<br>
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
	 * @return current value of the digital input 'Gripper1_isOpen'
	 */
	public boolean getGripper1_isOpen()
	{
		return getBooleanIOValue("Gripper1_isOpen", false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>Gripper1_isClosed</i>'</b>.<br>
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
	 * @return current value of the digital input 'Gripper1_isClosed'
	 */
	public boolean getGripper1_isClosed()
	{
		return getBooleanIOValue("Gripper1_isClosed", false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>Gripper2_isOpen</i>'</b>.<br>
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
	 * @return current value of the digital input 'Gripper2_isOpen'
	 */
	public boolean getGripper2_isOpen()
	{
		return getBooleanIOValue("Gripper2_isOpen", false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>Gripper2_isClosed</i>'</b>.<br>
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
	 * @return current value of the digital input 'Gripper2_isClosed'
	 */
	public boolean getGripper2_isClosed()
	{
		return getBooleanIOValue("Gripper2_isClosed", false);
	}

	/**
	 * Gets the value of the <b>digital output '<i>SecuritySwitch</i>'</b>.<br>
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
	 * @return current value of the digital output 'SecuritySwitch'
	 */
	public boolean getSecuritySwitch()
	{
		return getBooleanIOValue("SecuritySwitch", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>SecuritySwitch</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'SecuritySwitch'
	 */
	public void setSecuritySwitch(java.lang.Boolean value)
	{
		setDigitalOutput("SecuritySwitch", value);
	}

	/**
	 * Gets the value of the <b>digital input '<i>TestIN</i>'</b>.<br>
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
	 * @return current value of the digital input 'TestIN'
	 */
	public boolean getTestIN()
	{
		return getBooleanIOValue("TestIN", false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>TESTIN2</i>'</b>.<br>
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
	 * @return current value of the digital input 'TESTIN2'
	 */
	public boolean getTESTIN2()
	{
		return getBooleanIOValue("TESTIN2", false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>TESTIN3</i>'</b>.<br>
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
	 * @return current value of the digital input 'TESTIN3'
	 */
	public boolean getTESTIN3()
	{
		return getBooleanIOValue("TESTIN3", false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>TESTIN4</i>'</b>.<br>
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
	 * @return current value of the digital input 'TESTIN4'
	 */
	public boolean getTESTIN4()
	{
		return getBooleanIOValue("TESTIN4", false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>TESTIN5</i>'</b>.<br>
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
	 * @return current value of the digital input 'TESTIN5'
	 */
	public boolean getTESTIN5()
	{
		return getBooleanIOValue("TESTIN5", false);
	}

}
