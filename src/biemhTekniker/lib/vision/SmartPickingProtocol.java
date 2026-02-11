package biemhTekniker.lib.vision;

import biemhTekniker.lib.logger.Logger;

/**
 * Communication protocol for the SmartPicking system.
 * Translates logical commands into TCP frames and parses complex responses.
 */
public class SmartPickingProtocol
{

    private static final Logger log = Logger.getLogger(SmartPickingProtocol.class);
    private final VisionSocketClient _client;

    public SmartPickingProtocol(VisionSocketClient client)
    {
        this._client = client;
    }

    public boolean sendCustomMessage(String message, boolean expectReply)
    {
        _client.sendAndReceive(message, expectReply);
        return true;
    }

    /**
     * Loads a specific reference by name.
     *
     * @param name Reference name (e.g., "BIEMH26_105055")
     * @return true if successful, false otherwise
     */
    public boolean loadReference(String name)
    {
        VisionResult res = execute(Command.LOAD_REFERENCE, name, true);
        return res.isSuccess();
    }

    /**
     * Changes the application's operating mode.
     */
    public boolean setMode(Command mode)
    {
        if (mode != Command.SET_AUTO_MODE && mode != Command.SET_CALIB_MODE)
        {
            log.error("Invalid mode command requested.");
            return false;
        }
        return execute(mode, true).isSuccess();
    }

    /**
     * Locates parts for a specific zone and reference.
     * Supports optional subarguments for flexibility.
     *
     * @param zone      Zone number (1-based)
     * @param reference Reference index (1-based)
     * @return VisionResult with locate status
     */
    public VisionResult locateParts(int zone, int reference)
    {
        String args = zone + ";" + reference;
        return execute(Command.LOCATE_PARTS, args, true);
    }

    /**
     * Locates parts with additional optional arguments.
     * Allows for future extensibility (e.g., "4;1;2;extraParam").
     *
     * @param zone      Zone number (1-based)
     * @param reference Reference index (1-based)
     * @param additionalArgs Optional additional arguments (can be null or empty)
     * @return VisionResult with locate status
     */
    public VisionResult locatePartsWithArgs(int zone, int reference, String additionalArgs)
    {
        String args = zone + ";" + reference;
        if (additionalArgs != null && !additionalArgs.isEmpty())
        {
            args += ";" + additionalArgs;
        }
        return execute(Command.LOCATE_PARTS, args, true);
    }

    /**
     * Sends a workpiece scan request for a specific zone and reference number.
     * This is used to request scanning of a picked workpiece.
     *
     * @param zone Zone number (typically 1)
     * @param referenceNumber Reference number (53, 55, 60, etc.)
     * @return VisionResult with scan status
     */
    public VisionResult sendWorkpieceScanRequest(int zone, int referenceNumber)
    {
        String args = zone + ";" + referenceNumber;
        return execute(Command.SEND_WORKPIECE_SCAN_REQUEST, args, true);
    }

    /**
     * Requests the orientation of a scanned workpiece.
     * This command should be called after sendWorkpieceScanRequest.
     *
     * @param zone Zone number (typically 1)
     * @return VisionResult containing reference+orientation (e.g., 530, 531, 550, 551, 600, 601)
     */
    public VisionResult requestWorkpieceOrientation(int zone)
    {
        String args = String.valueOf(zone);
        return execute(Command.REQUEST_WORKPIECE_ORIENTATION, args, true);
    }

    /**
     * Callback interface for processing workpieces as they are found.
     */
    public interface WorkpieceCallback
    {
        /**
         * Called when a workpiece is found during scanning.
         * 
         * @param workpiece The found workpiece data
         */
        void onWorkpieceFound(biemhTekniker.lib.data.WorkpieceData workpiece);
    }

    /**
     * Locates parts across all references in a zone with progressive processing.
     * Iterates through reference indices 1 to referenceCount.
     * For each reference, locates parts then retrieves all part positions using GET_PART_POS and GET_NEXT_PART_POS.
     * The callback is invoked immediately for each found workpiece, enabling progressive processing.
     *
     * @param referenceCount Number of references to scan (e.g., 3)
     * @param zone           Zone number (e.g., 1)
     * @param callback       Callback to process workpieces as they are found
     * @return Total count of workpieces found across all references
     */
    public int locateAllParts(int referenceCount, int zone, WorkpieceCallback callback)
    {
        int totalCount = 0;

        for (int ref = 1; ref <= referenceCount; ref++)
        {
            log.info("Locating parts for reference " + ref + " in zone " + zone);

            // Locate parts for this reference
            VisionResult locateResult = locateParts(zone, ref);
            if (!locateResult.isSuccess())
            {
                log.warn("Failed to locate parts for reference " + ref);
                continue;
            }

            // Get first part position
            VisionResult firstPartResult = execute(Command.GET_PART_POS, true);
            if (firstPartResult.isSuccess())
            {
                biemhTekniker.lib.data.WorkpieceData wp = createWorkpieceFromResult(firstPartResult, ref);
                callback.onWorkpieceFound(wp);
                totalCount++;
                log.debug("Found part 1 for reference " + ref + ": score=" + wp.getScore());

                // Get remaining parts using GET_NEXT_PART_POS
                int partCount = 1;
                while (true)
                {
                    VisionResult nextPartResult = execute(Command.GET_NEXT_PART_POS, true);
                    if (!nextPartResult.isSuccess())
                    {
                        break;
                    }
                    biemhTekniker.lib.data.WorkpieceData nextWp = createWorkpieceFromResult(nextPartResult, ref);
                    callback.onWorkpieceFound(nextWp);
                    totalCount++;
                    partCount++;
                    log.debug("Found part " + partCount + " for reference " + ref + ": score=" + nextWp.getScore());
                }

                log.info("Found " + partCount + " parts for reference " + ref);
            } else
            {
                log.warn("No parts found for reference " + ref);
            }
        }

        log.info("Total parts found across all references: " + totalCount);
        return totalCount;
    }

    /**
     * Creates a WorkpieceData object from a VisionResult.
     *
     * @param result         VisionResult from GET_PART_POS or GET_NEXT_PART_POS
     * @param referenceIndex Reference index (1-based)
     * @return WorkpieceData object
     */
    private biemhTekniker.lib.data.WorkpieceData createWorkpieceFromResult(VisionResult result, int referenceIndex)
    {
        biemhTekniker.lib.data.WorkpieceData wp = new biemhTekniker.lib.data.WorkpieceData(result.getX(), result.getY(), result.getZ(), result.getRx(), result.getRy(), result.getRz(), result.getScore());
        wp.setReferenceIndex(referenceIndex);
        return wp;
    }

    public VisionResult execute(Command cmd, String args, boolean expectReply)
    {
        String message = cmd.getCode();
        if (args != null && !args.isEmpty())
        {
            message += ";" + args;
        }
        log.debug("Sending " + message + " to cam.");
        String rawResponse = _client.sendAndReceive(message, expectReply);
        VisionResult result = new VisionResult(rawResponse, cmd);
        log.debug(result.toString());

        if (!result.isSuccess())
        {
            log.warn("Command " + cmd + " failed or returned no data.");
        }

        return result;
    }

    public VisionResult execute(Command cmd, boolean expectReply)
    {
        return execute(cmd, null, expectReply);
    }

    /**
     * Enum mapping English command names to their respective protocol codes.
     */
    public enum Command
    {
        LOAD_REFERENCE("15"), 
        SET_AUTO_MODE("101"), 
        SET_CALIB_MODE("102"), 
        CAPTURE_DATA("2"), 
        LOCATE_CONTAINER("3"), 
        GET_CONTAINER_POS("8"), 
        LOCATE_PARTS("4"), 
        GET_PART_POS("9"), 
        GET_NEXT_PART_POS("11"), 
        ADD_CALIB_POINT("5"), 
        CALIBRATE("6"), 
        TEST_CALIB("7"), 
        SEND_ROBOT_POSE("14"), 
        SEND_CUSTOM_MESSAGE("103"),
        SEND_WORKPIECE_SCAN_REQUEST("10"),
        REQUEST_WORKPIECE_ORIENTATION("13");

        private final String code;

        Command(String code)
        {
            this.code = code;
        }

        public String getCode()
        {
            return code;
        }
    }

    /**
     * Internal class to handle and parse server responses.
     */
    public static class VisionResult
    {
        private final boolean success;
        private final Command _cmd;
        private final double[] data;
        private final String raw;

        public VisionResult(String rawResponse, Command cmd)
        {
            this.raw = rawResponse;
            this._cmd = cmd;
            if (rawResponse == null || rawResponse.isEmpty())
            {
                this.success = false;
                this.data = new double[0];
                return;
            }

            // Cleanup characters: ( ) and whitespace
            String cleaned = rawResponse.replace("(", "").replace(")", "").trim();
            String[] parts = cleaned.split(",");

            this.success = parts[0].trim().equals("0");
            this.data = new double[parts.length];

            for (int i = 0; i < parts.length; i++)
            {
                try
                {
                    this.data[i] = Double.parseDouble(parts[i].trim());
                } catch (NumberFormatException e)
                {
                    this.data[i] = 0.0;
                }
            }
        }

        public boolean isSuccess()
        {
            return success;
        }

        // --- Getters with Index Switching Logic ---

        public double getWorkpieceRefWithOrientation()
        {
            return getDataSafe(1);
        }

        public double getX()
        {
            int index = (_cmd == Command.GET_CONTAINER_POS) ? 5 : 2;
            return getDataSafe(index);
        }

        private double getDataSafe(int index)
        {
            return (data.length > index) ? data[index] : 0.0;
        }

        public double getY()
        {
            int index = (_cmd == Command.GET_CONTAINER_POS) ? 6 : 3;
            return getDataSafe(index);
        }

        public double getZ()
        {
            int index = (_cmd == Command.GET_CONTAINER_POS) ? 7 : 4;
            return getDataSafe(index);
        }

        public double getRx()
        {
            int index = (_cmd == Command.GET_CONTAINER_POS) ? 8 : 5;
            return getDataSafe(index);
        }

        public double getRy()
        {
            int index = (_cmd == Command.GET_CONTAINER_POS) ? 9 : 6;
            return getDataSafe(index);
        }

        public double getRz()
        {
            int index = (_cmd == Command.GET_CONTAINER_POS) ? 10 : 7;
            return getDataSafe(index);
        }

        public double getScore()
        {
            // Container score is at 11, Parts usually at 8 or similar depending on gripper count
            // Defaulting to Container index (11) if not specified, safe check applied.
            int index = 11;
            return getDataSafe(index);
        }

        @Override
        public String toString()
        {
            return "VisionResult{success=" + success + ", raw='" + raw + "'}";
        }
    }
}