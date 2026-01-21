package biemhTekniker.logger;

public class LogPublisher implements Runnable {
    private final LogCollector _LogCollector;
    private Thread _workerThread;
    private volatile boolean _running = false;

    public LogPublisher(LogCollector collector) {
        this._LogCollector = collector;
    }

    public void start() {
        if (_running) return;
        _running = true;
        _workerThread = new Thread(this, "LogPublisher-Thread");
        _workerThread.setDaemon(true);
        _workerThread.start();
    }

    public void stop() {
        _running = false;
        if (_workerThread != null) {
            _workerThread.interrupt();
        }
    }

    @Override
    public void run() {
        while (_running) {
            String msg = _LogCollector.pollMessage();
            if (msg != null) {
                System.out.println("[RobotLog] " + msg);
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