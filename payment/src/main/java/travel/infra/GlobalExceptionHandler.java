package travel.infra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Logger는 특정 이벤트를 로그로 출력하는 데 사용됩니다.
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // @ExceptionHandler 어노테이션을 통해 RollBackException가 발생했을때 해당 메서드가 실행되도록 설정합니다.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RollBackException e) {
        // RollBackException 발생했을 때 메시지를 로그로 출력합니다.
        logger.error("예상치 못한 오류로 결제정보 저장에 실패했습니다 : " + e.getMessage());
        // RollBackException 발생했을 때 HTTP 상태 코드 400과 함께 메시지를 반환합니다.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("예상치 못한 오류로 결제정보 저장에 실패했습니다 : " + e.getMessage());
    }
}