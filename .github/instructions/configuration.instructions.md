---
applyTo: "src/biemhTekniker/lib/config/**/*.java"
---

# Instructions for Configuration Management Code

Configuration classes load and provide access to system parameters from `application.properties`.

## Key Principles
- **Singleton Pattern**: Use thread-safe lazy initialization with `volatile` and double-checked locking
- **Fallback Defaults**: ALWAYS provide sensible defaults when properties are missing or invalid
- **Error Handling**: Log warnings when falling back to defaults, but don't throw exceptions
- **Type Safety**: Parse values carefully with try-catch for `NumberFormatException`
- **Immutability**: Return copies of arrays/collections, not direct references

## ConfigManager Pattern
```java
public class ConfigManager {
    private static volatile ConfigManager instance;
    private Properties properties;
    
    private ConfigManager() {
        properties = new Properties();
        try {
            // Load from controller filesystem
            FileInputStream fis = new FileInputStream("/home/kuka/application.properties");
            properties.load(fis);
            fis.close();
        } catch (IOException e) {
            // Log warning, use defaults
        }
    }
    
    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }
    
    public double getDoubleProperty(String key, double defaultValue) {
        try {
            String value = properties.getProperty(key);
            if (value != null && !value.trim().isEmpty()) {
                return Double.parseDouble(value.trim());
            }
        } catch (NumberFormatException e) {
            // Log warning with key and invalid value
        }
        return defaultValue;
    }
}
```

## Property Naming Convention
- Use dot notation: `category.subcategory.parameter`
- Examples: `vision.server.ip`, `motion.joint.velocity`, `impedance.stiffness.x`
- Units in name when not obvious: `motion.delay.ms`, `workpiece.position.tolerance.mm`

## CSV Parsing
Some properties contain comma-separated lists (e.g., `motion.redundancy.offsets`):
```java
public double[] parseDoubleArray(String key, double[] defaultValue) {
    String value = properties.getProperty(key);
    if (value == null || value.trim().isEmpty()) {
        return defaultValue;
    }
    
    String[] parts = value.split(",");
    double[] result = new double[parts.length];
    for (int i = 0; i < parts.length; i++) {
        try {
            result[i] = Double.parseDouble(parts[i].trim());
        } catch (NumberFormatException e) {
            return defaultValue; // Fail entire array on any parse error
        }
    }
    return result;
}
```

## Unit Conversions
- **Degrees to Radians**: Many config values are in degrees for readability, convert to radians for API
- **Millimeters to Meters**: Some values may need conversion for KUKA API
- **Percentages**: Joint velocity is 0.0-1.0 in API, but may be 0-100 in config
