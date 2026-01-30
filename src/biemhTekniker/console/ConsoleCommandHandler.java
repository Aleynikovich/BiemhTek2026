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
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            
            log.info("Client connected: " + clientSocket.getInetAddress());
            sendResponse("connection", "Connected to KUKA Robot Console", true);
            
            String inputLine;
            while (running && (inputLine = in.readLine()) != null) {
                handleCommand(inputLine);
            }
            
        } catch (IOException e) {
            log.error("Client handler error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    private void handleCommand(String command) {
        try {
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
            sendError("Invalid command format: " + e.getMessage());
        }
    }
    
    private void handleSetProgram(SimpleJSON json) {
        int programNumber = json.getInt("program", -1);
        if (programNumber >= 0 && programNumber <= 7) {
            serverInterface.setProgramNumber(programNumber);
            sendResponse("response", "Program set to " + programNumber, true);
            log.info("Program set to: " + programNumber);
        } else {
            sendError("Invalid program number: " + programNumber);
        }
    }
    
    private void handleGetStatus() {
        SimpleJSON status = new SimpleJSON();
        status.put("type", "status");
        status.put("program", serverInterface.getCurrentProgram());
        status.put("vision_connected", serverInterface.isVisionConnected());
        status.put("workpiece_position", serverInterface.getWorkpiecePosition());
        sendJson(status);
    }
    
    private void handleStop() {
        serverInterface.setProgramNumber(0);
        sendResponse("response", "Emergency stop - Program set to 0", true);
    }
    
    private void sendResponse(String type, String message, boolean success) {
        SimpleJSON response = new SimpleJSON();
        response.put("type", type);
        response.put("message", message);
        response.put("success", success);
        sendJson(response);
    }
    
    private void sendError(String message) {
        sendResponse("error", message, false);
    }
    
    private void sendJson(SimpleJSON json) {
        if (out != null) {
            out.println(json.toString());
        }
    }
    
    public void sendLog(String level, String message) {
        SimpleJSON log = new SimpleJSON();
        log.put("type", "log");
        log.put("level", level);
        log.put("message", message);
        sendJson(log);
    }
    
    private void cleanup() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            log.error("Cleanup error: " + e.getMessage());
        }
        log.info("Client disconnected");
    }
    
    public void shutdown() {
        running = false;
    }
}
