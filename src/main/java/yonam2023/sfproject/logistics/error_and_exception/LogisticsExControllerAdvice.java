package yonam2023.sfproject.logistics.error_and_exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class LogisticsExControllerAdvice {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> noSuchElementException(NoSuchElementException e){
        log.error("[exception Handler] ex", e);
        return new ResponseEntity<>("이미 처리된 작업입니다.", HttpStatus.BAD_REQUEST);
    }
}
