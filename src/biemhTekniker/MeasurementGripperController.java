package biemhTekniker;

import javax.inject.Inject;

import com.kuka.generated.ioAccess.PlcRequestsGrippersIOGroup;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPIBackgroundTask;

/**
 * Controller for Measurement Machine gripper via PLC requests.
 * PLC is the cell master - all operations are gated by handshake.
 */
public class MeasurementGripperController extends RoboticsAPIBackgroundTask {
    
    private static final int POLL_INTERVAL_MS = 50;
    
    @Inject
    private PlcRequestsGrippersIOGroup plcRequestsIO;
    
    public interface PlcRequestListener {
        void onGripper1OpenRequest();
        void onGripper1CloseRequest();
        void onGripper2OpenRequest();
        void onGripper2CloseRequest();
    }
    
    private PlcRequestListener listener;
    
    public void setListener(PlcRequestListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void initialize() {
        getLogger().info("MeasurementGripperController initialized");
    }
    
    @Override
    public void run() {
        boolean prevGripper1Open = false;
        boolean prevGripper1Close = false;
        boolean prevGripper2Open = false;
        boolean prevGripper2Close = false;
        
        getLogger().info("MeasurementGripperController started monitoring PLC requests");
        
        while (!Thread.currentThread().isInterrupted()) {
            try {
                boolean g1Open = plcRequestsIO.getGripper1Open();
                boolean g1Close = plcRequestsIO.getGripper1Close();
                boolean g2Open = plcRequestsIO.getGripper2Open();
                boolean g2Close = plcRequestsIO.getGripper2Close();
                
                // Detect rising edges
                if (g1Open && !prevGripper1Open && listener != null) {
                    getLogger().info("PLC Request: Gripper 1 OPEN");
                    listener.onGripper1OpenRequest();
                }
                
                if (g1Close && !prevGripper1Close && listener != null) {
                    getLogger().info("PLC Request: Gripper 1 CLOSE");
                    listener.onGripper1CloseRequest();
                }
                
                if (g2Open && !prevGripper2Open && listener != null) {
                    getLogger().info("PLC Request: Gripper 2 OPEN");
                    listener.onGripper2OpenRequest();
                }
                
                if (g2Close && !prevGripper2Close && listener != null) {
                    getLogger().info("PLC Request: Gripper 2 CLOSE");
                    listener.onGripper2CloseRequest();
                }
                
                prevGripper1Open = g1Open;
                prevGripper1Close = g1Close;
                prevGripper2Open = g2Open;
                prevGripper2Close = g2Close;
                
                Thread.sleep(POLL_INTERVAL_MS);
                
            } catch (InterruptedException e) {
                getLogger().info("MeasurementGripperController interrupted, stopping");
                break;
            } catch (Exception e) {
                getLogger().error("MeasurementGripperController error: " + e.getMessage());
            }
        }
        
        getLogger().info("MeasurementGripperController stopped");
    }
    
    public boolean isGripper1OpenRequested() {
        return plcRequestsIO.getGripper1Open();
    }
    
    public boolean isGripper1CloseRequested() {
        return plcRequestsIO.getGripper1Close();
    }
    
    public boolean isGripper2OpenRequested() {
        return plcRequestsIO.getGripper2Open();
    }
    
    public boolean isGripper2CloseRequested() {
        return plcRequestsIO.getGripper2Close();
    }
    
    @Override
    public void dispose() {
        super.dispose();
    }
}
