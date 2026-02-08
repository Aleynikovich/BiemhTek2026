package biemhTekniker.tasks;

/**
 * Result of a program task execution.
 * Java 7 compatible.
 */
public class TaskResult {
    
    private boolean success;
    private String message;
    private Exception error;
    
    public TaskResult(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.error = null;
    }
    
    public TaskResult(boolean success, String message, Exception error) {
        this.success = success;
        this.message = message;
        this.error = error;
    }
    
    public static TaskResult success(String message) {
        return new TaskResult(true, message);
    }
    
    public static TaskResult failure(String message) {
        return new TaskResult(false, message);
    }
    
    public static TaskResult failure(String message, Exception error) {
        return new TaskResult(false, message, error);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Exception getError() {
        return error;
    }
    
    @Override
    public String toString() {
        return "TaskResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                (error != null ? ", error=" + error.getMessage() : "") +
                '}';
    }
}
