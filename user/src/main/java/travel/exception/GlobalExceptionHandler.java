package travel.exception;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;

import com.auth0.jwt.exceptions.JWTDecodeException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 클라이언트로 구체적인 정보를 제공하고 싶을 때
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", e.getMessage());
        logger.error("요청 처리 중 오류 발생: ", e); // 예외 로깅
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    // 보안을 중시해서 간결한 메시지만 전달하고 싶을 때
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // HTTP 상태 코드 설정
    public String handleNoSuchElementException(NoSuchElementException e) {
        logger.error("요청 처리 중 오류 발생: ", e); // 예외 로깅
        return "요청한 리소스를 찾을 수 없습니다."; // 사용자가 이해하기 쉬운 메시지, 불필요한 정보 노출 방지
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // HTTP 상태 코드 설정
    public String handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        logger.error("데이터베이스 작업 중 충돌: ", e); // 예외 로깅
        return "작업 중 충돌이 발생했습니다."; // 사용자가 이해하기 쉬운 메시지, 불필요한 정보 노출 방지
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED) // HTTP 상태 코드 설정
    public String handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        logger.error("허락되지 않은 메소드 요청: ", e); // 예외 로깅
        return "지원하지 않는 요청입니다."; // 사용자가 이해하기 쉬운 메시지, 불필요한 정보 노출 방지
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // HTTP 상태 코드 설정
    public String handleGeneralException(Exception e) {
        logger.error("요청 처리 중 오류 발생: ", e); // 예외 로깅
        return "서버 내부 오류가 발생했습니다."; // 사용자가 이해하기 쉬운 메시지, 불필요한 정보 노출 방지
    }

    @ExceptionHandler(JWTDecodeException.class)
    public ResponseEntity<Map<String, String>> handlerJWTDecodeException(JWTDecodeException e) {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String,String>> handleRuntimeException(IOException e) {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException e) {
        logger.error("인증 실패: ", e); // 예외 로깅
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디나 비밀번호가 일치하지 않습니다.");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String,String>> handleRuntimeEx(RuntimeException e) {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
    }
}
