package travel.infra;

import org.springframework.http.HttpStatus;

public class ResponseExceptionCustom extends RuntimeException{
    private final HttpStatus status;
    
    public ResponseExceptionCustom(String message, HttpStatus status){
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus(){
        return status;
    }
}
