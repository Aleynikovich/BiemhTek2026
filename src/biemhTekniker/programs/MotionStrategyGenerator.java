package biemhTekniker.programs;

import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;

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

    /**
     * Generates a list of motion strategies with default redundancy variations.
     * Strategy order: regular position, then alternate position (180° rotation),
     * each with multiple redundancy configurations.
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
     *
     * @param tcp               Tool center point frame to use
     * @param robot             Robot instance for redundancy support
     * @param redundancyOffsets Array of E1 offsets in radians to try
     * @return List of motion strategies in priority order
     */
    public static List<MotionStrategy> generateStrategies(ObjectFrame tcp, LBR robot, double[] redundancyOffsets)
    {
        List<MotionStrategy> strategies = new ArrayList<MotionStrategy>();

        // Try regular position with different redundancy configurations
        // First attempt without redundancy (null), then with offsets
        strategies.add(new MotionStrategy(tcp, false, null, robot));
        for (int i = 0; i < redundancyOffsets.length; i++)
        {
            Double offset = Double.valueOf(redundancyOffsets[i]);
            strategies.add(new MotionStrategy(tcp, false, offset, robot));
        }

        // Try alternate position (180° rotation) with different redundancy configurations
        // First attempt without redundancy (null), then with offsets
        strategies.add(new MotionStrategy(tcp, true, null, robot));
        for (int i = 0; i < redundancyOffsets.length; i++)
        {
            Double offset = Double.valueOf(redundancyOffsets[i]);
            strategies.add(new MotionStrategy(tcp, true, offset, robot));
        }

        return strategies;
    }

    /**
     * Generates a simplified list of motion strategies without alternate position.
     * Only tries regular position with redundancy variations.
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
     *
     * @param tcp               Tool center point frame to use
     * @param robot             Robot instance for redundancy support
     * @param redundancyOffsets Array of E1 offsets in radians to try
     * @return List of motion strategies in priority order
     */
    public static List<MotionStrategy> generateStrategiesWithoutAlternate(ObjectFrame tcp, LBR robot, double[] redundancyOffsets)
    {
        List<MotionStrategy> strategies = new ArrayList<MotionStrategy>();

        // Try regular position with different redundancy configurations
        strategies.add(new MotionStrategy(tcp, false, null, robot));
        for (int i = 0; i < redundancyOffsets.length; i++)
        {
            Double offset = Double.valueOf(redundancyOffsets[i]);
            strategies.add(new MotionStrategy(tcp, false, offset, robot));
        }

        return strategies;
    }
}
