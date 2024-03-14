package travel.exception;

public class RollbackException extends RuntimeException {
    private int statusCode;
    private String error;
    private String exceptionName;

    public RollbackException(String message, int statusCode, String error) {
        super(message);
        this.statusCode = statusCode;
        this.error = error;
        this.exceptionName = this.getClass().getSimpleName();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getError() {
        return error;
    }

    public String getExceptionName() {
        return exceptionName;
    }
}