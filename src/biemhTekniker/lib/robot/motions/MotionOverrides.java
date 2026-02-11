package biemhTekniker.lib.robot.motions;

/**
 * Runtime overrides for motion generation and program flow, controlled via console commands.
 * When override is enabled, the robot tests exactly ONE specific motion configuration
 * instead of generating fallback lists.
 */
public final class MotionOverrides
{
    private static volatile Long forcedWorkpieceId = null;
    
    // Override enable flag
    private static volatile boolean overrideEnabled = false;
    
    // Pick overrides (single values)
    private static volatile Double pickRedundancyE1 = null;  // single E1 offset in radians, null = no redundancy
    private static volatile boolean pickAlternate = false;   // true = alternate (180°), false = regular
    
    // Place overrides (single values)
    private static volatile Double placeRedundancyE1 = null; // single E1 offset in radians, null = no redundancy
    private static volatile Double placeZRotation = null;    // single Z rotation in radians

    private MotionOverrides()
    {
    }

    // ----- Forced workpiece selection -----
    public static void setForcedWorkpieceId(Long id)
    {
        forcedWorkpieceId = id;
    }

    public static Long consumeForcedWorkpieceId()
    {
        Long id = forcedWorkpieceId;
        forcedWorkpieceId = null;
        return id;
    }

    // ----- Override enabled flag -----
    public static void setOverrideEnabled(boolean enabled)
    {
        overrideEnabled = enabled;
    }
    
    public static boolean isOverrideEnabled()
    {
        return overrideEnabled;
    }

    // ----- Pick overrides -----
    public static void setPickRedundancyE1(Double offsetRad)
    {
        pickRedundancyE1 = offsetRad;
    }
    
    public static Double getPickRedundancyE1()
    {
        return pickRedundancyE1;
    }
    
    public static void setPickAlternate(boolean alternate)
    {
        pickAlternate = alternate;
    }
    
    public static boolean isPickAlternate()
    {
        return pickAlternate;
    }

    // ----- Place overrides -----
    public static void setPlaceRedundancyE1(Double offsetRad)
    {
        placeRedundancyE1 = offsetRad;
    }
    
    public static Double getPlaceRedundancyE1()
    {
        return placeRedundancyE1;
    }
    
    public static void setPlaceZRotation(Double angleRad)
    {
        placeZRotation = angleRad;
    }
    
    public static Double getPlaceZRotation()
    {
        return placeZRotation;
    }

    // ----- Clear all overrides -----
    public static void clearMotionOverrides()
    {
        overrideEnabled = false;
        pickRedundancyE1 = null;
        pickAlternate = false;
        placeRedundancyE1 = null;
        placeZRotation = null;
    }
}
