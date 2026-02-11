package biemhTekniker.console;

/**
 * Interface for console server to communicate with Main application.
 */
public interface ConsoleServerInterface
{

    /**
     * Set the program number.
     *
     * @param programNumber Program number to execute (0-199)
     */
    void setProgramNumber(int programNumber);

    /**
     * Get the current program number.
     *
     * @return Current program number
     */
    int getCurrentProgram();

    /**
     * Check if vision server is connected.
     *
     * @return true if connected, false otherwise
     */
    boolean isVisionConnected();

    /**
     * Get the current workpiece position as string.
     *
     * @return Workpiece position string or status message
     */
    String getWorkpiecePosition();

    /**
     * Get the formatted queue status.
     *
     * @return Queue status string
     */
    String getQueueStatus();

    /**
     * Check if any console client is currently connected.
     *
     * @return true if at least one client is connected
     */
    boolean hasActiveClients();

    /**
     * Cancels the currently executing program and requests return to home position.
     * Does not open grippers to preserve any held workpiece.
     */
    void cancelCurrentProgram();
    
    /**
     * Get the workpiece queue data as JSON string.
     *
     * @return JSON array of workpiece data
     */
    String getWorkpiecesJson();
    
    /**
     * Clears all workpieces from the queue.
     */
    void clearWorkpieceQueue();
    
    /**
     * Removes a specific workpiece from the queue by ID.
     * 
     * @param workpieceId ID of the workpiece to remove
     * @return true if workpiece was found and removed, false otherwise
     */
    boolean removeWorkpiece(long workpieceId);

    // --- Gripper state reporting (open/closed) ---
    boolean isGripper1Closed();

    boolean isGripper2Closed();

    boolean isGripper3Closed();
}
