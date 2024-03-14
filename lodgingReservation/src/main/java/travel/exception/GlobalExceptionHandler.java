package travel.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // @ExceptionHandler 어노테이션을 통해 RollBackException가 발생했을때 해당 메서드가 실행되도록 설정합니다.
    @ExceptionHandler(RollBackException.class)
    public ResponseEntity<String> handleRollbackException(RollBackException e) {
        logger.error("\n예상치 못한 오류로 예약이 롤백 되었습니다\n", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("예상치 못한 오류로 예약이 롤백 되었습니다" + e.getMessage());
    }

    // TODO 주석 필요
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustomException(CustomException e) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("statusCode", e.getStatusCode());
        errorDetails.put("error", e.getError());
        errorDetails.put("exceptionType", e.getExceptionName());
        errorDetails.put("message", e.getMessage());

        return ResponseEntity.status(e.getStatusCode()).body(errorDetails);
    }
}
