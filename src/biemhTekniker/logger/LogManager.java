package biemhTekniker.logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogManager {
    private static final List<ILogListener> listeners = new CopyOnWriteArrayList<ILogListener>();

    private static final LinkedList<LogEntry> startupBuffer = new LinkedList<LogEntry>();
    private static final int MAX_BUFFER_SIZE = 50;

    public static synchronized void broadcast(LogEntry entry) {
        if (listeners.isEmpty()) {
            if (startupBuffer.size() >= MAX_BUFFER_SIZE) {
                startupBuffer.removeFirst();
            }
            startupBuffer.addLast(entry);
        } else {
            for (ILogListener listener : listeners) {
                listener.onNewLog(entry);
            }
        }
    }

    public static synchronized void register(ILogListener listener) {
        listeners.add(listener);
        while (!startupBuffer.isEmpty()) {
            listener.onNewLog(startupBuffer.pollFirst());
        }
    }

    public static void unregister(ILogListener listener) {
        listeners.remove(listener);
    }
}