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

    public VisionResult execute(Command cmd) {
        return execute(cmd, null);
    }

    public VisionResult execute(Command cmd, String args) {
        String message = cmd.getCode();
        if (args != null && !args.isEmpty()) {
            message += ";" + args;
        }


        String rawResponse = _client.sendAndReceive(message);
        VisionResult result = new VisionResult(rawResponse, cmd);
        log.debug(result.toString());

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
        private final Command _cmd;
        private final double[] data;
        private final String raw;

        public VisionResult(String rawResponse, Command cmd) {
            this.raw = rawResponse;
            this._cmd = cmd;
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

        // --- Getters with Index Switching Logic ---

        public double getX() {
            int index = (_cmd == Command.GET_CONTAINER_POS) ? 5 : 2;
            return getDataSafe(index);
        }

        public double getY() {
            int index = (_cmd == Command.GET_CONTAINER_POS) ? 6 : 3;
            return getDataSafe(index);
        }

        public double getZ() {
            int index = (_cmd == Command.GET_CONTAINER_POS) ? 7 : 4;
            return getDataSafe(index);
        }

        public double getRx() {
            int index = (_cmd == Command.GET_CONTAINER_POS) ? 8 : 5;
            return getDataSafe(index);
        }

        public double getRy() {
            int index = (_cmd == Command.GET_CONTAINER_POS) ? 9 : 6;
            return getDataSafe(index);
        }

        public double getRz() {
            int index = (_cmd == Command.GET_CONTAINER_POS) ? 10 : 7;
            return getDataSafe(index);
        }

        public double getScore() {
            // Container score is at 11, Parts usually at 8 or similar depending on gripper count
            // Defaulting to Container index (11) if not specified, safe check applied.
            int index = 11;
            return getDataSafe(index);
        }

        private double getDataSafe(int index) {
            return (data.length > index) ? data[index] : 0.0;
        }

        @Override
        public String toString() {
            return "VisionResult{success=" + success + ", raw='" + raw + "'}";
        }
    }
}