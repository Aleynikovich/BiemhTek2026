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
}
