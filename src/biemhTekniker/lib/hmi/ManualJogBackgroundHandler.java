package biemhTekniker.lib.hmi;

import biemhTekniker.lib.config.ConfigManager;
import biemhTekniker.lib.logger.Logger;
import com.kuka.roboticsAPI.applicationModel.tasks.CycleBehavior;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPICyclicBackgroundTask;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.uiModel.userKeys.IUserKeyBar;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

/**
 * Background task for manual jogging of the robot via SmartPad HMI buttons.
 * Creates 4 key bars with 3 buttons each for jogging in all 6 DOF directions.
 * Runs cyclically to poll button states and execute incremental linRel motions.
 */
public class ManualJogBackgroundHandler extends RoboticsAPICyclicBackgroundTask
{
    private static final Logger log = Logger.getLogger(ManualJogBackgroundHandler.class);

    @Inject
    LBR iiwa;

    @Inject
    Tool gripper;

    private ManualJogHandler jogHandler;
    private IUserKeyBar jogKeyBarPlusXYZ;
    private IUserKeyBar jogKeyBarMinusXYZ;
    private IUserKeyBar jogKeyBarPlusABC;
    private IUserKeyBar jogKeyBarMinusABC;

    @Override
    public void initialize()
    {
        // Load cycle time from configuration
        ConfigManager config = ConfigManager.getInstance();
        int cycleMs = config.getInt("jog.cycle.ms", 10);
        gripper.attachTo(iiwa.getFlange());
        // Initialize cyclic behavior
        initializeCyclic(0, cycleMs, TimeUnit.MILLISECONDS, CycleBehavior.BestEffort);

        // Initialize manual jog functionality
        initializeManualJog();
    }

    @Override
    protected void runCyclic()
    {
        if (jogHandler != null)
        {
            jogHandler.processCyclic();
        }
    }

    @Override
    public void dispose()
    {
        if (jogHandler != null)
        {
            jogHandler.clearAllFlags();
        }
        log.info("ManualJogBackgroundHandler disposed");
    }

    /**
     * Initializes the manual jog key bars and handler.
     */
    private void initializeManualJog()
    {
        try
        {
            log.info("Initializing manual jog key bars...");

            // Create the jog handler
            jogHandler = new ManualJogHandler(iiwa, gripper);

            // Create key bar for +XYZ directions
            jogKeyBarPlusXYZ = getApplicationUI().createUserKeyBar("Jog +XYZ");
            jogHandler.registerKeyBar(jogKeyBarPlusXYZ, 0, 2, 4, "X+", "Y+", "Z+");

            // Create key bar for -XYZ directions
            jogKeyBarMinusXYZ = getApplicationUI().createUserKeyBar("Jog -XYZ");
            jogHandler.registerKeyBar(jogKeyBarMinusXYZ, 1, 3, 5, "X-", "Y-", "Z-");

            // Create key bar for +ABC directions
            jogKeyBarPlusABC = getApplicationUI().createUserKeyBar("Jog +ABC");
            jogHandler.registerKeyBar(jogKeyBarPlusABC, 6, 8, 10, "A+", "B+", "C+");

            // Create key bar for -ABC directions
            jogKeyBarMinusABC = getApplicationUI().createUserKeyBar("Jog -ABC");
            jogHandler.registerKeyBar(jogKeyBarMinusABC, 7, 9, 11, "A-", "B-", "C-");

            log.info("Manual jog key bars initialized successfully");

        } catch (Exception e)
        {
            log.error("Failed to initialize manual jog: " + e.getMessage(), e);
        }
    }
}
