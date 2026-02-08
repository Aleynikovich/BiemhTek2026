package biemhTekniker.data;

/**
 * Enum representing the lifecycle state of a workpiece.
 */
public enum WorkpieceState
{
    /**
     * Camera found the workpiece, ready to be picked.
     */
    AVAILABLE,

    /**
     * Robot has picked up the workpiece.
     */
    PICKED,

    /**
     * Workpiece has been placed on measuring machine.
     */
    MEASURING,

    /**
     * Measuring is complete, ready to be removed from machine.
     */
    MEASURED,

    /**
     * Workpiece has been returned to bin at its origin position.
     */
    RETURNED
}
