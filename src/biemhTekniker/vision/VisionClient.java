package biemhTekniker.vision;

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
        getLogger().info("VisionClient initialized");
    }

    public void connect() throws IOException
    {
        if (connected.get())
        {
            getLogger().warn("VisionClient already connected");
            return;
        }

        clientSocket = new Socket(serverIp, serverPort);
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        writer = new DataOutputStream(clientSocket.getOutputStream());

        connected.set(true);
        getLogger().info("VisionClient connected to " + serverIp + ":" + serverPort);
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
            getLogger().warn("Error closing connection: " + e.getMessage());
        }

        getLogger().info("VisionClient disconnected");
    }

    public void sendData(String data) throws IOException
    {
        if (!connected.get())
        {
            throw new IOException("Not connected to vision system");
        }

        writer.writeBytes(data + "\n");
        writer.flush();
        getLogger().info("Sent to vision: " + data);
    }

    public void sendDataInt(int value) throws IOException
    {
        sendData(String.valueOf(value));
    }

    @Override
    public void run()
    {
        getLogger().info("VisionClient background task started");

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
                            getLogger().info("Vision data received: " + frame);
                        } else
                        {
                            getLogger().warn("Failed to parse vision data: " + line);
                        }
                    }
                }

                Thread.sleep(10);

            } catch (IOException e)
            {
                if (connected.get())
                {
                    getLogger().error("Vision read error: " + e.getMessage());
                    connected.set(false);
                }
                break;
            } catch (InterruptedException e)
            {
                break;
            }
        }

        getLogger().info("VisionClient background task stopped");
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
