package biemhTekniker.config;

import biemhTekniker.logger.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager for loading application settings.
 * Singleton pattern for centralized config access.
 */
public class ConfigManager
{
    private static final Logger         log            = Logger.getLogger(ConfigManager.class);
    private static final String         CONFIG_PATH    = "configs/application.properties";
    private static       ConfigManager  instance;
    private final        Properties     properties;

    /**
     * Private constructor for singleton.
     */
    private ConfigManager()
    {
        properties = new Properties();
        loadProperties();
    }

    /**
     * Gets the singleton instance.
     *
     * @return ConfigManager instance
     */
    public static synchronized ConfigManager getInstance()
    {
        if (instance == null)
        {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Loads properties from file.
     */
    private void loadProperties()
    {
        InputStream input = null;
        try
        {
            input = new FileInputStream(CONFIG_PATH);
            properties.load(input);
            log.info("Configuration loaded from " + CONFIG_PATH);
        }
        catch (IOException e)
        {
            log.error("Failed to load configuration from " + CONFIG_PATH + ": " + e.getMessage());
        }
        finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                }
                catch (IOException e)
                {
                    log.warn("Failed to close config file: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Gets a string property.
     *
     * @param key          Property key
     * @param defaultValue Default value if key not found
     * @return Property value or default
     */
    public String getString(String key, String defaultValue)
    {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gets an integer property.
     *
     * @param key          Property key
     * @param defaultValue Default value if key not found or invalid
     * @return Property value or default
     */
    public int getInt(String key, int defaultValue)
    {
        String value = properties.getProperty(key);
        if (value == null)
        {
            return defaultValue;
        }
        try
        {
            return Integer.parseInt(value.trim());
        }
        catch (NumberFormatException e)
        {
            log.warn("Invalid integer for key " + key + ": " + value);
            return defaultValue;
        }
    }

    /**
     * Gets a double property.
     *
     * @param key          Property key
     * @param defaultValue Default value if key not found or invalid
     * @return Property value or default
     */
    public double getDouble(String key, double defaultValue)
    {
        String value = properties.getProperty(key);
        if (value == null)
        {
            return defaultValue;
        }
        try
        {
            return Double.parseDouble(value.trim());
        }
        catch (NumberFormatException e)
        {
            log.warn("Invalid double for key " + key + ": " + value);
            return defaultValue;
        }
    }

    /**
     * Gets a string array property (comma-separated).
     *
     * @param key       Property key
     * @param delimiter Delimiter for splitting
     * @return Array of strings, or empty array if not found
     */
    public String[] getStringArray(String key, String delimiter)
    {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty())
        {
            return new String[0];
        }
        String[] parts = value.split(delimiter);
        // Trim each part
        for (int i = 0; i < parts.length; i++)
        {
            parts[i] = parts[i].trim();
        }
        return parts;
    }
}
