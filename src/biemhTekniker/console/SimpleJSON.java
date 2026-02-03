package biemhTekniker.console;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple JSON implementation for Java 1.7 compatibility.
 * Provides basic JSON object creation and parsing.
 */
public class SimpleJSON
{

    private final Map<String, Object> data;

    public SimpleJSON()
    {
        this.data = new HashMap<String, Object>();
    }

    public SimpleJSON(String jsonString)
    {
        this.data = new HashMap<String, Object>();
        parse(jsonString);
    }

    private void parse(String jsonString)
    {
        // Simple parser for basic JSON objects
        jsonString = jsonString.trim();
        if (jsonString.startsWith("{"))
        {
            jsonString = jsonString.substring(1);
        }
        if (jsonString.endsWith("}"))
        {
            jsonString = jsonString.substring(0, jsonString.length() - 1);
        }

        String[] pairs = jsonString.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs)
        {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2)
            {
                String key = keyValue[0].trim().replaceAll("\"", "");
                String value = keyValue[1].trim();

                if (value.startsWith("\"") && value.endsWith("\""))
                {
                    data.put(key, value.substring(1, value.length() - 1));
                } else if (value.equals("true") || value.equals("false"))
                {
                    data.put(key, Boolean.parseBoolean(value));
                } else
                {
                    try
                    {
                        data.put(key, Integer.parseInt(value));
                    } catch (NumberFormatException e)
                    {
                        data.put(key, value);
                    }
                }
            }
        }
    }

    public void put(String key, Object value)
    {
        data.put(key, value);
    }

    public String getString(String key, String defaultValue)
    {
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    public int getInt(String key, int defaultValue)
    {
        Object value = data.get(key);
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        try
        {
            return Integer.parseInt(value.toString());
        } catch (Exception e)
        {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue)
    {
        Object value = data.get(key);
        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        return defaultValue;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet())
        {
            if (!first)
            {
                sb.append(",");
            }
            first = false;
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String)
            {
                sb.append("\"").append(escape((String) value)).append("\"");
            } else if (value instanceof Boolean || value instanceof Number)
            {
                sb.append(value);
            } else
            {
                sb.append("\"").append(value.toString()).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private String escape(String s)
    {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
