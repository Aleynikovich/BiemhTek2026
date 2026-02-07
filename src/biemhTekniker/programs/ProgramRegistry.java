package biemhTekniker.programs;

import biemhTekniker.logger.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Registry that loads program factory mappings from a properties file.
 * Uses reflection to instantiate factory classes at runtime.
 * Caches factory instances for performance.
 */
public class ProgramRegistry
{
    private static final Logger log                     = Logger.getLogger(ProgramRegistry.class);
    private static final String CONFIG_SYSTEM_PROPERTY  = "biemh.programs.config";
    private static final String DEFAULT_CONFIG_PATH     = "./programs.properties";
    private final        Map    factoryCache            = new HashMap();
    private final        Properties properties;

    /**
     * Creates a ProgramRegistry and loads the configuration.
     * Configuration path is determined by:
     * 1. System property 'biemh.programs.config'
     * 2. Default path './programs.properties'
     */
    public ProgramRegistry()
    {
        this.properties = new Properties();
        loadConfiguration();
    }

    /**
     * Loads the properties configuration from file.
     */
    private void loadConfiguration()
    {
        String configPath = System.getProperty(CONFIG_SYSTEM_PROPERTY, DEFAULT_CONFIG_PATH);
        log.info("Loading program factory configuration from: " + configPath);

        InputStream input = null;
        try
        {
            input = new FileInputStream(configPath);
            properties.load(input);
            log.info("Loaded " + properties.size() + " program factory mappings");
        }
        catch (IOException e)
        {
            log.warn("Failed to load program factory configuration from " + configPath + ": " + e.getMessage());
            log.warn("ProgramRegistry will use fallback to hard-coded program registration");
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
                    log.warn("Error closing configuration file: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Looks up and instantiates a factory for the given program ID.
     *
     * @param programId The program ID to lookup
     * @return ProgramFactory instance, or null if not found or instantiation failed
     */
    public ProgramFactory lookup(int programId)
    {
        String programKey = String.valueOf(programId);

        // Check cache first
        if (factoryCache.containsKey(programKey))
        {
            log.debug("Returning cached factory for program ID: " + programId);
            return (ProgramFactory) factoryCache.get(programKey);
        }

        // Look up factory class name in properties
        String factoryClassName = properties.getProperty(programKey);
        if (factoryClassName == null)
        {
            log.debug("No factory mapping found for program ID: " + programId);
            return null;
        }

        // Instantiate factory using reflection
        try
        {
            log.info("Instantiating factory for program ID " + programId + ": " + factoryClassName);
            Class factoryClass = Class.forName(factoryClassName);
            ProgramFactory factory = (ProgramFactory) factoryClass.newInstance();

            // Cache the factory instance
            factoryCache.put(programKey, factory);
            log.debug("Cached factory instance for program ID: " + programId);

            return factory;
        }
        catch (ClassNotFoundException e)
        {
            log.error("Factory class not found for program ID " + programId + ": " + factoryClassName);
            return null;
        }
        catch (InstantiationException e)
        {
            log.error("Failed to instantiate factory for program ID " + programId + ": " + e.getMessage());
            return null;
        }
        catch (IllegalAccessException e)
        {
            log.error("Access denied when instantiating factory for program ID " + programId + ": " + e.getMessage());
            return null;
        }
        catch (ClassCastException e)
        {
            log.error("Factory class does not implement ProgramFactory interface: " + factoryClassName);
            return null;
        }
    }

    /**
     * Checks if a mapping exists for the given program ID.
     *
     * @param programId The program ID to check
     * @return true if a mapping exists, false otherwise
     */
    public boolean hasMapping(int programId)
    {
        return properties.containsKey(String.valueOf(programId));
    }

    /**
     * Gets the number of configured mappings.
     *
     * @return Number of program ID to factory mappings
     */
    public int getMappingCount()
    {
        return properties.size();
    }
}
