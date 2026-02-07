package biemhTekniker;

import biemhTekniker.console.ConsoleServer;
import biemhTekniker.console.ConsoleServerInterface;
import biemhTekniker.data.WorkpieceData;
import biemhTekniker.logger.Logger;
import biemhTekniker.managers.LoggingManager;
import biemhTekniker.programs.*;
import biemhTekniker.vision.SmartPickingThread;
import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.generated.ioAccess.RobotCartesianPositionIOGroup;
import com.kuka.generated.ioAccess.RobotSafetyIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

/**
 * Main robot application.
 * Manages program execution, logging, and vision system integration.
 * Implements ConsoleServerInterface for GUI control.
 */
public class Main extends RoboticsAPIApplication implements ConsoleServerInterface
{
    private static final Logger             log                = Logger.getLogger(Main.class);
    // Configuration
    private static final String             VISION_SERVER_IP   = "172.31.1.69";
    private static final int                VISION_SERVER_PORT = 59002;
    @Inject private      LBR                iiwa;
    @Inject private      RobotSafetyIOGroup safetyIO;
    // Managers and threads
    private              LoggingManager     loggingManager;
    private              SmartPickingThread smartPickingThread;
    private              ConsoleServer      consoleServer;

    // Gripper data
    @Inject @Named("Gripper") // Matches the name defined in your Station Setup
    private Tool gripper;

    // Gripper IOs
    @Inject private MediaFlangeIOGroup gripperIO;

    @Inject private RobotCartesianPositionIOGroup currentCartesianPosition;

    // Shared data
    private WorkpieceData workpieceData;

    // Program dispatch
    private final BlockingQueue<RobotProgram> programQueue    = new LinkedBlockingQueue<RobotProgram>();
    private final Map<Integer, ProgramFactory> programRegistry = new HashMap<Integer, ProgramFactory>();
    private       ProgramContext               programContext;

    @Override public void initialize()
    {
        // Initialize logging
        loggingManager = new LoggingManager();
        loggingManager.initialize();

        // Initialize shared data, workpiece data from camera
        workpieceData = new WorkpieceData();

        // Gripper
        gripper.attachTo(iiwa.getFlange());

        // Initialize and start SmartPicking thread
        smartPickingThread = new SmartPickingThread(VISION_SERVER_IP, VISION_SERVER_PORT);
        smartPickingThread.initialize();
        smartPickingThread.start();

        // Initialize and start console server for GUI control
        consoleServer = new ConsoleServer(this);
        consoleServer.initialize();

        // Create program context with shared dependencies
        programContext = new ProgramContext(
            this,
            iiwa,
            gripper,
            gripperIO,
            workpieceData,
            smartPickingThread.getProtocol()
        );

        // Register programs using factories
        registerPrograms();

        // Set robot control parameters
        getApplicationControl().setApplicationOverride(0.5);
        getApplicationControl().clipManualOverride(0.0);
        log.info("Main application initialized");
    }

    /**
     * Registers all available programs with their factory methods.
     * Uses anonymous inner classes for Java 1.7 compatibility.
     */
    private void registerPrograms()
    {
        // Program 1: Get New Workpiece Position
        programRegistry.put(1, new ProgramFactory()
        {
            public RobotProgram create(ProgramContext ctx)
            {
                final GetNewWorkpiecePositionProgram program = new GetNewWorkpiecePositionProgram(
                    ctx.getProtocol(),
                    ctx.getWorkpieceData()
                );
                return new ProgramAdapter("Get New Workpiece Position", new ProgramAdapter.Action()
                {
                    public boolean run()
                    {
                        return executeWithVisionCheck(program);
                    }
                });
            }
        });

        // Program 2: Calibration
        programRegistry.put(2, new ProgramFactory()
        {
            public RobotProgram create(ProgramContext ctx)
            {
                // Pre-move to P1 frame before calibration
                try
                {
                    ctx.getIiwa().getFlange().move(ptp(getApplicationData().getFrame("/P1")));
                }
                catch (Exception e)
                {
                    log.error("Failed to move to P1 frame: " + e.getMessage());
                }

                final CalibrationProgram program = new CalibrationProgram(
                    ctx.getApplication(),
                    ctx.getIiwa(),
                    ctx.getProtocol(),
                    ctx.getGripper()
                );
                return new ProgramAdapter("Calibration", new ProgramAdapter.Action()
                {
                    public boolean run()
                    {
                        return executeWithVisionCheck(program);
                    }
                });
            }
        });

        // Program 3: Test Calibration
        programRegistry.put(3, new ProgramFactory()
        {
            public RobotProgram create(ProgramContext ctx)
            {
                final TestCalibrationProgram program = new TestCalibrationProgram(
                    ctx.getApplication(),
                    ctx.getIiwa(),
                    ctx.getProtocol(),
                    ctx.getGripper()
                );
                return new ProgramAdapter("Test Calibration", new ProgramAdapter.Action()
                {
                    public boolean run()
                    {
                        return executeWithVisionCheck(program);
                    }
                });
            }
        });

        // Program 4: Pick New Workpiece
        programRegistry.put(4, new ProgramFactory()
        {
            public RobotProgram create(ProgramContext ctx)
            {
                final PickNewWorkpieceProgram program = new PickNewWorkpieceProgram(
                    ctx.getApplication(),
                    ctx.getIiwa(),
                    ctx.getWorkpieceData(),
                    ctx.getGripper(),
                    ctx.getGripperIO()
                );
                return new ProgramAdapter("Pick New Workpiece", new ProgramAdapter.Action()
                {
                    public boolean run()
                    {
                        return program.execute();
                    }
                });
            }
        });

        // Program 5: Place New Workpiece
        programRegistry.put(5, new ProgramFactory()
        {
            public RobotProgram create(ProgramContext ctx)
            {
                final PlaceNewWorkpieceProgram program = new PlaceNewWorkpieceProgram(
                    ctx.getApplication(),
                    ctx.getIiwa(),
                    ctx.getGripper(),
                    ctx.getGripperIO()
                );
                return new ProgramAdapter("Place New Workpiece", new ProgramAdapter.Action()
                {
                    public boolean run()
                    {
                        return program.execute();
                    }
                });
            }
        });

        // Program 6: Pick Measured Workpiece
        programRegistry.put(6, new ProgramFactory()
        {
            public RobotProgram create(ProgramContext ctx)
            {
                final PickMeasuredWorkpieceProgram program = new PickMeasuredWorkpieceProgram(
                    ctx.getApplication(),
                    ctx.getIiwa()
                );
                return new ProgramAdapter("Pick Measured Workpiece", new ProgramAdapter.Action()
                {
                    public boolean run()
                    {
                        return program.execute();
                    }
                });
            }
        });

        // Program 7: Place Measured Workpiece
        programRegistry.put(7, new ProgramFactory()
        {
            public RobotProgram create(ProgramContext ctx)
            {
                final PlaceMeasuredWorkpieceProgram program = new PlaceMeasuredWorkpieceProgram(
                    ctx.getApplication(),
                    ctx.getIiwa()
                );
                return new ProgramAdapter("Place Measured Workpiece", new ProgramAdapter.Action()
                {
                    public boolean run()
                    {
                        return program.execute();
                    }
                });
            }
        });

        log.info("Registered " + programRegistry.size() + " programs");
    }

    /**
     * Helper to execute a program with vision connection check.
     * Wraps the common pattern used by vision-dependent programs.
     */
    private boolean executeWithVisionCheck(Object program)
    {
        if (!checkVisionConnection())
        {
            return false;
        }
        // Use reflection to call execute() on any program object
        try
        {
            return ((Boolean) program.getClass().getMethod("execute").invoke(program)).booleanValue();
        }
        catch (Exception e)
        {
            log.error("Failed to execute program: " + e.getMessage());
            return false;
        }
    }

    @Override public void dispose()
    {
        log.info("Main application shutting down");

        // Shutdown console server
        if (consoleServer != null)
        {
            consoleServer.dispose();
        }

        // Shutdown SmartPicking thread
        if (smartPickingThread != null)
        {
            smartPickingThread.shutdown();
            try
            {
                smartPickingThread.join(15000); // Increased timeout to 15 seconds
                if (smartPickingThread.isAlive())
                {
                    log.warn("SmartPicking thread did not stop gracefully, interrupting");
                    smartPickingThread.interrupt();
                }
            }
            catch (InterruptedException e)
            {
                log.warn("Interrupted while waiting for SmartPicking thread to finish");
                Thread.currentThread().interrupt();
            }
        }

        // Shutdown logging
        if (loggingManager != null)
        {
            loggingManager.shutdown();
        }

        super.dispose();
    }

    @Override public void run()
    {
        log.info("Main application running, moving home");
        iiwa.getFlange().move(ptp(getApplicationData().getFrame("/BiemhHome")));

        // Queue-based program dispatcher
        while (true)
        {
            try
            {
                // Poll the queue with timeout to allow checking for shutdown
                RobotProgram program = programQueue.poll(200, java.util.concurrent.TimeUnit.MILLISECONDS);
                
                if (program != null)
                {
                    log.info("Executing program: " + program.getName());
                    boolean success = program.execute();
                    logProgramResult(program.getName(), success);
                }
            }
            catch (InterruptedException e)
            {
                log.warn("Program dispatcher interrupted");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // ========== ConsoleServerInterface Implementation ==========

    @Override public void setProgramNumber(int programNumber)
    {
        if (programNumber == 0)
        {
            // Program 0 is idle - do nothing
            log.info("Program 0 (idle) requested via console");
            return;
        }

        ProgramFactory factory = programRegistry.get(Integer.valueOf(programNumber));
        if (factory != null)
        {
            RobotProgram program = factory.create(programContext);
            boolean added = programQueue.offer(program);
            if (added)
            {
                log.info("Program " + programNumber + " (" + program.getName() + ") enqueued via console");
            }
            else
            {
                log.error("Failed to enqueue program " + programNumber + " - queue full");
            }
        }
        else
        {
            log.warn("Invalid program number requested: " + programNumber);
        }
    }

    @Override public int getCurrentProgram()
    {
        // Return the queue size as an indication of pending programs
        // This maintains backward compatibility with the console interface
        return programQueue.size();
    }

    @Override public boolean isVisionConnected()
    {
        return smartPickingThread != null && smartPickingThread.isConnected();
    }

    @Override public String getWorkpiecePosition()
    {
        if (workpieceData != null && workpieceData.isValid())
        {
            return workpieceData.toString();
        }
        return "invalid";
    }

    // ========== Helper Methods ==========

    private boolean checkVisionConnection()
    {
        if (!smartPickingThread.isConnected())
        {
            log.error("Cannot execute program - not connected to vision server");
            return false;
        }
        return true;
    }

    private void logProgramResult(String programName, boolean success)
    {
        if (success)
        {
            log.info(programName + " program completed successfully");
        }
        else
        {
            log.error(programName + " program failed");
        }
    }
}
