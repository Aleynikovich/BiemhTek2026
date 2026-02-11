package biemhTekniker.programs.vision;

import biemhTekniker.lib.data.WorkpieceQueue;
import biemhTekniker.lib.vision.SmartPickingProtocol;

/**
 * Context object providing dependencies for vision tasks.
 * Holds references to vision protocol and shared data structures.
 */
public class VisionContext
{
    private final SmartPickingProtocol protocol;
    private final WorkpieceQueue workpieceQueue;

    /**
     * Creates a new vision context.
     *
     * @param protocol       SmartPicking protocol for camera communication
     * @param workpieceQueue Shared workpiece queue
     */
    public VisionContext(SmartPickingProtocol protocol, WorkpieceQueue workpieceQueue)
    {
        this.protocol = protocol;
        this.workpieceQueue = workpieceQueue;
    }

    public SmartPickingProtocol getProtocol()
    {
        return protocol;
    }

    public WorkpieceQueue getWorkpieceQueue()
    {
        return workpieceQueue;
    }
}
