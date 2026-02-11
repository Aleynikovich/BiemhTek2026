package biemhTekniker.lib.robot.motions;

/**
 * Runtime overrides for motion generation and program flow, controlled via console commands.
 */
public final class MotionOverrides
{
    private static volatile Long forcedWorkpieceId = null;
    
    // Pick motion overrides
    private static volatile double[] pickRedundancyOverride = null;     // radians
    private static volatile boolean pickAlternateOnly = false;
    
    // Place motion overrides
    private static volatile double[] placeRedundancyOverride = null;    // radians
    private static volatile double[] placeZRotOverride = null;          // radians
    private static volatile boolean placeAlternateOnly = false;

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

    // ----- Pick redundancy offsets override (radians) -----
    public static void setPickRedundancyOverride(double[] offsetsRad)
    {
        pickRedundancyOverride = offsetsRad;
    }

    public static double[] getPickRedundancyOverride()
    {
        return pickRedundancyOverride;
    }

    // ----- Pick alternate position only flag -----
    public static void setPickAlternateOnly(boolean alternateOnly)
    {
        pickAlternateOnly = alternateOnly;
    }

    public static boolean isPickAlternateOnly()
    {
        return pickAlternateOnly;
    }

    // ----- Place redundancy offsets override (radians) -----
    public static void setPlaceRedundancyOverride(double[] offsetsRad)
    {
        placeRedundancyOverride = offsetsRad;
    }

    public static double[] getPlaceRedundancyOverride()
    {
        return placeRedundancyOverride;
    }

    // ----- Place Z rotation angles override (radians) -----
    public static void setPlaceZRotOverride(double[] anglesRad)
    {
        placeZRotOverride = anglesRad;
    }

    public static double[] getPlaceZRotOverride()
    {
        return placeZRotOverride;
    }

    // ----- Place alternate position only flag -----
    public static void setPlaceAlternateOnly(boolean alternateOnly)
    {
        placeAlternateOnly = alternateOnly;
    }

    public static boolean isPlaceAlternateOnly()
    {
        return placeAlternateOnly;
    }

    // ----- Clear all motion overrides -----
    public static void clearMotionOverrides()
    {
        pickRedundancyOverride = null;
        pickAlternateOnly = false;
        placeRedundancyOverride = null;
        placeZRotOverride = null;
        placeAlternateOnly = false;
    }
}
