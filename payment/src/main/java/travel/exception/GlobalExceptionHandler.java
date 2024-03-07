package travel.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

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