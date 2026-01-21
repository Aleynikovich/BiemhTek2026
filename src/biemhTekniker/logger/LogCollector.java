package biemhTekniker.logger;

import java.util.concurrent.ConcurrentLinkedQueue;

public class LogCollector implements ILogListener
{
    private final ConcurrentLinkedQueue<String> messages = new ConcurrentLinkedQueue<String>();

    @Override
    public void onNewLog(String message)
    {
        messages.add(message);
    }

    public String pollMessage()
    {
        return messages.poll();
    }
}
