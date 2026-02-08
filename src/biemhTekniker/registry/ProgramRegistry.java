package biemhTekniker.registry;

import biemhTekniker.logger.Logger;
import biemhTekniker.model.ProgramDescriptor;
import biemhTekniker.util.SimpleHttpClient;
import biemhTekniker.util.SimpleJson;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry that loads program descriptors from the config service.
 * Provides a cache of program configurations that can be refreshed.
 */
public class ProgramRegistry
{
    private static final Logger log = Logger.getLogger(ProgramRegistry.class);

    private final String                           configServiceUrl;
    private final Map<Integer, ProgramDescriptor> programCache;

    public ProgramRegistry(String configServiceUrl)
    {
        this.configServiceUrl = configServiceUrl;
        this.programCache     = new HashMap<Integer, ProgramDescriptor>();
    }

    /**
     * Load program descriptor from config service by program number.
     * Results are cached to minimize HTTP requests.
     *
     * @param programNumber the program number to load
     * @return ProgramDescriptor or null if not found
     */
    public ProgramDescriptor getProgram(int programNumber)
    {
        // Check cache first
        if (programCache.containsKey(programNumber))
        {
            return programCache.get(programNumber);
        }

        // Fetch from config service
        ProgramDescriptor descriptor = fetchProgramFromService(programNumber);
        if (descriptor != null)
        {
            programCache.put(programNumber, descriptor);
        }

        return descriptor;
    }

    /**
     * Refresh the program cache by clearing it.
     * Programs will be re-fetched on next access.
     */
    public void refresh()
    {
        log.info("Refreshing program registry cache");
        programCache.clear();
    }

    /**
     * Refresh a specific program in the cache.
     */
    public void refreshProgram(int programNumber)
    {
        log.info("Refreshing program " + programNumber + " in cache");
        programCache.remove(programNumber);
        getProgram(programNumber); // Re-fetch
    }

    /**
     * Fetch program descriptor from config service via HTTP.
     */
    private ProgramDescriptor fetchProgramFromService(int programNumber)
    {
        if (configServiceUrl == null || configServiceUrl.isEmpty())
        {
            log.warn("Config service URL not configured, cannot fetch programs");
            return null;
        }

        try
        {
            String url      = configServiceUrl + "/api/programs/" + programNumber;
            String response = SimpleHttpClient.get(url);

            if (response != null)
            {
                ProgramDescriptor descriptor = SimpleJson.parseProgramDescriptor(response);
                if (descriptor != null)
                {
                    log.info("Loaded program from config service: " + descriptor);
                    return descriptor;
                }
                else
                {
                    log.error("Failed to parse program descriptor from response");
                }
            }
            else
            {
                log.debug("Program " + programNumber + " not found in config service");
            }
        }
        catch (Exception e)
        {
            log.error("Error fetching program from config service: " + e.getMessage());
        }

        return null;
    }

    /**
     * Check if config service is configured.
     */
    public boolean isConfigured()
    {
        return configServiceUrl != null && !configServiceUrl.isEmpty();
    }
}
