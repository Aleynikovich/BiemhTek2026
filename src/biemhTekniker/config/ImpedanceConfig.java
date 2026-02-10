package biemhTekniker.config;

import biemhTekniker.logger.Logger;
import com.kuka.roboticsAPI.geometricModel.CartDOF;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;

/**
 * Configuration helper for Cartesian impedance control mode.
 * Provides compliant robot behavior for safe operation in collaborative environments.
 * 
 * Impedance control allows the robot to yield to external forces, making it safer
 * when interacting with grippers, workpieces, and other equipment.
 */
public class ImpedanceConfig
{
    private static final Logger log = Logger.getLogger(ImpedanceConfig.class);
    
    // Default values from KUKA documentation
    private static final double DEFAULT_STIFFNESS_X = 1000.0; // N/m (more compliant than default 2000)
    private static final double DEFAULT_STIFFNESS_Y = 1000.0; // N/m
    private static final double DEFAULT_STIFFNESS_Z = 800.0;  // N/m (lowest for Z-axis compliance)
    private static final double DEFAULT_STIFFNESS_A = 150.0;  // Nm/rad
    private static final double DEFAULT_STIFFNESS_B = 150.0;  // Nm/rad
    private static final double DEFAULT_STIFFNESS_C = 150.0;  // Nm/rad
    private static final double DEFAULT_DAMPING = 0.7;        // dimensionless
    private static final boolean DEFAULT_ENABLED = true;
    
    private final boolean enabled;
    private final double stiffnessX;
    private final double stiffnessY;
    private final double stiffnessZ;
    private final double stiffnessA;
    private final double stiffnessB;
    private final double stiffnessC;
    private final double damping;
    
    /**
     * Creates impedance configuration from ConfigManager properties.
     */
    public ImpedanceConfig()
    {
        ConfigManager config = ConfigManager.getInstance();
        
        this.enabled = config.getBoolean("impedance.enabled", DEFAULT_ENABLED);
        this.stiffnessX = config.getDouble("impedance.stiffness.x", DEFAULT_STIFFNESS_X);
        this.stiffnessY = config.getDouble("impedance.stiffness.y", DEFAULT_STIFFNESS_Y);
        this.stiffnessZ = config.getDouble("impedance.stiffness.z", DEFAULT_STIFFNESS_Z);
        this.stiffnessA = config.getDouble("impedance.stiffness.a", DEFAULT_STIFFNESS_A);
        this.stiffnessB = config.getDouble("impedance.stiffness.b", DEFAULT_STIFFNESS_B);
        this.stiffnessC = config.getDouble("impedance.stiffness.c", DEFAULT_STIFFNESS_C);
        this.damping = config.getDouble("impedance.damping", DEFAULT_DAMPING);
        
        log.info("Impedance control " + (enabled ? "ENABLED" : "DISABLED"));
        if (enabled)
        {
            log.info("  Stiffness: X=" + stiffnessX + " Y=" + stiffnessY + " Z=" + stiffnessZ 
                    + " A=" + stiffnessA + " B=" + stiffnessB + " C=" + stiffnessC);
            log.info("  Damping: " + damping);
        }
    }
    
    /**
     * Creates a CartesianImpedanceControlMode with configured parameters.
     * 
     * @return Configured impedance control mode, or null if disabled
     */
    public CartesianImpedanceControlMode createControlMode()
    {
        if (!enabled)
        {
            return null;
        }
        
        CartesianImpedanceControlMode mode = new CartesianImpedanceControlMode();
        
        // Set stiffness using parametrize() method (preferred over deprecated setStiffness)
        mode.parametrize(CartDOF.X).setStiffness(stiffnessX).setDamping(damping);
        mode.parametrize(CartDOF.Y).setStiffness(stiffnessY).setDamping(damping);
        mode.parametrize(CartDOF.Z).setStiffness(stiffnessZ).setDamping(damping);
        mode.parametrize(CartDOF.A).setStiffness(stiffnessA).setDamping(damping);
        mode.parametrize(CartDOF.B).setStiffness(stiffnessB).setDamping(damping);
        mode.parametrize(CartDOF.C).setStiffness(stiffnessC).setDamping(damping);
        
        return mode;
    }
    
    /**
     * Checks if impedance control is enabled.
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled()
    {
        return enabled;
    }
    
    /**
     * Gets stiffness value for X axis.
     * @return Stiffness in N/m
     */
    public double getStiffnessX()
    {
        return stiffnessX;
    }
    
    /**
     * Gets stiffness value for Y axis.
     * @return Stiffness in N/m
     */
    public double getStiffnessY()
    {
        return stiffnessY;
    }
    
    /**
     * Gets stiffness value for Z axis.
     * @return Stiffness in N/m
     */
    public double getStiffnessZ()
    {
        return stiffnessZ;
    }
    
    /**
     * Gets stiffness value for A rotation.
     * @return Stiffness in Nm/rad
     */
    public double getStiffnessA()
    {
        return stiffnessA;
    }
    
    /**
     * Gets stiffness value for B rotation.
     * @return Stiffness in Nm/rad
     */
    public double getStiffnessB()
    {
        return stiffnessB;
    }
    
    /**
     * Gets stiffness value for C rotation.
     * @return Stiffness in Nm/rad
     */
    public double getStiffnessC()
    {
        return stiffnessC;
    }
    
    /**
     * Gets damping value.
     * @return Damping (dimensionless)
     */
    public double getDamping()
    {
        return damping;
    }
}
