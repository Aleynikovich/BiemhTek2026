package biemhTekniker.util;

import biemhTekniker.model.ProgramDescriptor;
import biemhTekniker.model.ProgramType;

/**
 * Simple JSON parser/serializer for basic operations.
 * Java 7 compatible - handles only the specific DTOs needed for this application.
 * For more complex JSON operations, consider adding Gson library.
 */
public class SimpleJson
{
    /**
     * Parse a ProgramDescriptor from JSON string.
     * Expects format: {"id":1,"programNumber":1,"programName":"...","programType":"VISION","description":"...","enabled":true}
     */
    public static ProgramDescriptor parseProgramDescriptor(String json)
    {
        if (json == null || json.isEmpty())
        {
            return null;
        }

        ProgramDescriptor descriptor = new ProgramDescriptor();

        // Remove braces and split by commas (naive approach for simple JSON)
        String content = json.trim();
        if (content.startsWith("{"))
        {
            content = content.substring(1);
        }
        if (content.endsWith("}"))
        {
            content = content.substring(0, content.length() - 1);
        }

        // Split by comma but preserve quoted strings
        String[] pairs = splitJsonPairs(content);

        for (String pair : pairs)
        {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2)
            {
                continue;
            }

            String key   = unquote(keyValue[0].trim());
            String value = keyValue[1].trim();

            if ("id".equals(key))
            {
                descriptor.setId(parseLong(value));
            }
            else if ("programNumber".equals(key))
            {
                descriptor.setProgramNumber(parseInteger(value));
            }
            else if ("programName".equals(key))
            {
                descriptor.setProgramName(unquote(value));
            }
            else if ("programType".equals(key))
            {
                String typeStr = unquote(value);
                if (typeStr != null)
                {
                    try
                    {
                        descriptor.setProgramType(ProgramType.valueOf(typeStr));
                    }
                    catch (IllegalArgumentException e)
                    {
                        // Default to ROBOT if parsing fails
                        descriptor.setProgramType(ProgramType.ROBOT);
                    }
                }
            }
            else if ("description".equals(key))
            {
                descriptor.setDescription(unquote(value));
            }
            else if ("enabled".equals(key))
            {
                descriptor.setEnabled(parseBoolean(value));
            }
        }

        return descriptor;
    }

    /**
     * Build JSON for WorkpiecePosition POST request.
     */
    public static String buildWorkpieceJson(double x, double y, double z, double rx, double ry, double rz,
                                            double score, String sourceProgram)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"x\":").append(x).append(",");
        sb.append("\"y\":").append(y).append(",");
        sb.append("\"z\":").append(z).append(",");
        sb.append("\"rx\":").append(rx).append(",");
        sb.append("\"ry\":").append(ry).append(",");
        sb.append("\"rz\":").append(rz).append(",");
        sb.append("\"score\":").append(score).append(",");
        sb.append("\"sourceProgram\":\"").append(sourceProgram).append("\"");
        sb.append("}");
        return sb.toString();
    }

    // Helper methods

    private static String[] splitJsonPairs(String content)
    {
        // Simple split by comma, considering quoted strings
        java.util.List<String> pairs        = new java.util.ArrayList<String>();
        StringBuilder          currentPair  = new StringBuilder();
        boolean                inQuotes     = false;
        int                    braceLevel   = 0;
        int                    bracketLevel = 0;

        for (int i = 0; i < content.length(); i++)
        {
            char c = content.charAt(i);

            if (c == '"' && (i == 0 || content.charAt(i - 1) != '\\'))
            {
                inQuotes = !inQuotes;
            }
            else if (!inQuotes)
            {
                if (c == '{')
                {
                    braceLevel++;
                }
                else if (c == '}')
                {
                    braceLevel--;
                }
                else if (c == '[')
                {
                    bracketLevel++;
                }
                else if (c == ']')
                {
                    bracketLevel--;
                }
                else if (c == ',' && braceLevel == 0 && bracketLevel == 0)
                {
                    pairs.add(currentPair.toString());
                    currentPair = new StringBuilder();
                    continue;
                }
            }

            currentPair.append(c);
        }

        if (currentPair.length() > 0)
        {
            pairs.add(currentPair.toString());
        }

        return pairs.toArray(new String[pairs.size()]);
    }

    private static String unquote(String str)
    {
        if (str == null)
        {
            return null;
        }
        String trimmed = str.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\""))
        {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static Long parseLong(String str)
    {
        try
        {
            return Long.parseLong(unquote(str));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private static Integer parseInteger(String str)
    {
        try
        {
            return Integer.parseInt(unquote(str));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private static Boolean parseBoolean(String str)
    {
        String unquoted = unquote(str);
        if ("true".equalsIgnoreCase(unquoted))
        {
            return true;
        }
        else if ("false".equalsIgnoreCase(unquoted))
        {
            return false;
        }
        return null;
    }
}
