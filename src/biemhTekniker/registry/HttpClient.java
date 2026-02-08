package biemhTekniker.registry;

import biemhTekniker.logger.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Simple HTTP client for REST API calls.
 * Java 7 compatible - uses HttpURLConnection.
 */
public class HttpClient {
    
    private static final Logger log = Logger.getLogger(HttpClient.class);
    private static final int TIMEOUT_MS = 5000;
    
    /**
     * Perform HTTP GET request.
     * 
     * @param urlString The URL to GET
     * @return Response body as String, or null on error
     */
    public static String get(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                log.warn("HTTP GET " + urlString + " returned " + responseCode);
                return null;
            }
            
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            return response.toString();
        } catch (Exception e) {
            log.error("HTTP GET failed: " + e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * Perform HTTP POST request.
     * 
     * @param urlString The URL to POST to
     * @param jsonBody JSON body to send
     * @return Response body as String, or null on error
     */
    public static String post(String urlString, String jsonBody) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            
            // Write body
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(jsonBody);
            out.flush();
            out.close();
            
            int responseCode = connection.getResponseCode();
            if (responseCode != 200 && responseCode != 201) {
                log.warn("HTTP POST " + urlString + " returned " + responseCode);
                return null;
            }
            
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            return response.toString();
        } catch (Exception e) {
            log.error("HTTP POST failed: " + e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
