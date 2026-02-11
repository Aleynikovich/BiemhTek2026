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
    private static final Logger log = Logger.getLogger(ConfigManager.class);
    private static final String CONFIG_PATH = "C:\\KRC\\Projects\\BiemhTek2026\\configs";
    private static volatile ConfigManager instance;
    private final Properties properties;

    /**
     * Private constructor for singleton.
     */
    private ConfigManager()
    {
        properties = new Properties();
        loadProperties();
    }

    /**
     * Gets the singleton instance using double-checked locking for performance.
     * Thread-safe and efficient after initialization.
     *
     * @return ConfigManager instance
     */
    public static ConfigManager getInstance()
    {
        // First check without synchronization for performance
        if (instance == null)
        {
            synchronized (ConfigManager.class)
            {
                // Second check with synchronization to prevent double initialization
                if (instance == null)
                {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    /**
     * Loads properties from file.
     */
    private void loadProperties()
    {
        java.io.File configDir = new java.io.File(CONFIG_PATH);

        // 1. Verify the folder exists
        if (configDir.exists() && configDir.isDirectory())
        {
            // 2. Filter for .properties files using an Anonymous Inner Class
            java.io.File[] files = configDir.listFiles(new java.io.FilenameFilter() {
                @Override
                public boolean accept(java.io.File dir, String name) {
                    return name.toLowerCase().endsWith(".properties");
                }
            });

            if (files != null)
            {
                for (java.io.File file : files)
                {
                    InputStream input = null;
                    try
                    {
                        input = new FileInputStream(file);
                        properties.load(input);
                        log.info("Successfully loaded: " + file.getName());
                    } catch (IOException e) {
                        log.error("Failed to load " + file.getName() + ": " + e.getMessage());
                    } finally {
                        // Java 7 manual close (or you could use try-with-resources)
                        if (input != null) {
                            try {
                                input.close();
                            } catch (IOException e) {
                                // ignore
                            }
                        }
                    }
                }
            }
        } else {
            log.error("Config directory not found or is not a directory: " + CONFIG_PATH);
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
        } catch (NumberFormatException e)
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
        } catch (NumberFormatException e)
        {
            log.warn("Invalid double for key " + key + ": " + value);
            return defaultValue;
        }
    }

    /**
     * Gets a boolean property.
     *
     * @param key          Property key
     * @param defaultValue Default value if key not found
     * @return Property value or default
     */
    public boolean getBoolean(String key, boolean defaultValue)
    {
        String value = properties.getProperty(key);
        if (value == null)
        {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
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
