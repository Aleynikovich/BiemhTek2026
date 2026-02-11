package biemhTekniker.programs;

import biemhTekniker.config.ConfigManager;
import biemhTekniker.lib.vision.VisionContext;
import biemhTekniker.lib.vision.VisionTask;
import biemhTekniker.logger.Logger;

/**
 * Vision task to load all configured references.
 * Program 100: Load References
 */
public class LoadReferencesTask implements VisionTask
{
    private static final Logger log = Logger.getLogger(LoadReferencesTask.class);

    public void execute(VisionContext context) throws Exception
    {
        log.info("Loading references from configuration...");

        ConfigManager config = ConfigManager.getInstance();
        if (config == null)
        {
            log.error("Config manager not found");
            throw new Exception("Config manager not found");
        }

        String[] references = config.getStringArray("vision.references", ",");

        if (references.length == 0)
        {
            log.error("No references configured in application.properties");
            throw new Exception("No references configured");
        }

        int successCount = 0;
        for (int i = 0; i < references.length; i++)
        {
            String refName = references[i];
            log.info("Loading reference: " + refName);
            boolean success = context.getProtocol().loadReference(refName);
            if (success)
            {
                log.info("Successfully loaded reference: " + refName);
                successCount++;
            } else
            {
                log.error("Failed to load reference: " + refName);
            }
        }

        if (successCount == 0)
        {
            throw new Exception("Failed to load any references");
        }

        log.info("Loaded " + successCount + " of " + references.length + " references");
    }
}
