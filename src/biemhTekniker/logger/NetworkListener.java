package biemhTekniker.logger;

import java.io.PrintWriter;

public class NetworkListener implements ILogListener
{
    private final    PrintWriter _out;
    private volatile LogLevel    _minLevel = LogLevel.INFO;

    public NetworkListener(PrintWriter out)
    {
        this._out = out;
    }

    /**
     * Get the current minimum log level.
     *
     * @return The current minimum log level
     */
    public LogLevel getMinimumLevel()
    {
        return _minLevel;
    }

    /**
     * Set the minimum log level to be sent over the network.
     * Only logs with level >= minLevel will be sent.
     *
     * @param minLevel The minimum log level
     */
    public void setMinimumLevel(LogLevel minLevel)
    {
        this._minLevel = minLevel;
    }

    @Override public void onNewLog(LogEntry entry)
    {
        if (_out != null && entry.getLevel().ordinal() >= _minLevel.ordinal())
        {
            _out.println(entry);
        }
    }
}
