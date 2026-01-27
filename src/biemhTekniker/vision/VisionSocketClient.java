package biemhTekniker.vision;

import biemhTekniker.logger.Logger;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Standardized TCP Client for Vision System communication.
 * Compatible with Java 1.6.
 */
public class VisionSocketClient {
    private static final Logger log = Logger.getLogger(VisionSocketClient.class);

    private Socket socket;
    private InputStream in;
    private PrintWriter out;
    private final String ip;
    private final int port;
    private final int timeout = 5000;

    public VisionSocketClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public boolean connect() {
        close();
        try {
            socket = new Socket();
            socket.setReuseAddress(true);
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.setSoTimeout(10000);

            in = socket.getInputStream();
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "US-ASCII"), true);

            log.info("Connected to Vision Server at " + ip + ":" + port);
            return true;
        } catch (Exception e) {
            log.error("Failed to connect to " + ip + ": " + e.getMessage());
            return false;
        }
    }

    public String sendAndReceive(String message, boolean expectResponse) {
        if (!isConnected()) {
            return null;
        }

        try {
            out.print(message);
            out.flush();
            byte[] buffer = new byte[2048];
            
            if (expectResponse)
            {
	            int bytesRead = in.read(buffer);
	
	            if (bytesRead > 0) {
	                String result = new String(buffer, 0, bytesRead, "US-ASCII");
	                log.debug(result);
	                return result;
	            } else {
	                log.warn("No data returned from camera.");
	                //close();
	                return null;
	            }
            }
            else
            {
            	return "0";
            }
        } catch (IOException e) {
            log.error("Communication error: " + e.getMessage());
            //close();
            return null;
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        } finally {
            in = null;
            out = null;
            socket = null;
        }
    }
}