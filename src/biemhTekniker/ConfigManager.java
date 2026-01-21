package biemhTekniker;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Singleton configuration manager for loading robot and PLC properties.
 */
public class ConfigManager
{

    private static ConfigManager instance;
    private final Properties robotConfig;
    private final Properties plcConfig;
    private String configBasePath;

    private ConfigManager()
    {
        this.robotConfig = new Properties();
        this.plcConfig = new Properties();
        this.configBasePath = "C:/KRC/Projects/BiemhTek2026/configs";
    }

    public static synchronized ConfigManager getInstance()
    {
        if (instance == null)
        {
            instance = new ConfigManager();
        }
        return instance;
    }

    public void setConfigBasePath(String path)
    {
        this.configBasePath = path;
    }

    public void loadRobotConfig() throws IOException
    {
        String filePath = configBasePath + "robot.properties";
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(filePath);
            robotConfig.load(fis);
        } finally
        {
            if (fis != null)
            {
                fis.close();
            }
        }
    }

    public void loadPlcConfig() throws IOException
    {
        String filePath = configBasePath + "plc.properties";
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(filePath);
            plcConfig.load(fis);
        } finally
        {
            if (fis != null)
            {
                fis.close();
            }
        }
    }

    public String getRobotProperty(String key, String defaultValue)
    {
        return robotConfig.getProperty(key, defaultValue);
    }

    public String getPlcProperty(String key, String defaultValue)
    {
        return plcConfig.getProperty(key, defaultValue);
    }

    public int getRobotPropertyInt(String key, int defaultValue)
    {
        String value = robotConfig.getProperty(key);
        if (value == null)
        {
            return defaultValue;
        }
        try
        {
            return Integer.parseInt(value);
        } catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    public int getPlcPropertyInt(String key, int defaultValue)
    {
        String value = plcConfig.getProperty(key);
        if (value == null)
        {
            return defaultValue;
        }
        try
        {
            return Integer.parseInt(value);
        } catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }
}
