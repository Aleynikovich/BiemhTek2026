package biemhTekniker.programs;

import biemhTekniker.logger.Logger;
import biemhTekniker.vision.SmartPickingProtocol;
import biemhTekniker.vision.SmartPickingProtocol.Command;
import biemhTekniker.vision.SmartPickingProtocol.VisionResult;

/**
 * Vision task for individual vision commands.
 * Handles programs 101-108, 110.
 */
public class IndividualVisionCommandTask implements VisionTask
{
    private static final Logger log = Logger.getLogger(IndividualVisionCommandTask.class);

    private final Command command;
    private final String  customMessage;

    /**
     * Creates a task for a specific command.
     *
     * @param command Vision command to execute
     */
    public IndividualVisionCommandTask(Command command)
    {
        this.command       = command;
        this.customMessage = null;
    }

    /**
     * Creates a task for a custom message (command 110).
     *
     * @param command       Should be SEND_CUSTOM_MESSAGE
     * @param customMessage Custom message to send
     */
    public IndividualVisionCommandTask(Command command, String customMessage)
    {
        this.command       = command;
        this.customMessage = customMessage;
    }

    public void execute(VisionContext context) throws Exception
    {
        SmartPickingProtocol protocol = context.getProtocol();

        log.info("Executing vision command: " + command);

        if (command == Command.SEND_CUSTOM_MESSAGE && customMessage != null)
        {
            boolean success = protocol.sendCustomMessage(customMessage, true);
            log.info("Custom message sent: " + customMessage + " - success: " + success);
            return;
        }

        VisionResult result = protocol.execute(command, true);
        if (result.isSuccess())
        {
            log.info("Vision command completed successfully: " + command);
            log.debug("Result: " + result.toString());
        }
        else
        {
            log.error("Vision command failed: " + command, e);
            throw new Exception("Vision command failed: " + command);
        }
    }
}
