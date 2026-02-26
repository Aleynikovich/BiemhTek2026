package biemhTekniker.lib.managers;

import biemhTekniker.lib.logger.Logger;
import biemhTekniker.programs.robot.RobotDispatcher;
import biemhTekniker.programs.vision.VisionDispatcher;
import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.AutExtIOGroup;
import com.kuka.roboticsAPI.conditionModel.BooleanIOCondition;
import com.kuka.roboticsAPI.conditionModel.ICondition;
import com.kuka.roboticsAPI.conditionModel.ObserverManager;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages automatic cycle execution for the robot.
 * Implements the sequence: home -> load reference -> full scan -> pick -> home -> place -> home -> repeat
 * Thread-safe for start/stop control from console server.
 */
public class AutoCycleManager
{
    private static final Logger log = Logger.getLogger(AutoCycleManager.class);
    
    // Sequence configuration
    private static final int PROGRAM_LOAD_REFERENCE = 100;
    private static final int PROGRAM_FULL_SCAN = 109;
    private static final int PROGRAM_PICK_NEW = 1;
    private static final int PROGRAM_PLACE_NEW = 2;
    private static final int PROGRAM_IDLE = 0;
    
    // Timing configuration
    private static final int CYCLE_DELAY_MS = 500;
    private static final int VISION_WAIT_MS = 2000;
    private static final int MAX_VISION_WAIT_ITERATIONS = 150; // 30 seconds max wait
    
    private final RobotDispatcher robotDispatcher;
    private final VisionDispatcher visionDispatcher;
    private final HomePositionManager homePositionManager;
    
    private final AtomicBoolean running;
    private Thread cycleThread;
    private final AutExtIOGroup autExtIO;
    private final ObserverManager observerManager;

    //JAVI
    public AutoCycleManager(RobotDispatcher robotDispatcher, VisionDispatcher visionDispatcher, HomePositionManager homePositionManager, AutExtIOGroup autExtIO, ObserverManager observerManager)
    {
        this.robotDispatcher = robotDispatcher;
        this.visionDispatcher = visionDispatcher;
        this.homePositionManager = homePositionManager;
        this.autExtIO = autExtIO;
        this.observerManager = observerManager;
        this.running = new AtomicBoolean(false);
    }
    
    /*
    public AutoCycleManager(RobotDispatcher robotDispatcher, VisionDispatcher visionDispatcher, HomePositionManager homePositionManager, AutExtIOGroup autExtIO, observerManager)
    {
        this.robotDispatcher = robotDispatcher;
        this.visionDispatcher = visionDispatcher;
        this.homePositionManager = homePositionManager;
        this.running = new AtomicBoolean(false);
    }*/
    
    /**
     * Starts the automatic cycle.
     * Non-blocking - starts cycle in background thread.
     */
    public synchronized void startCycle()
    {
        if (running.get())
        {
            log.warn("Auto cycle already running");
            return;
        }
        
        if (robotDispatcher.isBusy())
        {
            log.error("Cannot start auto cycle - robot is busy");
            return;
        }
        
        log.info("Starting automatic cycle");
        running.set(true);
        
        cycleThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                executeCycle();
            }
        }, "AutoCycleThread");
        cycleThread.setDaemon(true);
        cycleThread.start();
    }
    
    /**
     * Stops the automatic cycle.
     */
    public synchronized void stopCycle()
    {
        if (!running.get())
        {
            log.warn("Auto cycle is not running");
            return;
        }
        
        log.info("Stopping automatic cycle");
        running.set(false);
        
        // Wait for cycle thread to finish
        if (cycleThread != null)
        {
            try
            {
                cycleThread.join(5000); // Wait up to 5 seconds
                if (cycleThread.isAlive())
                {
                    log.warn("Auto cycle thread did not stop gracefully");
                }
            } catch (InterruptedException e)
            {
                log.warn("Interrupted while waiting for auto cycle thread");
                Thread.currentThread().interrupt();
            }
            cycleThread = null;
        }
    }
    
    /**
     * Checks if auto cycle is currently running.
     *
     * @return true if auto cycle is active
     */
    public boolean isRunning()
    {
        return running.get();
    }
    
    /**
     * Main cycle execution loop.
     * Runs in background thread.
     */
    private void executeCycle()
    {
        log.info("Auto cycle thread started");
        autExtIO.setPart_Unloaded(false);
        ThreadUtil.milliSleep(500);
        autExtIO.setPart_Loaded(false);
        ThreadUtil.milliSleep(500);
        while (running.get())
        {
            try
            {
                // Execute one complete cycle
                boolean success = executeOneCycle();
                
                if (!success)
                {
                    log.error("Auto cycle iteration failed - stopping");
                    break;
                }
                
                // Short delay before next cycle
                ThreadUtil.milliSleep(CYCLE_DELAY_MS);
                
            } catch (Exception e)
            {
                log.error("Error in auto cycle: " + e.getMessage(), e);
                break;
            }
        }
        
        running.set(false);
        log.info("Auto cycle thread stopped");
    }
    
    /**
     * Executes one complete cycle iteration.
     *
     * @return true if cycle completed successfully, false otherwise
     */
    private boolean executeOneCycle_Alex()
    {
        if (!running.get())
        {
            return false;
        }
        
        log.info("Auto cycle: Starting new iteration");
        
        // Step 1: Move to home
        log.info("Auto cycle: Step 1 - Moving to home");
        if (!executeHomeMove())
        {
            return false;
        }
        
        // Step 2: Load reference (vision program 100)
        log.info("Auto cycle: Step 2 - Loading reference");
        if (!executeVisionProgram(PROGRAM_LOAD_REFERENCE))
        {
            return false;
        }
        
        // Step 3: Full scan (vision program 109)
        log.info("Auto cycle: Step 3 - Full scan");
        if (!executeVisionProgram(PROGRAM_FULL_SCAN))
        {
            return false;
        }
        
        // Step 4: Pick new workpiece (robot program 1)
        log.info("Auto cycle: Step 4 - Pick new workpiece");      
            
        if (!executeRobotProgram(PROGRAM_PICK_NEW))
        {
            return false;
        }
        
        // Step 5: Move to home
        log.info("Auto cycle: Step 5 - Moving to home");
        if (!executeHomeMove())
        {
            return false;
        }
        
        // Step 6: Place new workpiece (robot program 2)
        // TODO: Add PLC handshake check here when IOs are available
        // Check if Zeiss PLC signals machine is not busy and in home position
        // For now, this is commented out as per requirements
        /*
        if (!checkZeissPLCReady())
        {
            log.warn("Auto cycle: Zeiss PLC not ready - skipping place operation");
            return true; // Continue cycle but skip place
        }
        */

        // NEW JAVI STEP 5.1
        log.info("Auto cycle: Steo 5.1 Waiting Zeiss request...");

        // Condicion Load
        BooleanIOCondition loadCondition = new BooleanIOCondition(autExtIO.getInput("Zeiss_Load_Resquest"), true);

        // Condicion Unload
        BooleanIOCondition unloadCondition = new BooleanIOCondition(autExtIO.getInput("Zeiss_Unload_Request"), true);

        // OR logico entre ambas
        ICondition zeissCondition = loadCondition.or(unloadCondition);

        observerManager.waitFor(zeissCondition);


        // END JAVI
        
        log.info("Auto cycle: Step 6 - Place new workpiece");
        if (!executeRobotProgram(PROGRAM_PLACE_NEW))
        {
            return false;
        }
        
        // Step 7: Move to home
        log.info("Auto cycle: Step 7 - Moving to home");
        if (!executeHomeMove())
        {
            return false;
        }

        //NEW JAVI
        //DECIMOS QUE HEMOS DESCARGADO Y LUEGO QUE HEMOS CARGADO

        autExtIO.setPart_Unloaded(true);

        ThreadUtil.milliSleep(500); //
        

        autExtIO.setPart_Unloaded(false);

        ThreadUtil.milliSleep(500);

        // Senalizar que la pieza ha sido cargada
        
        BooleanIOCondition loadCondition2 = new BooleanIOCondition(autExtIO.getInput("Zeiss_Load_Resquest"), true);
        ICondition zeissCondition_load = loadCondition.or(unloadCondition);
        observerManager.waitFor(zeissCondition_load);
        autExtIO.setPart_Loaded(true);

        ThreadUtil.milliSleep(500); //

        autExtIO.setPart_Loaded(false);

        //END JAVI
              
        log.info("Auto cycle: Iteration completed successfully");
        return true;
    }

    //NEW JAVI
    private boolean executeOneCycle()
    {
        if (!running.get())
            return false;

        log.info("Auto cycle: Starting new iteration");

        autExtIO.setPart_Unloaded(false);
        ThreadUtil.milliSleep(500);
        autExtIO.setPart_Loaded(false);
        autExtIO.setRobot_Out_Zeiss_Zone(false);
        ThreadUtil.milliSleep(500);
        // Step 1: Move to home
        log.info("Auto cycle: Step 1 - Moving to home");
        if (!executeHomeMove())
            return false;
        else
        	autExtIO.setRobot_Out_Zeiss_Zone(true);

        // Step 2: Load reference
        log.info("Auto cycle: Step 2 - Loading reference");
        if (!executeVisionProgram(PROGRAM_LOAD_REFERENCE))
            return false;

        // Step 3: Full scan (primer ciclo bloqueante)
        log.info("Auto cycle: Step 3 - Full scan");
        if (!executeVisionProgram(PROGRAM_FULL_SCAN))
            return false;

        // Step 4: Pick new workpiece
        log.info("Auto cycle: Step 4 - Pick new workpiece");
        if (!executeRobotProgram(PROGRAM_PICK_NEW))
            return false;

        // Step 5: Move to home
        log.info("Auto cycle: Step 5 - Moving to home");
        if (!executeHomeMove())
            return false;
        
        // ----------------------------
        // Disparar full scan no bloqueante
        // ----------------------------
        log.info("Auto cycle: Step 3b - Full scan (non-blocking dispatch)");
        visionDispatcher.dispatch(PROGRAM_FULL_SCAN);

        // Step 5.1: Wait Zeiss request
        log.info("Auto cycle: Step 5.1 Waiting Zeiss request...");
        BooleanIOCondition loadCondition = new BooleanIOCondition(autExtIO.getInput("Zeiss_Load_Resquest"), true);
        BooleanIOCondition unloadCondition = new BooleanIOCondition(autExtIO.getInput("Zeiss_Unload_Request"), true);
        ICondition zeissCondition = loadCondition.or(unloadCondition);
        observerManager.waitFor(zeissCondition);

        // Step 6: Place new workpiece
        log.info("Auto cycle: Step 6 - Place new workpiece");
        autExtIO.setRobot_Out_Zeiss_Zone(false);
        if (!executeRobotProgram(PROGRAM_PLACE_NEW))
            return false;

        // Step 7: Move to home
        log.info("Auto cycle: Step 7 - Moving to home");
        if (!executeHomeMove())
            return false;
        else
        	autExtIO.setRobot_Out_Zeiss_Zone(true);
        	ThreadUtil.milliSleep(500);

        // Senalizar descarga/carga de pieza
        autExtIO.setPart_Unloaded(true);
        ThreadUtil.milliSleep(500);
        autExtIO.setPart_Unloaded(false);
        ThreadUtil.milliSleep(500);
    	
        BooleanIOCondition loadCondition2 = new BooleanIOCondition(autExtIO.getInput("Zeiss_Load_Resquest"), true);
        ICondition zeissCondition_load = (loadCondition2);
        observerManager.waitFor(zeissCondition_load);
        
        autExtIO.setPart_Loaded(true);
        ThreadUtil.milliSleep(500);
        autExtIO.setPart_Loaded(false);

        log.info("Auto cycle: First iteration completed successfully");

        // ----------------------------
        // BUCLE REPETITIVO
        // ----------------------------
        while (running.get())
        {
            // Step 4: Pick new workpiece esperar full scan si sigue en ejecucion
            log.info("Auto cycle: Step 4 - Waiting for full scan before picking new workpiece");
            while (visionDispatcher.isBusy())
            {
                ThreadUtil.milliSleep(200); // espera activa ligera
            }

            log.info("Auto cycle: Step 4 - Pick new workpiece");
            if (!executeRobotProgram(PROGRAM_PICK_NEW))
                return false;

            // Step 5: Move to home
            log.info("Auto cycle: Step 5 - Moving to home");
            if (!executeHomeMove())
                return false;
            else
            	autExtIO.setRobot_Out_Zeiss_Zone(true);
            	ThreadUtil.milliSleep(500);

            // Disparar full scan no bloqueante para siguiente iteracion
            log.info("Auto cycle: Step 3b - Full scan (non-blocking dispatch)");
            visionDispatcher.dispatch(PROGRAM_FULL_SCAN);

            // Step 5.1: Wait Zeiss request
            log.info("Auto cycle: Step 5.1 Waiting Zeiss request...");
            observerManager.waitFor(zeissCondition);

            // Step 6: Place new workpiece
            autExtIO.setRobot_Out_Zeiss_Zone(false);
            log.info("Auto cycle: Step 6 - Place new workpiece");
            if (!executeRobotProgram(PROGRAM_PLACE_NEW))
                return false;

            // Step 7: Move to home
            log.info("Auto cycle: Step 7 - Moving to home");
            if (!executeHomeMove())
                return false;
            else
            	autExtIO.setRobot_Out_Zeiss_Zone(true);
            	ThreadUtil.milliSleep(500);

            // Senalizar descarga/carga de pieza
            autExtIO.setPart_Unloaded(true);
            ThreadUtil.milliSleep(500);
            autExtIO.setPart_Unloaded(false);
            ThreadUtil.milliSleep(500);
            
        	
            BooleanIOCondition loadCondition3 = new BooleanIOCondition(autExtIO.getInput("Zeiss_Load_Resquest"), true);
            ICondition zeissCondition_load2 = loadCondition3;
            observerManager.waitFor(zeissCondition_load2);
            
            
            autExtIO.setPart_Loaded(true);
            ThreadUtil.milliSleep(500);
            autExtIO.setPart_Loaded(false);

            // Pequena pausa antes de la siguiente iteracion
            ThreadUtil.milliSleep(100);
        }

        return true;
    }

    //END JAVI
    
    /**
     * Executes a home position move.
     *
     * @return true if successful
     */
    private boolean executeHomeMove()
    {
        if (!running.get())
        {
            return false;
        }
        
        try
        {
            homePositionManager.requestHomeMove();
            
            // Wait for home move to complete
            int iterations = 0;
            while (homePositionManager.isHomeMoveRequested() && iterations < 100)
            {
                ThreadUtil.milliSleep(1000);
                iterations++;
                
                if (!running.get())
                {
                    return false;
                }
            }
            
            if (homePositionManager.isHomeMoveRequested())
            {
                log.error("Home move timeout");
                return false;
            }
            
            return true;
        } catch (Exception e)
        {
            log.error("Home move failed: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Executes a vision program and waits for completion.
     *
     * @param programNumber Vision program number
     * @return true if successful
     */
    private boolean executeVisionProgram(int programNumber)
    {
        if (!running.get())
        {
            return false;
        }
        
        try
        {
            // Dispatch vision program (non-blocking)
            boolean dispatched = visionDispatcher.dispatch(programNumber);
            
            if (!dispatched)
            {
                log.error("Failed to dispatch vision program " + programNumber);
                return false;
            }
            
            // Wait for vision program to complete
            int iterations = 0;
            while (visionDispatcher.isBusy() && iterations < MAX_VISION_WAIT_ITERATIONS)
            {
                ThreadUtil.milliSleep(VISION_WAIT_MS);
                iterations++;
                
                if (!running.get())
                {
                    return false;
                }
            }
            
            if (visionDispatcher.isBusy())
            {
                log.error("Vision program " + programNumber + " timeout");
                return false;
            }
            
            log.debug("Vision program " + programNumber + " completed");
            return true;
            
        } catch (Exception e)
        {
            log.error("Vision program " + programNumber + " failed: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Executes a robot program and waits for completion.
     *
     * @param programNumber Robot program number
     * @return true if successful
     */
    private boolean executeRobotProgram(int programNumber)
    {
        if (!running.get())
        {
            return false;
        }
        
        try
        {
            // Dispatch robot program (blocking)
            boolean success = robotDispatcher.dispatch(programNumber);
            
            if (!success)
            {
                log.error("Robot program " + programNumber + " failed");
                return false;
            }
            
            log.debug("Robot program " + programNumber + " completed");
            return true;
            
        } catch (Exception e)
        {
            log.error("Robot program " + programNumber + " failed: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Placeholder for Zeiss PLC handshake check.
     * TODO: Implement when IOs are available.
     *
     * @return true if machine is ready for workpiece placement
     */
    @SuppressWarnings("unused")
    private boolean checkZeissPLCReady()
    {
        // TODO: Check Zeiss PLC IOs
        // - Machine not busy
        // - Machine in home position
        // Example:
        // return zeissPLCIO.getMachineReady() && zeissPLCIO.getMachineInHome();
        return true;
    }
}
