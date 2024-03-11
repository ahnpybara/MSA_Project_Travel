package travel.exception;

public class RollbackException extends RuntimeException {
    public RollbackException(String message) {
        super(message);
    }
}