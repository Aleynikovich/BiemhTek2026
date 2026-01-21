package biemhTekniker.logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogEntry
{
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    private final long timestamp;
    private final LogLevel level;
    private final String source;
    private final String message;

    public LogEntry(LogLevel level, String source, String message)
    {
        this.timestamp = System.currentTimeMillis();
        this.level = level;
        this.source = source;
        this.message = message;
    }

    @Override
    public String toString()
    {
        return String.format("[%s] %s | %-5s: %s",
                sdf.format(new Date(timestamp)), source, level, message);
    }

    public LogLevel getLevel()
    {
        return level;
    }

    public String getSource()
    {
        return source;
    }
}