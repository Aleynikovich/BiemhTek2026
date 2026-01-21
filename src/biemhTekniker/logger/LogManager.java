package biemhTekniker.logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogManager
{
    private static final List<ILogListener> listeners = new CopyOnWriteArrayList<ILogListener>();

    public static void broadcast(String message)
    {
        for (ILogListener listener : listeners)
        {
            listener.onNewLog(message);
        }
    }

    public static void register(ILogListener listener)
    {
        listeners.add(listener);
    }

    public static void unregister(ILogListener listener)
    {
        listeners.remove(listener);
    }
}
