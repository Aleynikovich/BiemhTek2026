package biemhTekniker.logger;

public class LogPublisher implements Runnable
{
    private final LogCollector _LogCollector;
    private Thread _workerThread;
    private volatile boolean _running = false;

    public LogPublisher(LogCollector collector)
    {
        this._LogCollector = collector;
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
    public void run()
    {
        while (_running)
        {
            LogEntry entry = _LogCollector.pollMessage();
            if (entry != null)
            {
                System.out.println(entry);
            } else
            {
                try
                {
                    Thread.sleep(100);
                } catch (InterruptedException e)
                {
                    break;
                }
            }
        }
    }
}