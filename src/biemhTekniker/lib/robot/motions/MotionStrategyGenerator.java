package biemhTekniker.lib.robot.motions;

import biemhTekniker.lib.config.ConfigManager;
import biemhTekniker.lib.config.ImpedanceConfig;
import biemhTekniker.lib.logger.Logger;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for generating motion strategies with redundancy variations.
 * Can be used by any program that needs to try multiple robot configurations
 * to reach a target position.
 */
public class MotionStrategyGenerator
{
    // Cached configuration values (volatile for thread-safe lazy initialization)
    private static volatile double[] cachedRedundancyOffsets = null;
    private static volatile double[] cachedZRotationAngles = null;
    private static volatile ImpedanceConfig impedanceConfig = null;
    private static volatile CartesianImpedanceControlMode cachedImpedanceMode = null;
    private static boolean impedanceModeInitialized = false;

    /**
     * Loads redundancy offsets from configuration.
     * Defaults: -80, 80, -60, 60 degrees
     */
    private static synchronized double[] getRedundancyOffsets()
    {
        if (cachedRedundancyOffsets == null)
        {
            ConfigManager config = ConfigManager.getInstance();
            String offsetsStr = config.getString("motion.redundancy.offsets", "-80,80,-60,60");
            String[] parts = offsetsStr.split(",");
            cachedRedundancyOffsets = new double[parts.length];
            try
            {
                for (int i = 0; i < parts.length; i++)
                {
                    cachedRedundancyOffsets[i] = Math.toRadians(Double.parseDouble(parts[i].trim()));
                }
            } catch (NumberFormatException e)
            {
                Logger.getLogger(MotionStrategyGenerator.class).error("Invalid redundancy offsets in configuration: " + offsetsStr + ", using defaults");
                // Fallback to defaults
                cachedRedundancyOffsets = new double[]{Math.toRadians(-80), Math.toRadians(80), Math.toRadians(-60), Math.toRadians(60)};
            }
        }
        return cachedRedundancyOffsets;
    }

    /**
     * Loads Z-rotation angles from configuration.
     * Defaults: 0, 45, 90, 135, 180, -45, -90, -135 degrees
     */
    private static synchronized double[] getZRotationAngles()
    {
        if (cachedZRotationAngles == null)
        {
            ConfigManager config = ConfigManager.getInstance();
            String anglesStr = config.getString("motion.place.z.rotations", "90,45,0,135,180,-45,-90,-135");
            String[] parts = anglesStr.split(",");
            cachedZRotationAngles = new double[parts.length];
            try
            {
                for (int i = 0; i < parts.length; i++)
                {
                    cachedZRotationAngles[i] = Math.toRadians(Double.parseDouble(parts[i].trim()));
                }
            } catch (NumberFormatException e)
            {
                Logger.getLogger(MotionStrategyGenerator.class).error("Invalid Z-rotation angles in configuration: " + anglesStr + ", using defaults");
                // Fallback to defaults
                cachedZRotationAngles = new double[]{Math.toRadians(90), Math.toRadians(45), Math.toRadians(0), Math.toRadians(135), Math.toRadians(180), Math.toRadians(-45), Math.toRadians(-90), Math.toRadians(-135)};
            }
        }
        return cachedZRotationAngles;
    }

    /**
     * Gets or creates the impedance configuration singleton (thread-safe).
     *
     * @return ImpedanceConfig instance
     */
    private static synchronized ImpedanceConfig getImpedanceConfig()
    {
        if (impedanceConfig == null)
        {
            impedanceConfig = new ImpedanceConfig();
        }
        return impedanceConfig;
    }

    /**
     * Gets the impedance control mode if enabled in configuration.
     * Caches the instance to avoid redundant object creation.
     *
     * @return CartesianImpedanceControlMode or null if disabled
     */
    private static synchronized CartesianImpedanceControlMode getImpedanceMode()
    {
        if (!impedanceModeInitialized)
        {
            cachedImpedanceMode = getImpedanceConfig().createControlMode();
            impedanceModeInitialized = true;
        }
        return cachedImpedanceMode;
    }

    /**
     * Generates a list of motion strategies with default redundancy variations.
     * Strategy order: regular position, then alternate position (180° rotation),
     * each with multiple redundancy configurations.
     * Impedance control is automatically enabled if configured.
     *
     * @param tcp   Tool center point frame to use
     * @param robot Robot instance for redundancy support
     * @return List of motion strategies in priority order
     */
    public static List<MotionStrategy> generateStrategies(ObjectFrame tcp, LBR robot)
    {
        return generateStrategies(tcp, robot, getRedundancyOffsets());
    }

    /**
     * Generates a list of motion strategies with custom redundancy variations.
     * Strategy order: regular position, then alternate position (180° rotation),
     * each with multiple redundancy configurations.
     * Impedance control is automatically enabled if configured.
     *
     * @param tcp               Tool center point frame to use
     * @param robot             Robot instance for redundancy support
     * @param redundancyOffsets Array of E1 offsets in radians to try
     * @return List of motion strategies in priority order
     */
    public static List<MotionStrategy> generateStrategies(ObjectFrame tcp, LBR robot, double[] redundancyOffsets)
    {
        List<MotionStrategy> strategies = new ArrayList<>();
        CartesianImpedanceControlMode impedanceMode = getImpedanceMode();

        // Try regular position with different redundancy configurations
        // First attempt without redundancy (null), then with offsets
        strategies.add(new MotionStrategy.Builder(tcp).impedanceMode(impedanceMode).build());
        for (int i = 0; i < redundancyOffsets.length; i++)
        {
            Double offset = Double.valueOf(redundancyOffsets[i]);
            strategies.add(new MotionStrategy.Builder(tcp).redundancy(offset, robot).impedanceMode(impedanceMode).build());
        }

        // Try alternate position (180° rotation) with different redundancy configurations
        // First attempt without redundancy (null), then with offsets
        strategies.add(new MotionStrategy.Builder(tcp).useAlternatePosition(true).impedanceMode(impedanceMode).build());
        for (int i = 0; i < redundancyOffsets.length; i++)
        {
            Double offset = Double.valueOf(redundancyOffsets[i]);
            strategies.add(new MotionStrategy.Builder(tcp).useAlternatePosition(true).redundancy(offset, robot).impedanceMode(impedanceMode).build());
        }

        return strategies;
    }

    /**
     * Generates a simplified list of motion strategies without alternate position.
     * Only tries regular position with redundancy variations.
     * Impedance control is automatically enabled if configured.
     *
     * @param tcp   Tool center point frame to use
     * @param robot Robot instance for redundancy support
     * @return List of motion strategies in priority order
     */
    public static List<MotionStrategy> generateStrategiesWithoutAlternate(ObjectFrame tcp, LBR robot)
    {
        return generateStrategiesWithoutAlternate(tcp, robot, getRedundancyOffsets());
    }

    /**
     * Generates a simplified list of motion strategies without alternate position.
     * Only tries regular position with redundancy variations.
     * Impedance control is automatically enabled if configured.
     *
     * @param tcp               Tool center point frame to use
     * @param robot             Robot instance for redundancy support
     * @param redundancyOffsets Array of E1 offsets in radians to try
     * @return List of motion strategies in priority order
     */
    public static List<MotionStrategy> generateStrategiesWithoutAlternate(ObjectFrame tcp, LBR robot, double[] redundancyOffsets)
    {
        List<MotionStrategy> strategies = new ArrayList<MotionStrategy>();
        CartesianImpedanceControlMode impedanceMode = getImpedanceMode();

        // Try regular position with different redundancy configurations
        strategies.add(new MotionStrategy.Builder(tcp).impedanceMode(impedanceMode).build());
        for (int i = 0; i < redundancyOffsets.length; i++)
        {
            Double offset = Double.valueOf(redundancyOffsets[i]);
            strategies.add(new MotionStrategy.Builder(tcp).redundancy(offset, robot).impedanceMode(impedanceMode).build());
        }

        return strategies;
    }

    /**
     * Generates a list of motion strategies for place operations with Z-axis rotation freedom
     * and tool coordinate system approach.
     * This allows the robot to place workpieces at different rotations around Z-axis
     * and approach perpendicular to the workpiece surface.
     * NOTE: Approach offsets are applied in tool coordinates (negated internally to move away from workpiece).
     * Impedance control is automatically enabled if configured.
     *
     * @param tcp   Tool center point frame to use
     * @param robot Robot instance for redundancy support
     * @return List of motion strategies in priority order
     */
    public static List<MotionStrategy> generatePlaceStrategies(ObjectFrame tcp, LBR robot)
    {
        return generatePlaceStrategies(tcp, robot, true);
    }

    /**
     * Generates a list of motion strategies for place operations with Z-axis rotation freedom
     * and tool coordinate system approach.
     * This allows the robot to place workpieces at different rotations around Z-axis
     * and approach perpendicular to the workpiece surface.
     * NOTE: Approach offsets are applied in tool coordinates (negated internally to move away from workpiece).
     * Impedance control is automatically enabled if configured.
     *
     * @param tcp               Tool center point frame to use
     * @param robot             Robot instance for redundancy support
     * @param allowConfigChange If true, tries multiple Z-rotations and redundancy offsets.
     *                          If false, only tries the default orientation and redundancy.
     * @return List of motion strategies in priority order
     */
    public static List<MotionStrategy> generatePlaceStrategies(ObjectFrame tcp, LBR robot, boolean allowConfigChange)
    {
        return generatePlaceStrategies(tcp, robot, getRedundancyOffsets(), getZRotationAngles(), allowConfigChange);
    }

    /**
     * Generates a list of motion strategies for place operations with custom Z-axis rotation angles
     * and tool coordinate system approach.
     * NOTE: Approach offsets are applied in tool coordinates (negated internally to move away from workpiece).
     * Impedance control is automatically enabled if configured.
     *
     * @param tcp                      Tool center point frame to use
     * @param robot                    Robot instance for redundancy support
     * @param redundancyOffsets        Array of E1 offsets in radians to try
     * @param zRotationAngles          Array of Z-axis rotation angles in radians to try
     * @param allowConfigurationChange If true, tries all combinations of rotations and offsets.
     *                                 If false, only tries the first rotation angle and default redundancy.
     * @return List of motion strategies in priority order
     */
    public static List<MotionStrategy> generatePlaceStrategies(ObjectFrame tcp, LBR robot, double[] redundancyOffsets, double[] zRotationAngles, boolean allowConfigurationChange)
    {
        List<MotionStrategy> strategies = new ArrayList<MotionStrategy>();
        CartesianImpedanceControlMode impedanceMode = getImpedanceMode();

        if (allowConfigurationChange)
        {        // Try each Z-rotation angle with different redundancy configurations
            for (int z = 0; z < zRotationAngles.length; z++)
            {
                Double zAngle = zRotationAngles[z];

                // First attempt without redundancy (null), then with offsets
                strategies.add(new MotionStrategy.Builder(tcp).allowZRotation(true).zRotationAngle(zAngle).useToolCoordinates(true).impedanceMode(impedanceMode).build());
                for (int i = 0; i < redundancyOffsets.length; i++)
                {
                    Double offset = redundancyOffsets[i];
                    strategies.add(new MotionStrategy.Builder(tcp).redundancy(offset, robot).allowZRotation(true).zRotationAngle(zAngle).useToolCoordinates(true).impedanceMode(impedanceMode).build());
                }
            }
        } else
        {
            // Default: Try the first Z-rotation angle (usually 0) without redundancy offset
            Double defaultZAngle = (zRotationAngles.length > 0) ? Double.valueOf(zRotationAngles[0]) : Double.valueOf(0);
            strategies.add(new MotionStrategy.Builder(tcp).allowZRotation(true).zRotationAngle(defaultZAngle).useToolCoordinates(true).impedanceMode(impedanceMode).build());
        }

        return strategies;
    }

    /**
     * Generates a list of motion strategies with tool coordinate system approach
     * but without Z-axis rotation freedom. Use this for pick operations where orientation matters.
     * Strategy order: regular position, then alternate position (180° rotation),
     * each with multiple redundancy configurations.
     * NOTE: Approach offsets are applied in tool coordinates (negated internally to move away from workpiece).
     * Impedance control is automatically enabled if configured.
     *
     * @param tcp   Tool center point frame to use
     * @param robot Robot instance for redundancy support
     * @return List of motion strategies in priority order
     */
    public static List<MotionStrategy> generateStrategiesWithToolCoordinates(ObjectFrame tcp, LBR robot)
    {
        return generateStrategiesWithToolCoordinates(tcp, robot, getRedundancyOffsets());
    }

    /**
     * Generates a list of motion strategies with tool coordinate system approach
     * and custom redundancy variations.
     * NOTE: Approach offsets are applied in tool coordinates (negated internally to move away from workpiece).
     * Impedance control is automatically enabled if configured.
     *
     * @param tcp               Tool center point frame to use
     * @param robot             Robot instance for redundancy support
     * @param redundancyOffsets Array of E1 offsets in radians to try
     * @return List of motion strategies in priority order
     */
    public static List<MotionStrategy> generateStrategiesWithToolCoordinates(ObjectFrame tcp, LBR robot, double[] redundancyOffsets)
    {
        List<MotionStrategy> strategies = new ArrayList<MotionStrategy>();
        CartesianImpedanceControlMode impedanceMode = getImpedanceMode();

        // Try regular position with different redundancy configurations
        // First attempt without redundancy (null), then with offsets
        strategies.add(new MotionStrategy.Builder(tcp).useToolCoordinates(true).impedanceMode(impedanceMode).build());
        for (int i = 0; i < redundancyOffsets.length; i++)
        {
            Double offset = Double.valueOf(redundancyOffsets[i]);
            strategies.add(new MotionStrategy.Builder(tcp).redundancy(offset, robot).useToolCoordinates(true).impedanceMode(impedanceMode).build());
        }

        // Try alternate position (180° rotation) with different redundancy configurations
        // First attempt without redundancy (null), then with offsets
        strategies.add(new MotionStrategy.Builder(tcp).useAlternatePosition(true).useToolCoordinates(true).impedanceMode(impedanceMode).build());
        for (int i = 0; i < redundancyOffsets.length; i++)
        {
            Double offset = Double.valueOf(redundancyOffsets[i]);
            strategies.add(new MotionStrategy.Builder(tcp).useAlternatePosition(true).redundancy(offset, robot).useToolCoordinates(true).impedanceMode(impedanceMode).build());
        }

        return strategies;
    }
}
