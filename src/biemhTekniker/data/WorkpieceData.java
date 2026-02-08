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

    @Override
    public synchronized String toString()
    {
        if (!valid)
        {
            return "WorkpieceData{id=" + id + ", invalid}";
        }
        return String.format("WorkpieceData{id=%d, ref=%d, state=%s, x=%.1f, y=%.1f, z=%.1f, rz(A)=%.1f, ry(B)=%.1f, rx(C)=%.1f, score=%.2f}", id, referenceIndex, state, x, y, z, rx, ry, rz, score);
    }
}
