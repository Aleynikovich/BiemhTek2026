package biemhTekniker;

import com.kuka.roboticsAPI.geometricModel.Frame;

/**
 * Data class representing a frame received from the vision system.
 * Parses incoming string data into structured coordinates.
 */
public class VisionFrame
{

    private String operationType;
    private String timestamp;
    private double x;
    private double y;
    private double z;
    private double alpha;
    private double beta;
    private double gamma;
    private boolean valid;

    public VisionFrame()
    {
        this.valid = false;
    }

    /**
     * Parse vision system data string into frame components.
     * Expected format: "type,timestamp,reserved,reserved,reserved,x,y,z,gamma,beta,alpha"
     */
    public boolean parseFromString(String datagram, String delimiter)
    {
        if (datagram == null || datagram.isEmpty())
        {
            this.valid = false;
            return false;
        }

        try
        {
            String[] tokens = datagram.split(delimiter);

            if (tokens.length < 3)
            {
                this.valid = false;
                return false;
            }

            this.operationType = tokens[0];

            if (tokens.length >= 8)
            {
                this.x = Double.parseDouble(tokens[5]);
                this.y = Double.parseDouble(tokens[6]);
                this.z = Double.parseDouble(tokens[7]);

                if (tokens.length >= 11)
                {
                    this.gamma = Double.parseDouble(tokens[8]);
                    this.beta = Double.parseDouble(tokens[9]);
                    this.alpha = Double.parseDouble(tokens[10]);
                } else
                {
                    this.gamma = 0.0;
                    this.beta = 0.0;
                    this.alpha = 0.0;
                }
            }

            this.valid = true;
            return true;

        } catch (NumberFormatException e)
        {
            this.valid = false;
            return false;
        } catch (ArrayIndexOutOfBoundsException e)
        {
            this.valid = false;
            return false;
        }
    }

    public Frame toKukaFrame(Frame baseFrame)
    {
        Frame result = new Frame(baseFrame);
        if (this.valid)
        {
            result.setX(this.x);
            result.setY(this.y);
            result.setZ(this.z);
            result.setAlphaRad(this.alpha);
            result.setBetaRad(this.beta);
            result.setGammaRad(this.gamma);
        }
        return result;
    }

    public String getOperationType()
    {
        return operationType;
    }

    public String getTimestamp()
    {
        return timestamp;
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

    public double getAlpha()
    {
        return alpha;
    }

    public double getBeta()
    {
        return beta;
    }

    public double getGamma()
    {
        return gamma;
    }

    public boolean isValid()
    {
        return valid;
    }

    public String toString()
    {
        return "VisionFrame[type=" + operationType +
                ", x=" + x + ", y=" + y + ", z=" + z +
                ", alpha=" + alpha + ", beta=" + beta + ", gamma=" + gamma +
                ", valid=" + valid + "]";
    }
}
