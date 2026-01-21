package com.kuka.generated.ioAccess;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>Photoneo</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class PhotoneoIOGroup extends AbstractIOGroup
{
    /**
     * Constructor to create an instance of class 'Photoneo'.<br>
     * <i>This constructor is automatically generated. Please, do not modify!</i>
     *
     * @param controller the controller, which has access to the I/O group 'Photoneo'
     */
    @Inject
    public PhotoneoIOGroup(Controller controller)
    {
        super(controller, "Photoneo");

    }

}
