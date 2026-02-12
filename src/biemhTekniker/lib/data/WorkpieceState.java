package biemhTekniker.lib.data;

/**
 * Enum representing the lifecycle state of a workpiece.
 */
public enum WorkpieceState
{
    /**
     * Camera found the workpiece, ready to be picked.
     * Also used when workpiece is returned to table.
     */
    AVAILABLE,

    /**
     * Workpiece has been placed on measuring machine.
     */
    MEASURING,

    /**
     * Measuring is complete, ready to be removed from machine.
     */
    MEASURED,

    /**
     * Workpiece picked and not available.
     */
    PICKED,


}
