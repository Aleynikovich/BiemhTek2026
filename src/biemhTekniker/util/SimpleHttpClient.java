package biemhTekniker.util;

import biemhTekniker.logger.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Simple HTTP client for making REST API calls.
 * Java 7 compatible using HttpURLConnection.
 */
public class SimpleHttpClient
{
    private static final Logger log             = Logger.getLogger(SimpleHttpClient.class);
    private static final int    CONNECT_TIMEOUT = 5000;  // 5 seconds
    private static final int    READ_TIMEOUT    = 10000; // 10 seconds

    /**
     * Perform a GET request.
     *
     * @param urlString the URL to request
     * @return response body as String, or null on error
     */
    public static String get(String urlString)
    {
        HttpURLConnection conn = null;
        try
        {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                return readResponse(conn);
            }
            else
            {
                log.error("GET request failed: " + urlString + " - HTTP " + responseCode);
                return null;
            }
        }
        catch (Exception e)
        {
            log.error("GET request exception: " + urlString + " - " + e.getMessage());
            return null;
        }
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            }
        }
    }

    /**
     * Perform a POST request with JSON body.
     *
     * @param urlString the URL to request
     * @param jsonBody  the JSON body to send
     * @return response body as String, or null on error
     */
    public static String post(String urlString, String jsonBody)
    {
        HttpURLConnection conn = null;
        try
        {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // Write request body
            OutputStream os = conn.getOutputStream();
            os.write(jsonBody.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED)
            {
                return readResponse(conn);
            }
            else
            {
                log.error("POST request failed: " + urlString + " - HTTP " + responseCode);
                return null;
            }
        }
        catch (Exception e)
        {
            log.error("POST request exception: " + urlString + " - " + e.getMessage());
            return null;
        }
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            }
        }
    }

    /**
     * Read the response body from a connection.
     */
    private static String readResponse(HttpURLConnection conn) throws Exception
    {
        BufferedReader br     = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder  sb     = new StringBuilder();
        String         line;
        while ((line = br.readLine()) != null)
        {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }
}
