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
		addMockedInput("Gripper1_isOpen", IOTypes.BOOLEAN, 1, false);
		addMockedInput("Gripper1_isClosed", IOTypes.BOOLEAN, 1, false);
		addMockedInput("Gripper2_isOpen", IOTypes.BOOLEAN, 1, false);
		addMockedInput("Gripper2_isClosed", IOTypes.BOOLEAN, 1, false);
		addDigitalOutput("Test1", IOTypes.BOOLEAN, 1);
		addDigitalOutput("Test2", IOTypes.BOOLEAN, 1);
		addDigitalOutput("Test3", IOTypes.BOOLEAN, 1);
		addDigitalOutput("Test4", IOTypes.BOOLEAN, 1);
		addDigitalOutput("Test5", IOTypes.BOOLEAN, 1);
		addDigitalOutput("Test6", IOTypes.BOOLEAN, 1);
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
	* 
	 * @deprecated The output 'Gripper1_isOpen' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
	 */
	@Deprecated
	public boolean getGripper1_isOpen()
	{
		return getBooleanIOValue("Gripper1_isOpen", false);
	}

	/**
	 * Sets the value of the <b>mocked digital input '<i>Gripper1_isOpen</i>'</b>.<br>
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
	 *            the value, which has to be written to the mocked digital input 'Gripper1_isOpen'
	* 
	 * @deprecated The output 'Gripper1_isOpen' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
	 */
	@Deprecated
	public void setMockedGripper1_isOpenValue(java.lang.Boolean value)
	{
		setMockedInput("Gripper1_isOpen", value);
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
	* 
	 * @deprecated The output 'Gripper1_isClosed' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
	 */
	@Deprecated
	public boolean getGripper1_isClosed()
	{
		return getBooleanIOValue("Gripper1_isClosed", false);
	}

	/**
	 * Sets the value of the <b>mocked digital input '<i>Gripper1_isClosed</i>'</b>.<br>
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
	 *            the value, which has to be written to the mocked digital input 'Gripper1_isClosed'
	* 
	 * @deprecated The output 'Gripper1_isClosed' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
	 */
	@Deprecated
	public void setMockedGripper1_isClosedValue(java.lang.Boolean value)
	{
		setMockedInput("Gripper1_isClosed", value);
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
	* 
	 * @deprecated The output 'Gripper2_isOpen' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
	 */
	@Deprecated
	public boolean getGripper2_isOpen()
	{
		return getBooleanIOValue("Gripper2_isOpen", false);
	}

	/**
	 * Sets the value of the <b>mocked digital input '<i>Gripper2_isOpen</i>'</b>.<br>
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
	 *            the value, which has to be written to the mocked digital input 'Gripper2_isOpen'
	* 
	 * @deprecated The output 'Gripper2_isOpen' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
	 */
	@Deprecated
	public void setMockedGripper2_isOpenValue(java.lang.Boolean value)
	{
		setMockedInput("Gripper2_isOpen", value);
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
	* 
	 * @deprecated The output 'Gripper2_isClosed' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
	 */
	@Deprecated
	public boolean getGripper2_isClosed()
	{
		return getBooleanIOValue("Gripper2_isClosed", false);
	}

	/**
	 * Sets the value of the <b>mocked digital input '<i>Gripper2_isClosed</i>'</b>.<br>
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
	 *            the value, which has to be written to the mocked digital input 'Gripper2_isClosed'
	* 
	 * @deprecated The output 'Gripper2_isClosed' has not been assigned to a field bus address - thus this operation will be <b>simulated</b> only.
	 */
	@Deprecated
	public void setMockedGripper2_isClosedValue(java.lang.Boolean value)
	{
		setMockedInput("Gripper2_isClosed", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Test1</i>'</b>.<br>
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
	 * @return current value of the digital output 'Test1'
	 */
	public boolean getTest1()
	{
		return getBooleanIOValue("Test1", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Test1</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'Test1'
	 */
	public void setTest1(java.lang.Boolean value)
	{
		setDigitalOutput("Test1", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Test2</i>'</b>.<br>
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
	 * @return current value of the digital output 'Test2'
	 */
	public boolean getTest2()
	{
		return getBooleanIOValue("Test2", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Test2</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'Test2'
	 */
	public void setTest2(java.lang.Boolean value)
	{
		setDigitalOutput("Test2", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Test3</i>'</b>.<br>
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
	 * @return current value of the digital output 'Test3'
	 */
	public boolean getTest3()
	{
		return getBooleanIOValue("Test3", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Test3</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'Test3'
	 */
	public void setTest3(java.lang.Boolean value)
	{
		setDigitalOutput("Test3", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Test4</i>'</b>.<br>
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
	 * @return current value of the digital output 'Test4'
	 */
	public boolean getTest4()
	{
		return getBooleanIOValue("Test4", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Test4</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'Test4'
	 */
	public void setTest4(java.lang.Boolean value)
	{
		setDigitalOutput("Test4", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Test5</i>'</b>.<br>
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
	 * @return current value of the digital output 'Test5'
	 */
	public boolean getTest5()
	{
		return getBooleanIOValue("Test5", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Test5</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'Test5'
	 */
	public void setTest5(java.lang.Boolean value)
	{
		setDigitalOutput("Test5", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Test6</i>'</b>.<br>
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
	 * @return current value of the digital output 'Test6'
	 */
	public boolean getTest6()
	{
		return getBooleanIOValue("Test6", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Test6</i>'</b>.<br>
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
	 *            the value, which has to be written to the digital output 'Test6'
	 */
	public void setTest6(java.lang.Boolean value)
	{
		setDigitalOutput("Test6", value);
	}

}
