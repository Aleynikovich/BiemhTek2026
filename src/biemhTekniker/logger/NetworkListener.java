package biemhTekniker.logger;

import java.io.PrintWriter;

public class NetworkListener implements ILogListener
{
    private final PrintWriter _out;

    public NetworkListener(PrintWriter out)
    {
        this._out = out;
    }

    @Override
    public void onNewLog(String message)
    {
        if (_out != null)
        {
            _out.println(message);
        }
    }
}
