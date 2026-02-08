package biemhTekniker.tasks;

/**
 * Result of a program task execution.
 * Contains status and optional message/data.
 */
public class TaskResult
{
    private final boolean success;
    private final String  message;
    private final Object  data;

    public TaskResult(boolean success, String message)
    {
        this.success = success;
        this.message = message;
        this.data    = null;
    }

    public TaskResult(boolean success, String message, Object data)
    {
        this.success = success;
        this.message = message;
        this.data    = data;
    }

    public static TaskResult success(String message)
    {
        return new TaskResult(true, message);
    }

    public static TaskResult success(String message, Object data)
    {
        return new TaskResult(true, message, data);
    }

    public static TaskResult failure(String message)
    {
        return new TaskResult(false, message);
    }

    public boolean isSuccess()
    {
        return success;
    }

    public String getMessage()
    {
        return message;
    }

    public Object getData()
    {
        return data;
    }

    @Override public String toString()
    {
        return "TaskResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
