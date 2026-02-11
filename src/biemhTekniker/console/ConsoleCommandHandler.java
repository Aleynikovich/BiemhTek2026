package biemhTekniker.console;

import biemhTekniker.lib.logger.LogLevel;
import biemhTekniker.lib.logger.LogManager;
import biemhTekniker.lib.logger.Logger;
import biemhTekniker.lib.logger.NetworkListener;
import biemhTekniker.lib.robot.motions.MotionOverrides;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles individual client connections and processes commands.
 */
public class ConsoleCommandHandler implements Runnable
{

    private static final Logger log = Logger.getLogger(ConsoleCommandHandler.class);
    private final Socket clientSocket;
    private final ConsoleServerInterface serverInterface;
    private PrintWriter out;
    private BufferedReader in;
    private boolean running = true;
    private NetworkListener networkListener;

    public ConsoleCommandHandler(Socket socket, ConsoleServerInterface serverInterface)
    {
        this.clientSocket = socket;
        this.serverInterface = serverInterface;
    }

    @Override
    public void run()
    {
        log.info("ConsoleCommandHandler thread started for client: " + clientSocket.getInetAddress());

        try
        {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Register network listener to forward logs to this client
            networkListener = new NetworkListener(out);
            LogManager.register(networkListener);

            log.info("Client streams initialized and network listener registered: " + clientSocket.getInetAddress());
            sendResponse("connection", "Connected to KUKA Robot Console", true);

            String inputLine;
            while (running && (inputLine = in.readLine()) != null)
            {
                handleCommand(inputLine);
            }

            log.info("Client disconnected (EOF): " + clientSocket.getInetAddress());

        } catch (IOException e)
        {
            log.error("Client handler I/O error for " + clientSocket.getInetAddress() + ": " + e.getMessage(), e);
        } catch (Exception e)
        {
            log.error("Unexpected error in client handler for " + clientSocket.getInetAddress() + ": " + e.getMessage(), e);
        } finally
        {
            cleanup();
        }
    }

    private void handleCommand(String command)
    {
        try
        {
            // Validate command is not empty
            if (command == null || command.trim().isEmpty())
            {
                sendError("Empty command received");
                return;
            }
            
            SimpleJSON json = new SimpleJSON(command);
            
            // Validate type field exists
            if (!json.has("type"))
            {
                sendError("Missing required 'type' field in command");
                return;
            }
            
            String type = json.getString("type", "unknown");

            if ("set_program".equals(type))
            {
                handleSetProgram(json);
            } else if ("get_status".equals(type))
            {
                handleGetStatus();
            } else if ("get_queue_status".equals(type))
            {
                handleGetQueueStatus();
            } else if ("get_workpieces".equals(type))
            {
                handleGetWorkpieces();
            } else if ("stop".equals(type))
            {
                handleStop();
            } else if ("cancel_program".equals(type))
            {
                handleCancelProgram();
            } else if ("set_log_level".equals(type))
            {
                handleSetLogLevel(json);
            } else if ("get_log_level".equals(type))
            {
                handleGetLogLevel();
            } else if ("clear_queue".equals(type))
            {
                handleClearQueue();
            } else if ("delete_workpiece".equals(type))
            {
                handleDeleteWorkpiece(json);
            } else if ("pick_specific_workpiece".equals(type))
            {
                handlePickSpecificWorkpiece(json);
            } else if ("set_motion_override".equals(type))
            {
                handleSetMotionOverride(json);
            } else if ("clear_motion_override".equals(type))
            {
                handleClearMotionOverride();
            } else
            {
                sendError("Unknown command type: " + type);
            }

        } catch (IllegalArgumentException e)
        {
            log.error("JSON validation error: " + e.getMessage());
            sendError("Invalid JSON format: " + e.getMessage());
        } catch (Exception e)
        {
            log.error("Command handling error: " + e.getMessage(), e);
            sendError("Command processing error: " + e.getMessage());
        }
    }

    private void handleSetProgram(SimpleJSON json)
    {
        try
        {
            // Validate program field exists
            if (!json.has("program"))
            {
                sendError("Missing required 'program' field");
                log.warn("set_program command missing program field");
                return;
            }
            
            int programNumber = json.getInt("program", -1);
            log.debug("handleSetProgram called with program: " + programNumber);

            if (programNumber >= 0 && programNumber <= 199)
            {
                serverInterface.setProgramNumber(programNumber);
                sendResponse("response", "Program set to " + programNumber, true);
                log.info("Program set successfully to: " + programNumber);
            } else
            {
                sendError("Invalid program number: " + programNumber + " (valid range: 0-199)");
                log.warn("Invalid program number requested: " + programNumber);
            }
        } catch (Exception e)
        {
            log.error("Error in handleSetProgram: " + e.getMessage(), e);
            sendError("Error setting program: " + e.getMessage());
        }
    }

    private void handleGetStatus()
    {
        try
        {
            // Silent for cyclic calls - no debug logging
            SimpleJSON status = new SimpleJSON();
            status.put("type", "status");
            status.put("program", serverInterface.getCurrentProgram());
            status.put("vision_connected", serverInterface.isVisionConnected());
            status.put("workpiece_position", serverInterface.getWorkpiecePosition());
            // Gripper open/closed state reporting
            status.put("gripper1_closed", serverInterface.isGripper1Closed());
            status.put("gripper2_closed", serverInterface.isGripper2Closed());
            status.put("gripper3_closed", serverInterface.isGripper3Closed());
            sendJson(status);
        } catch (Exception e)
        {
            log.error("Error in handleGetStatus: " + e.getMessage(), e);
            sendError("Error getting status: " + e.getMessage());
        }
    }

    private void handleGetQueueStatus()
    {
        try
        {
            log.debug("handleGetQueueStatus called");
            String queueStatus = serverInterface.getQueueStatus();
            SimpleJSON response = new SimpleJSON();
            response.put("type", "queue_status");
            response.put("status", queueStatus);
            sendJson(response);
            log.debug("Queue status sent to client");
        } catch (Exception e)
        {
            log.error("Error in handleGetQueueStatus: " + e.getMessage(), e);
            sendError("Error getting queue status: " + e.getMessage());
        }
    }
    
    private void handleGetWorkpieces()
    {
        try
        {
            // Silent for cyclic calls - no debug logging
            String workpiecesJson = serverInterface.getWorkpiecesJson();
            SimpleJSON response = new SimpleJSON();
            response.put("type", "workpieces");
            response.put("workpieces", workpiecesJson);
            sendJson(response);
        } catch (Exception e)
        {
            log.error("Error in handleGetWorkpieces: " + e.getMessage(), e);
            sendError("Error getting workpieces: " + e.getMessage());
        }
    }

    private void handleStop()
    {
        try
        {
            log.debug("handleStop called");
            serverInterface.setProgramNumber(0);
            sendResponse("response", "Emergency stop - Program set to 0", true);
            log.info("Emergency stop executed");
        } catch (Exception e)
        {
            log.error("Error in handleStop: " + e.getMessage(), e);
            sendError("Error executing stop: " + e.getMessage());
        }
    }

    private void handleCancelProgram()
    {
        try
        {
            log.debug("handleCancelProgram called");
            serverInterface.cancelCurrentProgram();
            sendResponse("response", "Program cancelled - returning home without opening grippers", true);
            log.info("Program cancellation executed");
        } catch (Exception e)
        {
            log.error("Error in handleCancelProgram: " + e.getMessage(), e);
            sendError("Error cancelling program: " + e.getMessage());
        }
    }

    private void handleSetLogLevel(SimpleJSON json)
    {
        try
        {
            // Validate level field exists
            if (!json.has("level"))
            {
                sendError("Missing required 'level' field");
                log.warn("set_log_level command missing level field");
                return;
            }
            
            String levelStr = json.getString("level", "DEBUG");
            log.info("handleSetLogLevel called with level: " + levelStr);

            LogLevel level;
            try
            {
                level = LogLevel.valueOf(levelStr.toUpperCase());
            } catch (IllegalArgumentException e)
            {
                sendError("Invalid log level: " + levelStr + " (valid: DEBUG, INFO, WARN, ERROR)");
                return;
            }

            if (networkListener != null)
            {
                networkListener.setMinimumLevel(level);
                sendResponse("response", "Log level set to " + level, true);
                log.info("Log level set successfully to: " + level);
            } else
            {
                sendError("Network listener not initialized");
                log.error("Network listener is null in handleSetLogLevel");
            }
        } catch (Exception e)
        {
            log.error("Error in handleSetLogLevel: " + e.getMessage(), e);
            sendError("Error setting log level: " + e.getMessage());
        }
    }

    private void handleGetLogLevel()
    {
        try
        {
            log.info("handleGetLogLevel called");

            if (networkListener != null)
            {
                LogLevel currentLevel = networkListener.getMinimumLevel();
                SimpleJSON response = new SimpleJSON();
                response.put("type", "log_level");
                response.put("level", currentLevel.toString());
                sendJson(response);
                log.info("Log level sent to client: " + currentLevel);
            } else
            {
                sendError("Network listener not initialized");
                log.error("Network listener is null in handleGetLogLevel");
            }
        } catch (Exception e)
        {
            log.error("Error in handleGetLogLevel: " + e.getMessage(), e);
            sendError("Error getting log level: " + e.getMessage());
        }
    }
    
    private void handleClearQueue()
    {
        try
        {
            log.info("handleClearQueue called");
            serverInterface.clearWorkpieceQueue();
            sendResponse("response", "Workpiece queue cleared successfully", true);
            log.info("Workpiece queue cleared successfully");
        } catch (Exception e)
        {
            log.error("Error in handleClearQueue: " + e.getMessage(), e);
            sendError("Error clearing queue: " + e.getMessage());
        }
    }
    
    private void handleDeleteWorkpiece(SimpleJSON json)
    {
        try
        {
            // Validate id field exists
            if (!json.has("id"))
            {
                sendError("Missing required 'id' field");
                log.warn("delete_workpiece command missing id field");
                return;
            }
            long workpieceId = json.getLong("id", -1);
            log.info("handleDeleteWorkpiece called with id: " + workpieceId);
            if (workpieceId < 0)
            {
                sendError("Invalid workpiece ID: " + workpieceId);
                return;
            }
            boolean removed = serverInterface.removeWorkpiece(workpieceId);
            if (removed)
            {
                sendResponse("response", "Workpiece " + workpieceId + " deleted successfully", true);
                log.info("Workpiece deleted successfully: " + workpieceId);
            } else
            {
                sendError("Workpiece not found: " + workpieceId);
            }
        } catch (Exception e)
        {
            log.error("Error in handleDeleteWorkpiece: " + e.getMessage(), e);
            sendError("Error deleting workpiece: " + e.getMessage());
        }
    }

    private void handlePickSpecificWorkpiece(SimpleJSON json)
    {
        try
        {
            if (!json.has("id"))
            {
                sendError("Missing required 'id' field");
                return;
            }
            long id = json.getLong("id", -1);
            if (id <= 0)
            {
                sendError("Invalid workpiece ID: " + id);
                return;
            }
            MotionOverrides.setForcedWorkpieceId(id);
            // Trigger Pick New Workpiece program (1)
            serverInterface.setProgramNumber(1);
            sendResponse("response", "Forced pick requested for workpiece " + id, true);
            log.info("Forced pick requested for ID " + id + ", program 1 started");
        } catch (Exception e)
        {
            log.error("Error in handlePickSpecificWorkpiece: " + e.getMessage(), e);
            sendError("Error forcing pick: " + e.getMessage());
        }
    }

    private void handleSetMotionOverride(SimpleJSON json)
    {
        try
        {
            String redCsv = json.getString("redundancy", null);
            String zCsv = json.getString("zrot", null);
            boolean any = false;
            if (redCsv != null && !redCsv.trim().isEmpty())
            {
                double[] rads = parseCsvDegreesToRadians(redCsv);
                MotionOverrides.setRedundancyOffsetsOverride(rads);
                any = true;
            }
            if (zCsv != null && !zCsv.trim().isEmpty())
            {
                double[] rads = parseCsvDegreesToRadians(zCsv);
                MotionOverrides.setZRotationAnglesOverride(rads);
                any = true;
            }
            if (any)
            {
                sendResponse("response", "Motion overrides applied", true);
            } else
            {
                sendError("No overrides provided. Use fields 'redundancy' and/or 'zrot' with CSV degrees");
            }
        } catch (Exception e)
        {
            log.error("Error in handleSetMotionOverride: " + e.getMessage(), e);
            sendError("Error setting motion overrides: " + e.getMessage());
        }
    }

    private void handleClearMotionOverride()
    {
        try
        {
            MotionOverrides.clearMotionOverrides();
            sendResponse("response", "Motion overrides cleared", true);
        } catch (Exception e)
        {
            log.error("Error in handleClearMotionOverride: " + e.getMessage(), e);
            sendError("Error clearing motion overrides: " + e.getMessage());
        }
    }

    private double[] parseCsvDegreesToRadians(String csv)
    {
        String[] parts = csv.split(",");
        double[] arr = new double[parts.length];
        for (int i = 0; i < parts.length; i++)
        {
            String p = parts[i].trim();
            if (p.isEmpty())
            {
                arr[i] = 0.0;
            } else
            {
                arr[i] = Math.toRadians(Double.parseDouble(p));
            }
        }
        return arr;
    }

    private void sendResponse(String type, String message, boolean success)
    {
        try
        {
            SimpleJSON response = new SimpleJSON();
            response.put("type", type);
            response.put("message", message);
            response.put("success", success);
            sendJson(response);
        } catch (Exception e)
        {
            log.error("Error sending response: " + e.getMessage(), e);
        }
    }

    private void sendError(String message)
    {
        log.warn("Sending error to client: " + message);
        sendResponse("error", message, false);
    }

    public void sendLog(String level, String message)
    {
        try
        {
            SimpleJSON logJson = new SimpleJSON();
            logJson.put("type", "log");
            logJson.put("level", level);
            logJson.put("message", message);
            sendJson(logJson);
        } catch (Exception e)
        {
            log.error("Error sending log message: " + e.getMessage(), e);
        }
    }

    private void sendJson(SimpleJSON json)
    {
        try
        {
            if (out != null)
            {
                out.println(json.toString());
            } else
            {
                log.error("Cannot send JSON - output stream is null");
            }
        } catch (Exception e)
        {
            log.error("Error in sendJson: " + e.getMessage(), e);
        }
    }

    private void cleanup()
    {
        log.info("Cleaning up client handler for: " + clientSocket.getInetAddress());
        running = false;

        // Unregister network listener
        if (networkListener != null)
        {
            LogManager.unregister(networkListener);
            log.info("Network listener unregistered");
        }

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
            if (clientSocket != null)
            {
                clientSocket.close();
            }
            log.info("Client handler cleaned up successfully");
        } catch (IOException e)
        {
            log.error("Cleanup error: " + e.getMessage(), e);
        }
    }

    public void shutdown()
    {
        log.info("Shutdown requested for client handler");
        running = false;
    }

    /**
     * Check if the handler is still active.
     *
     * @return true if running and socket is connected
     */
    public boolean isActive()
    {
        return running && clientSocket != null && !clientSocket.isClosed() && clientSocket.isConnected();
    }
}
