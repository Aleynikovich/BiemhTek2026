package biemhTekniker.registry;

import biemhTekniker.logger.Logger;
import biemhTekniker.model.ProgramDescriptor;
import biemhTekniker.model.ProgramType;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for program descriptors loaded from the config service.
 * Provides caching and HTTP client to GET /api/programs.
 * Java 7 compatible.
 */
public class ProgramRegistry {
    
    private static final Logger log = Logger.getLogger(ProgramRegistry.class);
    
    private final String configServiceBaseUrl;
    private final Map<Integer, ProgramDescriptor> cache;
    private long lastRefreshTime;
    private static final long CACHE_TIMEOUT_MS = 300000; // 5 minutes
    
    /**
     * Create a new program registry.
     * 
     * @param configServiceBaseUrl Base URL of config service (e.g., "http://172.31.1.100:8080")
     */
    public ProgramRegistry(String configServiceBaseUrl) {
        this.configServiceBaseUrl = configServiceBaseUrl;
        this.cache = new HashMap<Integer, ProgramDescriptor>();
        this.lastRefreshTime = 0;
    }
    
    /**
     * Get a program descriptor by program number.
     * Fetches from config service if not in cache or cache is stale.
     * 
     * @param programNumber The program number
     * @return ProgramDescriptor or null if not found
     */
    public ProgramDescriptor getProgram(int programNumber) {
        // Check cache first
        long now = System.currentTimeMillis();
        if (cache.containsKey(programNumber) && (now - lastRefreshTime) < CACHE_TIMEOUT_MS) {
            log.debug("Returning cached program " + programNumber);
            return cache.get(programNumber);
        }
        
        // Fetch from config service
        log.info("Fetching program " + programNumber + " from config service");
        ProgramDescriptor descriptor = fetchProgramFromService(programNumber);
        
        if (descriptor != null) {
            cache.put(programNumber, descriptor);
            lastRefreshTime = now;
        }
        
        return descriptor;
    }
    
    /**
     * Refresh the cache by fetching all programs from config service.
     */
    public void refreshCache() {
        log.info("Refreshing program registry cache from config service");
        
        String url = configServiceBaseUrl + "/api/programs";
        String response = HttpClient.get(url);
        
        if (response == null) {
            log.error("Failed to refresh program registry");
            return;
        }
        
        // Parse JSON array - very basic parsing
        // Expected format: [{"programNumber":1,...},{"programNumber":2,...},...]
        parseProgramsArray(response);
        
        lastRefreshTime = System.currentTimeMillis();
        log.info("Program registry cache refreshed with " + cache.size() + " programs");
    }
    
    /**
     * Fetch a single program from the config service.
     */
    private ProgramDescriptor fetchProgramFromService(int programNumber) {
        String url = configServiceBaseUrl + "/api/programs/" + programNumber;
        String response = HttpClient.get(url);
        
        if (response == null) {
            log.error("Failed to fetch program " + programNumber + " from config service");
            return null;
        }
        
        return parseProgramDescriptor(response);
    }
    
    /**
     * Parse a single program descriptor from JSON.
     */
    private ProgramDescriptor parseProgramDescriptor(String json) {
        try {
            ProgramDescriptor descriptor = new ProgramDescriptor();
            
            Integer programNumber = SimpleJSON.extractProgramNumber(json);
            String programName = SimpleJSON.extractStringValue(json, "programName");
            String programTypeStr = SimpleJSON.extractProgramType(json);
            Boolean enabled = SimpleJSON.extractBooleanValue(json, "enabled");
            
            if (programNumber == null || programName == null || programTypeStr == null) {
                log.error("Failed to parse program descriptor: missing required fields");
                return null;
            }
            
            ProgramType programType = ProgramType.valueOf(programTypeStr);
            
            descriptor.setProgramNumber(programNumber);
            descriptor.setProgramName(programName);
            descriptor.setProgramType(programType);
            descriptor.setEnabled(enabled != null ? enabled : Boolean.TRUE);
            descriptor.setDescription(SimpleJSON.extractStringValue(json, "description"));
            
            return descriptor;
        } catch (Exception e) {
            log.error("Error parsing program descriptor: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse an array of programs from JSON.
     * Very basic parsing - splits by "programNumber" occurrences.
     */
    private void parseProgramsArray(String json) {
        // Simple approach: split by objects in array
        int index = 0;
        while (true) {
            int start = json.indexOf("{", index);
            if (start == -1) break;
            
            int end = json.indexOf("}", start);
            if (end == -1) break;
            
            String programJson = json.substring(start, end + 1);
            ProgramDescriptor descriptor = parseProgramDescriptor(programJson);
            
            if (descriptor != null && descriptor.getProgramNumber() != null) {
                cache.put(descriptor.getProgramNumber(), descriptor);
            }
            
            index = end + 1;
        }
    }
    
    /**
     * Clear the cache.
     */
    public void clearCache() {
        cache.clear();
        lastRefreshTime = 0;
    }
}
