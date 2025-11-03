package application;


import javax.inject.Inject;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;

import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.motionModel.PTP;

/**
 * Implementation of a robot application.
 * <p>
 * The application provides a {@link RoboticsAPITask#initialize()} and a 
 * {@link RoboticsAPITask#run()} method, which will be called successively in 
 * the application lifecycle. The application will terminate automatically after 
 * the {@link RoboticsAPITask#run()} method has finished or after stopping the 
 * task. The {@link RoboticsAPITask#dispose()} method will be called, even if an 
 * exception is thrown during initialization or run. 
 * <p>
 * <b>It is imperative to call <code>super.dispose()</code> when overriding the 
 * {@link RoboticsAPITask#dispose()} method.</b> 
 * 
 * @see UseRoboticsAPIContext
 * @see #initialize()
 * @see #run()
 * @see #dispose()
 */
public class TestKUKA extends RoboticsAPIApplication {
	@Inject
	private LBR lbr;
	private PTP P_inicio = ptp(Math.toRadians(36), Math.toRadians(-47), Math.toRadians(10), Math.toRadians(-54), Math.toRadians(-78), Math.toRadians(97), Math.toRadians(-140));
	private PTP P_final = ptp(Math.toRadians(-35), Math.toRadians(76), Math.toRadians(62), Math.toRadians(93), Math.toRadians(-38), Math.toRadians(-88), Math.toRadians(-61));
	@Override
	public void initialize() {
		// initialize your application here
		 
		P_inicio.setJointVelocityRel(0.25);
		P_final.setJointVelocityRel(0.25);

	}

	@Override
	public void run() {
		// your application execution starts here
		
		while(true)
		{
			lbr.move(P_inicio);
			lbr.move(P_final);
		}
		
		
	}
}