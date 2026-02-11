package biemhTekniker.data;

import com.kuka.roboticsAPI.geometricModel.Frame;

/**
 * Data class to store workpiece position and orientation.
 * Thread-safe with synchronized accessors.
 */
public class WorkpieceData
{
    private static long idCounter = 0;

    private final long id;
    private int referenceIndex;
    private WorkpieceState state;

    private double x;
    private double y;
    private double z;
    private double rx;
    private double ry;
    private double rz;
    private double score;
    private boolean valid;
    private int orientation; // 0 = regular, 1 = inverted
    private String gripperLocation; // "A", "B", or null if not picked

    // Origin position (where workpiece was first found)
    private double originX;
    private double originY;
    private double originZ;
    private double originRx;
    private double originRy;
    private double originRz;

    public WorkpieceData()
    {
        this.id = generateId();
        this.valid = false;
        this.state = WorkpieceState.AVAILABLE;
        this.referenceIndex = 1;
    }

    public WorkpieceData(double x, double y, double z, double rx, double ry, double rz, double score)
    {
        this.id = generateId();
        this.x = x;
        this.y = y;
        this.z = z;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
        this.score = score;
        this.valid = true;
        this.state = WorkpieceState.AVAILABLE;
        this.referenceIndex = 1;

        // Save origin position
        this.originX = x;
        this.originY = y;
        this.originZ = z;
        this.originRx = rx;
        this.originRy = ry;
        this.originRz = rz;
    }

    /**
     * Generates a unique ID using timestamp and counter.
     * Thread-safe.
     */
    private static synchronized long generateId()
    {
        return System.currentTimeMillis() + (idCounter++);
    }

    public synchronized long getId()
    {
        return id;
    }

    public synchronized int getReferenceIndex()
    {
        return referenceIndex;
    }

    public synchronized void setReferenceIndex(int referenceIndex)
    {
        this.referenceIndex = referenceIndex;
    }

    public synchronized WorkpieceState getState()
    {
        return state;
    }

    public synchronized void setState(WorkpieceState state)
    {
        this.state = state;
    }

    public synchronized double getX()
    {
        return x;
    }

    public synchronized double getY()
    {
        return y;
    }

    public synchronized double getZ()
    {
        return z;
    }

    public synchronized double getRx()
    {
        return rx;
    }

    public synchronized double getRy()
    {
        return ry;
    }

    public synchronized double getRz()
    {
        return rz;
    }

    public synchronized double getScore()
    {
        return score;
    }

    public synchronized boolean isValid()
    {
        return valid;
    }

    public synchronized void set(double x, double y, double z, double rx, double ry, double rz, double score)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
        this.score = score;
        this.valid = true;

        // Save origin position
        this.originX = x;
        this.originY = y;
        this.originZ = z;
        this.originRx = rx;
        this.originRy = ry;
        this.originRz = rz;
    }

    public synchronized Frame getWorkPiecePickFrame()
    {
        Frame workPiecePickFrame;
        workPiecePickFrame = new Frame(x, y, z, Math.toRadians(rz), Math.toRadians(ry), Math.toRadians(rx));
        return workPiecePickFrame;
    }

    /**
     * Returns a Frame for the origin position (where the workpiece was first found).
     * Used to return workpiece to the same spot after measuring.
     *
     * @return Frame built from origin coordinates
     */
    public synchronized Frame getReturnFrame()
    {
        Frame returnFrame;
        returnFrame = new Frame(originX, originY, originZ, Math.toRadians(originRz), Math.toRadians(originRy), Math.toRadians(originRx));
        return returnFrame;
    }

    public synchronized void invalidate()
    {
        this.valid = false;
    }

    /**
     * Gets the workpiece orientation.
     *
     * @return 0 for regular, 1 for inverted
     */
    public synchronized int getOrientation()
    {
        return orientation;
    }

    /**
     * Sets the workpiece orientation.
     *
     * @param orientation 0 for regular, 1 for inverted
     */
    public synchronized void setOrientation(int orientation)
    {
        this.orientation = orientation;
    }

    /**
     * Gets the gripper location where the workpiece is held.
     *
     * @return "A", "B", or null if not in a gripper
     */
    public synchronized String getGripperLocation()
    {
        return gripperLocation;
    }

    /**
     * Sets the gripper location where the workpiece is held.
     *
     * @param gripperLocation "A", "B", or null
     */
    public synchronized void setGripperLocation(String gripperLocation)
    {
        this.gripperLocation = gripperLocation;
    }

    /**
     * Gets the origin position of the workpiece.
     *
     * @return Array [x, y, z, rx, ry, rz]
     */
    public synchronized double[] getOriginPosition()
    {
        return new double[]{originX, originY, originZ, originRx, originRy, originRz};
    }

    /**
     * Gets the reference string in "xy" format where:
     * x = reference number (1, 2, 3)
     * y = orientation (0=regular, 1=180° rotation)
     * 
     * Examples:
     * - Reference 1, regular: "10"
     * - Reference 1, 180° rotation: "11"
     * - Reference 2, regular: "20"
     * - Reference 3, 180° rotation: "31"
     *
     * @return Reference string in "xy" format
     */
    public synchronized String getReferenceString()
    {
        return String.valueOf(referenceIndex) + String.valueOf(orientation);
    }

    /**
     * Checks if this workpiece is at approximately the same position as given coordinates.
     * Uses a tolerance of ±5mm for position matching.
     *
     * @param checkX X coordinate to check
     * @param checkY Y coordinate to check
     * @param checkZ Z coordinate to check
     * @param tolerance Tolerance in mm (default 5.0)
     * @return true if position matches within tolerance
     */
    public synchronized boolean isAtPosition(double checkX, double checkY, double checkZ, double tolerance)
    {
        double dx = Math.abs(x - checkX);
        double dy = Math.abs(y - checkY);
        double dz = Math.abs(z - checkZ);
        return (dx <= tolerance && dy <= tolerance && dz <= tolerance);
    }

    @Override
    public synchronized String toString()
    {
        if (!valid)
        {
            return "WorkpieceData{id=" + id + ", invalid}";
        }
        String gripperStr = (gripperLocation != null) ? ", gripper=" + gripperLocation : "";
        return String.format("WorkpieceData{id=%d, state=%s, ref=%s (idx=%d, ori=%d)%s, x=%.1f, y=%.1f, z=%.1f, rz(A)=%.1f, ry(B)=%.1f, rx(C)=%.1f, score=%.2f}", 
            id, state, getReferenceString(), referenceIndex, orientation, gripperStr, x, y, z, rx, ry, rz, score);
    }
}
