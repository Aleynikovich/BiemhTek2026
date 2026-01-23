package biemhTekniker.logger;

public class LogPublisher implements Runnable
{
    private final LogCollector _logCollector;
    private Thread _workerThread;
    private volatile boolean _running = false;
    private LogLevel _filterLevel = LogLevel.DEBUG;

    public LogPublisher(LogCollector collector)
    {
        this._logCollector = collector;
    }

    public void setFilterLevel(LogLevel level) {
        this._filterLevel = level;
    }

    public void start()
    {
        if (_running) return;
        _running = true;
        _workerThread = new Thread(this, "LogPublisher-Thread");
        _workerThread.setDaemon(true);
        _workerThread.start();
    }

    public void stop()
    {
        _running = false;
        if (_workerThread != null)
        {
            _workerThread.interrupt();
        }
    }

    @Override
    public void run() {
        while (_running) {
            LogEntry entry = _logCollector.pollEntry();
            if (entry != null) {
                if (entry.getLevel().ordinal() >= _filterLevel.ordinal()) {
                    System.out.println(entry);
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}