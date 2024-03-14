package travel.exception;

public class RollBackException extends RuntimeException {
    public RollBackException(String message){
        super(message);
    }
}
