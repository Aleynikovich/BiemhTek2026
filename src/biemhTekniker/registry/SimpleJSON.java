package biemhTekniker.registry;

/**
 * Simple JSON builder and parser for Java 7.
 * Provides basic JSON serialization without external dependencies.
 */
public class SimpleJSON {
    
    /**
     * Build a simple JSON object for workpiece position.
     */
    public static String buildWorkpieceJSON(double x, double y, double z, 
                                            double rx, double ry, double rz,
                                            double score, String sourceProgram) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"x\":").append(x).append(",");
        json.append("\"y\":").append(y).append(",");
        json.append("\"z\":").append(z).append(",");
        json.append("\"rx\":").append(rx).append(",");
        json.append("\"ry\":").append(ry).append(",");
        json.append("\"rz\":").append(rz).append(",");
        json.append("\"score\":").append(score).append(",");
        json.append("\"sourceProgram\":\"").append(sourceProgram).append("\",");
        json.append("\"metadata\":\"\"");
        json.append("}");
        return json.toString();
    }
    
    /**
     * Parse program type from JSON string.
     * Very basic parsing - looks for "programType":"VALUE"
     */
    public static String extractProgramType(String json) {
        if (json == null) return null;
        
        int typeIndex = json.indexOf("\"programType\"");
        if (typeIndex == -1) return null;
        
        int colonIndex = json.indexOf(":", typeIndex);
        if (colonIndex == -1) return null;
        
        int quoteStart = json.indexOf("\"", colonIndex);
        if (quoteStart == -1) return null;
        
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        if (quoteEnd == -1) return null;
        
        return json.substring(quoteStart + 1, quoteEnd);
    }
    
    /**
     * Parse program number from JSON string.
     * Very basic parsing - looks for "programNumber":VALUE
     */
    public static Integer extractProgramNumber(String json) {
        if (json == null) return null;
        
        int numberIndex = json.indexOf("\"programNumber\"");
        if (numberIndex == -1) return null;
        
        int colonIndex = json.indexOf(":", numberIndex);
        if (colonIndex == -1) return null;
        
        // Skip whitespace
        int start = colonIndex + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        
        // Find end of number
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Parse string value from JSON.
     */
    public static String extractStringValue(String json, String fieldName) {
        if (json == null) return null;
        
        String searchKey = "\"" + fieldName + "\"";
        int fieldIndex = json.indexOf(searchKey);
        if (fieldIndex == -1) return null;
        
        int colonIndex = json.indexOf(":", fieldIndex);
        if (colonIndex == -1) return null;
        
        int quoteStart = json.indexOf("\"", colonIndex);
        if (quoteStart == -1) return null;
        
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        if (quoteEnd == -1) return null;
        
        return json.substring(quoteStart + 1, quoteEnd);
    }
    
    /**
     * Parse boolean value from JSON.
     */
    public static Boolean extractBooleanValue(String json, String fieldName) {
        if (json == null) return null;
        
        String searchKey = "\"" + fieldName + "\"";
        int fieldIndex = json.indexOf(searchKey);
        if (fieldIndex == -1) return null;
        
        int colonIndex = json.indexOf(":", fieldIndex);
        if (colonIndex == -1) return null;
        
        // Find the value after colon
        int start = colonIndex + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        
        if (json.substring(start).startsWith("true")) {
            return Boolean.TRUE;
        } else if (json.substring(start).startsWith("false")) {
            return Boolean.FALSE;
        }
        
        return null;
    }
}
