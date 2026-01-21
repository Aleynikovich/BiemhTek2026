package biemhTekniker.vision;

import biemhTekniker.ConfigManager;
import biemhTekniker.logger.CentralLogger;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPIBackgroundTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Background TCP client for vision system communication.
 * Connects to vision system and continuously reads incoming frames.
 * Uses CentralLogger for logging as background tasks cannot write to robot console directly.
 */
public class VisionClient extends RoboticsAPIBackgroundTask
{

    private Socket clientSocket;
    private BufferedReader reader;
    private DataOutputStream writer;

    private String serverIp;
    private int serverPort;
    private String delimiter;

    private AtomicBoolean connected;
    private AtomicBoolean dataAvailable;

    private VisionFrame latestFrame;
    private String latestRawData;

    private ConfigManager config;

    public void setConnectionParams(String serverIp, int serverPort, String delimiter)
    {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.delimiter = delimiter;
    }

    @Override
    public void initialize()
    {
        this.connected = new AtomicBoolean(false);
        this.dataAvailable = new AtomicBoolean(false);
        this.latestFrame = new VisionFrame();
        CentralLogger.getInstance().info("VISION", "VisionClient initialized");
    }

    public void connect() throws IOException
    {
        if (connected.get())
        {
            CentralLogger.getInstance().warn("VISION", "VisionClient already connected");
            return;
        }

        clientSocket = new Socket(serverIp, serverPort);
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        writer = new DataOutputStream(clientSocket.getOutputStream());

        connected.set(true);
        CentralLogger.getInstance().info("VISION", "VisionClient connected to " + serverIp + ":" + serverPort);
    }

    public void disconnect()
    {
        connected.set(false);

        try
        {
            if (reader != null)
            {
                reader.close();
            }
            if (writer != null)
            {
                writer.close();
            }
            if (clientSocket != null && !clientSocket.isClosed())
            {
                clientSocket.close();
            }
        } catch (IOException e)
        {
            CentralLogger.getInstance().warn("VISION", "Error closing connection: " + e.getMessage());
        }

        CentralLogger.getInstance().info("VISION", "VisionClient disconnected");
    }

    public void sendData(String data) throws IOException
    {
        if (!connected.get())
        {
            throw new IOException("Not connected to vision system");
        }

        writer.writeBytes(data + "\n");
        writer.flush();
        CentralLogger.getInstance().info("VISION", "Sent to vision: " + data);
    }

    public void sendDataInt(int value) throws IOException
    {
        sendData(String.valueOf(value));
    }

    @Override
    public void run()
    {
        CentralLogger.getInstance().info("VISION", "VisionClient background task started");

        while (!Thread.currentThread().isInterrupted() && connected.get())
        {
            try
            {
                if (reader.ready())
                {
                    String line = reader.readLine();

                    if (line != null && !line.isEmpty())
                    {
                        latestRawData = line;

                        VisionFrame frame = new VisionFrame();
                        if (frame.parseFromString(line, delimiter))
                        {
                            latestFrame = frame;
                            dataAvailable.set(true);
                            CentralLogger.getInstance().info("VISION", "Vision data received: " + frame);
                        } else
                        {
                            CentralLogger.getInstance().warn("VISION", "Failed to parse vision data: " + line);
                        }
                    }
                }

                Thread.sleep(10);

            } catch (IOException e)
            {
                if (connected.get())
                {
                    CentralLogger.getInstance().error("VISION", "Vision read error: " + e.getMessage());
                    connected.set(false);
                }
                break;
            } catch (InterruptedException e)
            {
                break;
            }
        }

        CentralLogger.getInstance().info("VISION", "VisionClient background task stopped");
    }

    public VisionFrame getLatestFrame()
    {
        if (dataAvailable.get())
        {
            return latestFrame;
        }
        return null;
    }

    public String getLatestRawData()
    {
        return latestRawData;
    }

    public boolean isDataAvailable()
    {
        return dataAvailable.getAndSet(false);
    }

    public boolean waitForData(long timeoutMs)
    {
        long startTime = System.currentTimeMillis();

        while (!dataAvailable.get())
        {
            try
            {
                Thread.sleep(100);
            } catch (InterruptedException e)
            {
                return false;
            }

            if (System.currentTimeMillis() - startTime > timeoutMs)
            {
                return false;
            }

            if (!connected.get())
            {
                return false;
            }
        }

        return true;
    }

    public boolean isConnected()
    {
        return connected.get();
    }

    @Override
    public void dispose()
    {
        disconnect();
        super.dispose();
    }
}
