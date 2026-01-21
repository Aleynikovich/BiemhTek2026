package com.kuka.generated.ioAccess;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>ProgramData</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class ProgramDataIOGroup extends AbstractIOGroup
{
    /**
     * Constructor to create an instance of class 'ProgramData'.<br>
     * <i>This constructor is automatically generated. Please, do not modify!</i>
     *
     * @param controller the controller, which has access to the I/O group 'ProgramData'
     */
    @Inject
    public ProgramDataIOGroup(Controller controller)
    {
        super(controller, "ProgramData");

        addInput("ProgramNumber", IOTypes.UNSIGNED_INTEGER, 16);
        addInput("DryRun", IOTypes.BOOLEAN, 1);
        addInput("ZRes1", IOTypes.BOOLEAN, 1);
        addInput("ZRes2", IOTypes.BOOLEAN, 1);
        addInput("ZRes3", IOTypes.BOOLEAN, 1);
        addInput("ZRes4", IOTypes.BOOLEAN, 1);
        addInput("ZRes5", IOTypes.BOOLEAN, 1);
        addInput("ZRes6", IOTypes.BOOLEAN, 1);
        addInput("ZRes7", IOTypes.BOOLEAN, 1);
    }

    /**
     * Gets the value of the <b>digital input '<i>ProgramNumber</i>'</b>.<br>
     * <i>This method is automatically generated. Please, do not modify!</i>
     * <p>
     * <b>I/O direction and type:</b><br>
     * digital input
     * <p>
     * <b>User description of the I/O:</b><br>
     * ./.
     * <p>
     * <b>Range of the I/O value:</b><br>
     * [0; 65535]
     *
     * @return current value of the digital input 'ProgramNumber'
     */
    public java.lang.Integer getProgramNumber()
    {
        return getNumberIOValue("ProgramNumber", false).intValue();
    }

    /**
     * Gets the value of the <b>digital input '<i>DryRun</i>'</b>.<br>
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
     * @return current value of the digital input 'DryRun'
     */
    public boolean getDryRun()
    {
        return getBooleanIOValue("DryRun", false);
    }

    /**
     * Gets the value of the <b>digital input '<i>ZRes1</i>'</b>.<br>
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
     * @return current value of the digital input 'ZRes1'
     */
    public boolean getZRes1()
    {
        return getBooleanIOValue("ZRes1", false);
    }

    /**
     * Gets the value of the <b>digital input '<i>ZRes2</i>'</b>.<br>
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
     * @return current value of the digital input 'ZRes2'
     */
    public boolean getZRes2()
    {
        return getBooleanIOValue("ZRes2", false);
    }

    /**
     * Gets the value of the <b>digital input '<i>ZRes3</i>'</b>.<br>
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
     * @return current value of the digital input 'ZRes3'
     */
    public boolean getZRes3()
    {
        return getBooleanIOValue("ZRes3", false);
    }

    /**
     * Gets the value of the <b>digital input '<i>ZRes4</i>'</b>.<br>
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
     * @return current value of the digital input 'ZRes4'
     */
    public boolean getZRes4()
    {
        return getBooleanIOValue("ZRes4", false);
    }

    /**
     * Gets the value of the <b>digital input '<i>ZRes5</i>'</b>.<br>
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
     * @return current value of the digital input 'ZRes5'
     */
    public boolean getZRes5()
    {
        return getBooleanIOValue("ZRes5", false);
    }

    /**
     * Gets the value of the <b>digital input '<i>ZRes6</i>'</b>.<br>
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
     * @return current value of the digital input 'ZRes6'
     */
    public boolean getZRes6()
    {
        return getBooleanIOValue("ZRes6", false);
    }

    /**
     * Gets the value of the <b>digital input '<i>ZRes7</i>'</b>.<br>
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
     * @return current value of the digital input 'ZRes7'
     */
    public boolean getZRes7()
    {
        return getBooleanIOValue("ZRes7", false);
    }

}
