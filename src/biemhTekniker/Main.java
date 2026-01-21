package biemhTekniker;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import javax.inject.Inject;


@SuppressWarnings("unused")
public class Main extends RoboticsAPIApplication
{
    @Inject
    private LBR iiwa;

    @Override
    public void initialize()
    {

    }

    @Override
    public void run()
    {
    	while(true)
    	{
    		try {
    			getLogger().info("Server started on port 30001");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

    @Override
    public void dispose()
    {

        super.dispose();
    }

}