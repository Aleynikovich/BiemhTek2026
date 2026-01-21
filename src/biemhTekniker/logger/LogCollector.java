package biemhTekniker.logger;

import java.util.concurrent.ConcurrentLinkedQueue;

public class LogCollector implements ILogListener
{
    private final ConcurrentLinkedQueue<LogEntry> entries = new ConcurrentLinkedQueue<LogEntry>();

    @Override
    public void onNewLog(LogEntry entry)
    {
        entries.add(entry);
    }

    public LogEntry pollMessage()
    {
        return entries.poll();
    }
}
