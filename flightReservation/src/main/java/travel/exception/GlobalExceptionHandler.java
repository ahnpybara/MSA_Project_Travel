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
    public ResponseEntity<String> handleRuntimeException(RollBackException e) {

        logger.error("\n예상치 못한 오류로 예약이 롤백 되었습니다\n", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("예상치 못한 오류로 예약이 롤백 되었습니다" + e.getMessage());
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustomException(CustomException e) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("statusCode", e.getStatusCode());
        errorDetails.put("error", e.getError());
        errorDetails.put("exceptionType", e.getExceptionName());
        errorDetails.put("message", e.getMessage());

        return ResponseEntity.status(e.getStatusCode()).body(errorDetails);
    }
    // 응답을 반환 하기위해 사용하는 예외 핸들러
    @ExceptionHandler(ResponseException.class)
    public ResponseEntity<String> handleReservationException(ResponseException e) {
        logger.error("\n"+e.getMessage()+"\n", e);
        return new ResponseEntity<>(e.getMessage(), e.getStatus());
    }

}
