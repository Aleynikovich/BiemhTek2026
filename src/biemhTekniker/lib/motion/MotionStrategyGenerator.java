package biemhTekniker.lib.motion;

import biemhTekniker.config.ImpedanceConfig;
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
    // Default redundancy E1 offsets to try (in radians)
    private static final double[] DEFAULT_REDUNDANCY_OFFSETS = new double[] {
        Math.toRadians(-80),          // -80 degrees
        Math.toRadians(80),           // +80 degrees
        Math.toRadians(-60),          // -60 degrees
        Math.toRadians(60)            // +60 degrees
    };

    // Z-axis rotation angles to try for place operations (in radians)
    private static final double[] DEFAULT_Z_ROTATION_ANGLES = new double[] {
        0,                            // 0 degrees (no rotation)
        Math.toRadians(45),          // 45 degrees
        Math.toRadians(90),          // 90 degrees
        Math.toRadians(135),         // 135 degrees
        Math.toRadians(180),         // 180 degrees
        Math.toRadians(-45),         // -45 degrees
        Math.toRadians(-90),         // -90 degrees
        Math.toRadians(-135)         // -135 degrees
    };

    // Singleton impedance configuration instance
    private static ImpedanceConfig impedanceConfig = null;

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
     * 
     * @return CartesianImpedanceControlMode or null if disabled
     */
    private static CartesianImpedanceControlMode getImpedanceMode()
    {
        return getImpedanceConfig().createControlMode();
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
        return generateStrategies(tcp, robot, DEFAULT_REDUNDANCY_OFFSETS);
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
        List<MotionStrategy> strategies = new ArrayList<MotionStrategy>();
        CartesianImpedanceControlMode impedanceMode = getImpedanceMode();

        // Try regular position with different redundancy configurations
        // First attempt without redundancy (null), then with offsets
        strategies.add(new MotionStrategy(tcp, false, null, robot, false, null, false, impedanceMode));
        for (int i = 0; i < redundancyOffsets.length; i++)
        {
            Double offset = Double.valueOf(redundancyOffsets[i]);
            strategies.add(new MotionStrategy(tcp, false, offset, robot, false, null, false, impedanceMode));
        }

        // Try alternate position (180° rotation) with different redundancy configurations
        // First attempt without redundancy (null), then with offsets
        strategies.add(new MotionStrategy(tcp, true, null, robot, false, null, false, impedanceMode));
        for (int i = 0; i < redundancyOffsets.length; i++)
        {
            Double offset = Double.valueOf(redundancyOffsets[i]);
            strategies.add(new MotionStrategy(tcp, true, offset, robot, false, null, false, impedanceMode));
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
        return generateStrategiesWithoutAlternate(tcp, robot, DEFAULT_REDUNDANCY_OFFSETS);
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
        strategies.add(new MotionStrategy(tcp, false, null, robot, false, null, false, impedanceMode));
        for (int i = 0; i < redundancyOffsets.length; i++)
        {
            Double offset = Double.valueOf(redundancyOffsets[i]);
            strategies.add(new MotionStrategy(tcp, false, offset, robot, false, null, false, impedanceMode));
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
        return generatePlaceStrategies(tcp, robot, DEFAULT_REDUNDANCY_OFFSETS, DEFAULT_Z_ROTATION_ANGLES, false);
    }

    /**
     * Generates a list of motion strategies for place operations with Z-axis rotation freedom,
     * tool coordinate system approach, and option to force linear approach.
     * NOTE: Approach offsets are applied in tool coordinates (negated internally to move away from workpiece).
     * Impedance control is automatically enabled if configured.
     *
     * @param tcp                 Tool center point frame to use
     * @param robot               Robot instance for redundancy support
     * @param forceLinealApproach If true, use linear motion for approach instead of PTP
     * @return List of motion strategies in priority order
     */
    public static List<MotionStrategy> generatePlaceStrategies(ObjectFrame tcp, LBR robot, boolean forceLinealApproach)
    {
        return generatePlaceStrategies(tcp, robot, DEFAULT_REDUNDANCY_OFFSETS, DEFAULT_Z_ROTATION_ANGLES, forceLinealApproach);
    }

    /**
     * Generates a list of motion strategies for place operations with custom Z-axis rotation angles
     * and tool coordinate system approach.
     * NOTE: Approach offsets are applied in tool coordinates (negated internally to move away from workpiece).
     * Impedance control is automatically enabled if configured.
     *
     * @param tcp                Tool center point frame to use
     * @param robot              Robot instance for redundancy support
     * @param redundancyOffsets  Array of E1 offsets in radians to try
     * @param zRotationAngles    Array of Z-axis rotation angles in radians to try
     * @return List of motion strategies in priority order
     */
    public static List<MotionStrategy> generatePlaceStrategies(ObjectFrame tcp, LBR robot, 
                                                               double[] redundancyOffsets, 
                                                               double[] zRotationAngles)
    {
        return generatePlaceStrategies(tcp, robot, redundancyOffsets, zRotationAngles, false);
    }

    /**
     * Generates a list of motion strategies for place operations with custom Z-axis rotation angles,
     * tool coordinate system approach, and option to force linear approach.
     * NOTE: Approach offsets are applied in tool coordinates (negated internally to move away from workpiece).
     * Impedance control is automatically enabled if configured.
     *
     * @param tcp                 Tool center point frame to use
     * @param robot               Robot instance for redundancy support
     * @param redundancyOffsets   Array of E1 offsets in radians to try
     * @param zRotationAngles     Array of Z-axis rotation angles in radians to try
     * @param forceLinealApproach If true, use linear motion for approach instead of PTP
     * @return List of motion strategies in priority order
     */
    public static List<MotionStrategy> generatePlaceStrategies(ObjectFrame tcp, LBR robot, 
                                                               double[] redundancyOffsets, 
                                                               double[] zRotationAngles,
                                                               boolean forceLinealApproach)
    {
        List<MotionStrategy> strategies = new ArrayList<MotionStrategy>();
        CartesianImpedanceControlMode impedanceMode = getImpedanceMode();

        // Try each Z-rotation angle with different redundancy configurations
        for (int z = 0; z < zRotationAngles.length; z++)
        {
            Double zAngle = Double.valueOf(zRotationAngles[z]);
            
            // First attempt without redundancy (null), then with offsets
            strategies.add(new MotionStrategy(tcp, false, null, robot, true, zAngle, true, impedanceMode, forceLinealApproach));
            for (int i = 0; i < redundancyOffsets.length; i++)
            {
                Double offset = Double.valueOf(redundancyOffsets[i]);
                strategies.add(new MotionStrategy(tcp, false, offset, robot, true, zAngle, true, impedanceMode, forceLinealApproach));
            }
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
        return generateStrategiesWithToolCoordinates(tcp, robot, DEFAULT_REDUNDANCY_OFFSETS, false);
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
        return generateStrategiesWithToolCoordinates(tcp, robot, redundancyOffsets, false);
    }

    /**
     * Generates a list of motion strategies with tool coordinate system approach
     * and option to force linear approach motion.
     * NOTE: Approach offsets are applied in tool coordinates (negated internally to move away from workpiece).
     * Impedance control is automatically enabled if configured.
     *
     * @param tcp                 Tool center point frame to use
     * @param robot               Robot instance for redundancy support
     * @param forceLinealApproach If true, use linear motion for approach instead of PTP
     * @return List of motion strategies in priority order
     */
    public static List<MotionStrategy> generateStrategiesWithToolCoordinates(ObjectFrame tcp, LBR robot, boolean forceLinealApproach)
    {
        return generateStrategiesWithToolCoordinates(tcp, robot, DEFAULT_REDUNDANCY_OFFSETS, forceLinealApproach);
    }

    /**
     * Generates a list of motion strategies with tool coordinate system approach,
     * custom redundancy variations, and option to force linear approach motion.
     * NOTE: Approach offsets are applied in tool coordinates (negated internally to move away from workpiece).
     * Impedance control is automatically enabled if configured.
     *
     * @param tcp                 Tool center point frame to use
     * @param robot               Robot instance for redundancy support
     * @param redundancyOffsets   Array of E1 offsets in radians to try
     * @param forceLinealApproach If true, use linear motion for approach instead of PTP
     * @return List of motion strategies in priority order
     */
    public static List<MotionStrategy> generateStrategiesWithToolCoordinates(ObjectFrame tcp, LBR robot, double[] redundancyOffsets, boolean forceLinealApproach)
    {
        List<MotionStrategy> strategies = new ArrayList<MotionStrategy>();
        CartesianImpedanceControlMode impedanceMode = getImpedanceMode();

        // Try regular position with different redundancy configurations
        // First attempt without redundancy (null), then with offsets
        strategies.add(new MotionStrategy(tcp, false, null, robot, false, null, true, impedanceMode, forceLinealApproach));
        for (int i = 0; i < redundancyOffsets.length; i++)
        {
            Double offset = Double.valueOf(redundancyOffsets[i]);
            strategies.add(new MotionStrategy(tcp, false, offset, robot, false, null, true, impedanceMode, forceLinealApproach));
        }

        // Try alternate position (180° rotation) with different redundancy configurations
        // First attempt without redundancy (null), then with offsets
        strategies.add(new MotionStrategy(tcp, true, null, robot, false, null, true, impedanceMode, forceLinealApproach));
        for (int i = 0; i < redundancyOffsets.length; i++)
        {
            Double offset = Double.valueOf(redundancyOffsets[i]);
            strategies.add(new MotionStrategy(tcp, true, offset, robot, false, null, true, impedanceMode, forceLinealApproach));
        }

        return strategies;
    }
}
