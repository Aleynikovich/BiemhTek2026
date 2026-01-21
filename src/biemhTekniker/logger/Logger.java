package biemhTekniker.logger;

public class Logger {
    private final String _source;

    // The constructor is private; we use the static factory method below
    private Logger(String source) {
        this._source = source;
    }

    // This mimics how professional Java loggers are created
    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz.getSimpleName());
    }

    public void info(String msg) {
        LogManager.broadcast(new LogEntry(LogLevel.INFO, _source, msg));
    }

    public void warn(String msg) {
        LogManager.broadcast(new LogEntry(LogLevel.WARN, _source, msg));
    }

    public void error(String msg) {
        LogManager.broadcast(new LogEntry(LogLevel.ERROR, _source, msg));
    }

    public void debug(String msg) {
        LogManager.broadcast(new LogEntry(LogLevel.DEBUG, _source, msg));
    }
}