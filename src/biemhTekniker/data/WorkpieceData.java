package biemhTekniker.data;

import com.kuka.roboticsAPI.geometricModel.Frame;

/**
 * Data class to store workpiece position and orientation.
 * Shared across programs for pick and place operations.
 */
public class WorkpieceData
{

    private double x;
    private double y;
    private double z;
    private double rx;
    private double ry;
    private double rz;
    private double score;
    private boolean valid;

    public WorkpieceData()
    {
        this.valid = false;
    }

    public WorkpieceData(double x, double y, double z, double rx, double ry, double rz, double score)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
        this.score = score;
        this.valid = true;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    public double getRx()
    {
        return rx;
    }

    public double getRy()
    {
        return ry;
    }

    public double getRz()
    {
        return rz;
    }

    public double getScore()
    {
        return score;
    }

    public boolean isValid()
    {
        return valid;
    }


    public void set(double x, double y, double z, double rx, double ry, double rz, double score)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
        this.score = score;
        this.valid = true;
    }

    public Frame getWorkPiecePickFrame()
    {
        Frame workPiecePickFrame;
        workPiecePickFrame = new Frame(x, y, z, Math.toRadians(rz), Math.toRadians(ry), Math.toRadians(rx));
        return workPiecePickFrame;
    }

    public void invalidate()
    {
        this.valid = false;
    }

    @Override
    public String toString()
    {
        if (!valid)
        {
            return "WorkpieceData{invalid}";
        }
        return String.format("WorkpieceData{x=%.1f, y=%.1f, z=%.1f, rz(A)=%.1f, ry(B)=%.1f, rx(C)=%.1f, score=%.1f}", x, y, z, rx, ry, rz, score);
    }
}
