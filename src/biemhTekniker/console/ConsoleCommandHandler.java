package biemhTekniker.console;

import biemhTekniker.logger.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles individual client connections and processes commands.
 */
public class ConsoleCommandHandler implements Runnable {
    
    private static final Logger log = Logger.getLogger(ConsoleCommandHandler.class);
    private final Socket clientSocket;
    private final ConsoleServerInterface serverInterface;
    private PrintWriter out;
    private BufferedReader in;
    private boolean running = true;
    
    public ConsoleCommandHandler(Socket socket, ConsoleServerInterface serverInterface) {
        this.clientSocket = socket;
        this.serverInterface = serverInterface;
    }
    
    @Override
    public void run() {
        log.info("ConsoleCommandHandler thread started for client: " + clientSocket.getInetAddress());
        
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            
            log.info("Client streams initialized: " + clientSocket.getInetAddress());
            sendResponse("connection", "Connected to KUKA Robot Console", true);
            
            String inputLine;
            while (running && (inputLine = in.readLine()) != null) {
                log.debug("Received from client: " + inputLine);
                handleCommand(inputLine);
            }
            
            log.info("Client disconnected (EOF): " + clientSocket.getInetAddress());
            
        } catch (IOException e) {
            log.error("Client handler I/O error for " + clientSocket.getInetAddress() + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            log.error("Unexpected error in client handler for " + clientSocket.getInetAddress() + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
    
    private void handleCommand(String command) {
        try {
            log.debug("Parsing command: " + command);
            SimpleJSON json = new SimpleJSON(command);
            String type = json.getString("type", "unknown");
            
            log.debug("Received command: " + type);
            
            if ("set_program".equals(type)) {
                handleSetProgram(json);
            } else if ("get_status".equals(type)) {
                handleGetStatus();
            } else if ("stop".equals(type)) {
                handleStop();
            } else {
                sendError("Unknown command type: " + type);
            }
            
        } catch (Exception e) {
            log.error("Command handling error: " + e.getMessage());
            e.printStackTrace();
            sendError("Invalid command format: " + e.getMessage());
        }
    }
    
    private void handleSetProgram(SimpleJSON json) {
        try {
            int programNumber = json.getInt("program", -1);
            log.info("handleSetProgram called with program: " + programNumber);
            
            if (programNumber >= 0 && programNumber <= 7) {
                serverInterface.setProgramNumber(programNumber);
                sendResponse("response", "Program set to " + programNumber, true);
                log.info("Program set successfully to: " + programNumber);
            } else {
                sendError("Invalid program number: " + programNumber);
                log.warn("Invalid program number requested: " + programNumber);
            }
        } catch (Exception e) {
            log.error("Error in handleSetProgram: " + e.getMessage());
            e.printStackTrace();
            sendError("Error setting program: " + e.getMessage());
        }
    }
    
    private void handleGetStatus() {
        try {
            log.info("handleGetStatus called");
            SimpleJSON status = new SimpleJSON();
            status.put("type", "status");
            status.put("program", serverInterface.getCurrentProgram());
            status.put("vision_connected", serverInterface.isVisionConnected());
            status.put("workpiece_position", serverInterface.getWorkpiecePosition());
            sendJson(status);
            log.info("Status sent to client");
        } catch (Exception e) {
            log.error("Error in handleGetStatus: " + e.getMessage());
            e.printStackTrace();
            sendError("Error getting status: " + e.getMessage());
        }
    }
    
    private void handleStop() {
        try {
            log.info("handleStop called");
            serverInterface.setProgramNumber(0);
            sendResponse("response", "Emergency stop - Program set to 0", true);
            log.info("Emergency stop executed");
        } catch (Exception e) {
            log.error("Error in handleStop: " + e.getMessage());
            e.printStackTrace();
            sendError("Error executing stop: " + e.getMessage());
        }
    }
    
    private void sendResponse(String type, String message, boolean success) {
        try {
            SimpleJSON response = new SimpleJSON();
            response.put("type", type);
            response.put("message", message);
            response.put("success", success);
            sendJson(response);
        } catch (Exception e) {
            log.error("Error sending response: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void sendError(String message) {
        log.warn("Sending error to client: " + message);
        sendResponse("error", message, false);
    }
    
    private void sendJson(SimpleJSON json) {
        try {
            if (out != null) {
                String jsonStr = json.toString();
                log.debug("Sending JSON to client: " + jsonStr);
                out.println(jsonStr);
            } else {
                log.error("Cannot send JSON - output stream is null");
            }
        } catch (Exception e) {
            log.error("Error in sendJson: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void sendLog(String level, String message) {
        try {
            SimpleJSON logJson = new SimpleJSON();
            logJson.put("type", "log");
            logJson.put("level", level);
            logJson.put("message", message);
            sendJson(logJson);
        } catch (Exception e) {
            log.error("Error sending log message: " + e.getMessage());
        }
    }
    
    private void cleanup() {
        log.info("Cleaning up client handler for: " + clientSocket.getInetAddress());
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
            log.info("Client handler cleaned up successfully");
        } catch (IOException e) {
            log.error("Cleanup error: " + e.getMessage());
        }
    }
    
    public void shutdown() {
        log.info("Shutdown requested for client handler");
        running = false;
    }
}
