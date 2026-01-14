package hardware;

import com.kuka.generated.ioAccess.PlcRequestsGrippersIOGroup;
import java.util.concurrent.atomic.AtomicBoolean;

import common.ILogger;

/**
 * Controller for Measurement Machine gripper via PLC requests.
 * Implements request logic using Profinet signals controlled by PLC.
 * PLC is the cell master - all operations are gated by handshake.
 * 
 * Java 1.7 compatible - no lambdas, no diamond operators
 */
public class MeasurementGripperController implements Runnable {
    
    private PlcRequestsGrippersIOGroup plcRequestsIO;
    private ILogger logger;
    
    private AtomicBoolean monitoring;
    private Thread monitorThread;
    
    // Callback interface for PLC requests
    public interface PlcRequestListener {
        void onGripper1OpenRequest();
        void onGripper1CloseRequest();
        void onGripper2OpenRequest();
        void onGripper2CloseRequest();
    }
    
    private PlcRequestListener listener;
    
    /**
     * Create measurement gripper controller
     * @param plcRequestsIO PLC requests I/O group
     * @param logger Task logger
     */
    public MeasurementGripperController(PlcRequestsGrippersIOGroup plcRequestsIO, ILogger logger) {
        this.plcRequestsIO = plcRequestsIO;
        this.logger = logger;
        this.monitoring = new AtomicBoolean(false);
    }
    
    /**
     * Set listener for PLC request callbacks
     * @param listener Callback listener
     */
    public void setListener(PlcRequestListener listener) {
        this.listener = listener;
    }
    
    /**
     * Start monitoring PLC requests in background
     */
    public void startMonitoring() {
        if (monitoring.get()) {
            logger.warn("MeasurementGripperController already monitoring");
            return;
        }
        
        monitoring.set(true);
        
        monitorThread = new Thread(this);
        monitorThread.setDaemon(true);
        monitorThread.setName("MeasurementGripperController");
        monitorThread.start();
        
        logger.info("MeasurementGripperController started monitoring PLC requests");
    }
    
    /**
     * Stop monitoring PLC requests
     */
    public void stopMonitoring() {
        monitoring.set(false);
        
        if (monitorThread != null) {
            try {
                monitorThread.interrupt();
                monitorThread.join(1000);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
        
        logger.info("MeasurementGripperController stopped monitoring");
    }
    
    /**
     * Background thread that polls PLC request signals
     */
    public void run() {
        boolean prevGripper1Open = false;
        boolean prevGripper1Close = false;
        boolean prevGripper2Open = false;
        boolean prevGripper2Close = false;
        
        while (monitoring.get()) {
            try {
                // Poll PLC request signals
                boolean g1Open = plcRequestsIO.getGripper1Open();
                boolean g1Close = plcRequestsIO.getGripper1Close();
                boolean g2Open = plcRequestsIO.getGripper2Open();
                boolean g2Close = plcRequestsIO.getGripper2Close();
                
                // Detect rising edges (request initiated)
                if (g1Open && !prevGripper1Open && listener != null) {
                    logger.info("PLC Request: Gripper 1 OPEN");
                    listener.onGripper1OpenRequest();
                }
                
                if (g1Close && !prevGripper1Close && listener != null) {
                    logger.info("PLC Request: Gripper 1 CLOSE");
                    listener.onGripper1CloseRequest();
                }
                
                if (g2Open && !prevGripper2Open && listener != null) {
                    logger.info("PLC Request: Gripper 2 OPEN");
                    listener.onGripper2OpenRequest();
                }
                
                if (g2Close && !prevGripper2Close && listener != null) {
                    logger.info("PLC Request: Gripper 2 CLOSE");
                    listener.onGripper2CloseRequest();
                }
                
                // Store previous states
                prevGripper1Open = g1Open;
                prevGripper1Close = g1Close;
                prevGripper2Open = g2Open;
                prevGripper2Close = g2Close;
                
                // Poll at 50ms intervals (20Hz)
                Thread.sleep(50);
                
            } catch (InterruptedException e) {
                if (monitoring.get()) {
                    logger.warn("MeasurementGripperController interrupted");
                }
                break;
            } catch (Exception e) {
                logger.error("MeasurementGripperController error: " + e.getMessage());
                // Continue monitoring even on error
            }
        }
    }
    
    /**
     * Check if PLC is requesting Gripper 1 open
     * @return true if request active
     */
    public boolean isGripper1OpenRequested() {
        return plcRequestsIO.getGripper1Open();
    }
    
    /**
     * Check if PLC is requesting Gripper 1 close
     * @return true if request active
     */
    public boolean isGripper1CloseRequested() {
        return plcRequestsIO.getGripper1Close();
    }
    
    /**
     * Check if PLC is requesting Gripper 2 open
     * @return true if request active
     */
    public boolean isGripper2OpenRequested() {
        return plcRequestsIO.getGripper2Open();
    }
    
    /**
     * Check if PLC is requesting Gripper 2 close
     * @return true if request active
     */
    public boolean isGripper2CloseRequested() {
        return plcRequestsIO.getGripper2Close();
    }
    
    public boolean isMonitoring() {
        return monitoring.get();
    }
}
