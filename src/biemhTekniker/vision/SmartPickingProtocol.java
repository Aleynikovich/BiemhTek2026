package biemhTekniker.vision;

import biemhTekniker.logger.Logger;

/**
 * Communication protocol for the SmartPicking system.
 * Translates logical commands into TCP frames and parses complex responses.
 */
public class SmartPickingProtocol {

    private static final Logger log = Logger.getLogger(SmartPickingProtocol.class);
    private final VisionSocketClient _client;

    /**
     * Enum mapping English command names to their respective protocol codes.
     */
    public enum Command {
        LOAD_REFERENCE("15"),
        SET_AUTO_MODE("101"),
        SET_CALIB_MODE("102"),
        CAPTURE_DATA("2"),
        LOCATE_CONTAINER("3"),
        GET_CONTAINER_POS("8"),
        LOCATE_PARTS("4"),
        GET_PART_POS("9"),
        GET_NEXT_PART_POS("11");

        private final String code;
        Command(String code) { this.code = code; }
        public String getCode() { return code; }
    }

    public SmartPickingProtocol(VisionSocketClient client) {
        this._client = client;
    }

    /**
     * Loads a specific reference by name.
     * Follows the sequence required by the manual: 15;ref -> 19 (reset) -> 15;ref
     */
    public boolean loadReference(String name) {
        execute(Command.LOAD_REFERENCE, name);
        _client.sendAndReceive("19"); // Internal cleanup/reset command
        VisionResult res = execute(Command.LOAD_REFERENCE, name);
        return res.isSuccess();
    }

    /**
     * Changes the application's operating mode.
     */
    public boolean setMode(Command mode) {
        if (mode != Command.SET_AUTO_MODE && mode != Command.SET_CALIB_MODE) {
            log.error("Invalid mode command requested.");
            return false;
        }
        return execute(mode).isSuccess();
    }

    /**
     * Executes a basic command without additional parameters.
     */
    public VisionResult execute(Command cmd) {
        return execute(cmd, null);
    }

    /**
     * Executes a command with arguments (e.g., "4;1;1" or "15;refName").
     */
    public VisionResult execute(Command cmd, String args) {
        String message = cmd.getCode();
        if (args != null && !args.isEmpty()) {
            message += ";" + args;
        }

        String rawResponse = _client.sendAndReceive(message);
        VisionResult result = new VisionResult(rawResponse);

        if (!result.isSuccess()) {
            log.warn("Command " + cmd + " failed or returned no data.");
        }

        return result;
    }

    /**
     * Internal class to handle and parse server responses.
     */
    public static class VisionResult {
        private final boolean success;
        private final double[] data;
        private final String raw;

        public VisionResult(String rawResponse) {
            this.raw = rawResponse;
            if (rawResponse == null || rawResponse.isEmpty()) {
                this.success = false;
                this.data = new double[0];
                return;
            }

            // Cleanup characters: ( ) and whitespace
            String cleaned = rawResponse.replace("(", "").replace(")", "").trim();
            String[] parts = cleaned.split(",");

            this.success = parts[0].trim().equals("0");
            this.data = new double[parts.length];

            for (int i = 0; i < parts.length; i++) {
                try {
                    this.data[i] = Double.parseDouble(parts[i].trim());
                } catch (NumberFormatException e) {
                    this.data[i] = 0.0;
                }
            }
        }

        public boolean isSuccess() { return success; }

        /**
         * Getters for Coordinates (applicable to commands 8, 9, 11).
         * Indices based on the user manual:
         * - Container (8): X=5, Y=6, Z=7 (meters)
         * - Parts (9/11): X=2, Y=3, Z=4 (meters)
         */

        public double getX() {
            return data.length > 5 ? data[5] : 0;
        }
        public double getY() {
            return data.length > 6 ? data[6] : 0;
        }
        public double getZ() {
            return data.length > 7 ? data[7] : 0;
        }

        public double getRx() { return data.length > 8 ? data[8] : 0; }
        public double getRy() { return data.length > 9 ? data[9] : 0; }
        public double getRz() { return data.length > 10 ? data[10] : 0; }

        public double getScore() {
            // Index 11 for Container (8). For Parts (9), 11 is usually part of grasping point info.
            return data.length > 11 ? data[11] : 0;
        }

        @Override
        public String toString() {
            return "VisionResult{success=" + success + ", raw='" + raw + "'}";
        }
    }
}