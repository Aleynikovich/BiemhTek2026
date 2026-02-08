package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import com.kuka.common.ThreadUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class VisionSocketClient
{
    private static final Logger      log           = Logger.getLogger(VisionSocketClient.class);
    private final        String      ip;
    private final        int         port;
    private final        int         timeout       = 5000;
    private final        int         socketTimeout = 30000; // 30s for operations like loading references
    private              Socket      socket;
    private              InputStream in;
    private              PrintWriter out;

    public VisionSocketClient(String ip, int port)
    {
        this.ip   = ip;
        this.port = port;
    }

    public boolean connect()
    {
        close();
        try
        {
            socket = new Socket();
            socket.setReuseAddress(true);
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.setSoTimeout(socketTimeout);

            in  = socket.getInputStream();
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII), true);

            log.info("Connected to Vision Server at " + ip + ":" + port);
            return true;
        }
        catch (Exception e)
        {
            log.error("Failed to connect to " + ip + ": " + e.getMessage());
            return false;
        }
    }

    public void close()
    {
        try
        {
            if (in != null)
            {
                in.close();
            }
            if (out != null)
            {
                out.close();
            }
            if (socket != null)
            {
                socket.close();
            }
        }
        catch (IOException ignored)
        {
        }
        finally
        {
            in     = null;
            out    = null;
            socket = null;
        }
    }

    public String sendAndReceive(String message, boolean expectResponse)
    {
        if (!isConnected())
        {
            return null;
        }

        try
        {
            out.print(message);
            out.flush();
            ThreadUtil.milliSleep(100);
            byte[] buffer = new byte[2048];

            if (expectResponse)
            {
                int bytesRead = in.read(buffer);

                if (bytesRead > 0)
                {
                    String result = new String(buffer, 0, bytesRead, StandardCharsets.US_ASCII);
                    return result;
                }
                else
                {
                    log.warn("No data returned from camera.");
                    return null;
                }
            }
            else
            {
                return "0"; //Return 0 (success) if no expected response
            }
        }
        catch (IOException e)
        {
            log.error("Communication error: " + e.getMessage());
            return null;
        }
    }

    public boolean isConnected()
    {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * Tests connection health by checking if socket is still valid.
     * This helps detect cases where socket appears connected but server has restarted.
     *
     * @return true if connection is healthy, false otherwise
     */
    public boolean testConnection()
    {
        if (!isConnected())
        {
            return false;
        }

        try
        {
            // Check if streams are available and socket is not closed
            if (in == null || out == null || socket.isClosed())
            {
                return false;
            }

            // Check if input stream is still functional by calling available()
            // This method throws IOException if the stream is closed or connection is broken
            // We call it for its side effect - the return value doesn't matter
            in.available();
            return true;
        }
        catch (Exception e)
        {
            log.error("Connection test failed: " + e.getMessage());
            return false;
        }
    }
}