package biemhTekniker.lib.robot.motions;

/**
 * Runtime overrides for motion generation and program flow, controlled via console commands.
 */
public final class MotionOverrides
{
    private static volatile Long forcedWorkpieceId = null;
    private static volatile double[] redundancyOffsetsOverride = null; // radians
    private static volatile double[] zRotationAnglesOverride = null;   // radians

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

    // ----- Redundancy offsets override (radians) -----
    public static void setRedundancyOffsetsOverride(double[] offsetsRad)
    {
        redundancyOffsetsOverride = offsetsRad;
    }

    public static double[] getForcedRedundancyOffsets()
    {
        return redundancyOffsetsOverride;
    }

    // ----- Z rotation angles override (radians) -----
    public static void setZRotationAnglesOverride(double[] anglesRad)
    {
        zRotationAnglesOverride = anglesRad;
    }

    public static double[] getForcedZRotationAngles()
    {
        return zRotationAnglesOverride;
    }

    public static void clearMotionOverrides()
    {
        redundancyOffsetsOverride = null;
        zRotationAnglesOverride = null;
    }
}
