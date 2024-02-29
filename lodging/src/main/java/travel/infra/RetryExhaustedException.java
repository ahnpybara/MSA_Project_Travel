package travel.infra;

// 사용자가 생성한 런타임예외를 상속받는 예외
public class RetryExhaustedException extends RuntimeException {
    public RetryExhaustedException(String message) {
        super(message);
    }
}