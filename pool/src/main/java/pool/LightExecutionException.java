package pool;

public class LightExecutionException extends Exception {
    public LightExecutionException() {}

    public LightExecutionException(String message) {
        super(message);
    }

    public LightExecutionException(Throwable error) {
        super(error);
    }
}
