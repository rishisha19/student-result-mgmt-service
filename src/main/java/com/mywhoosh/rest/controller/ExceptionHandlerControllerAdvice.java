package com.mywhoosh.rest.controller;

import com.mywhoosh.exception.StudentMgmtException;
import com.mywhoosh.rest.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlerControllerAdvice {

    @ExceptionHandler(StudentMgmtException.StudentValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleStudentValidation(StudentMgmtException.StudentValidationException ex) {
        log.error("StudentValidationException: ", ex);
        return new ResponseEntity<>(ErrorResponse.builder().status("failed").message(ex.getMessage()).build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, WebExchangeBindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleArgumentValidation(Exception ex) {
        log.error("MethodArgumentNotValidException: ", ex);
        if(ex instanceof MethodArgumentNotValidException)
            return new ResponseEntity<>(ErrorResponse.builder()
                    .status("failed")
                    .message(Objects.requireNonNull(((MethodArgumentNotValidException) ex).getBindingResult().getFieldError()).getDefaultMessage())
                    .build(), HttpStatus.BAD_REQUEST);
        else if(ex instanceof WebExchangeBindException)
            return new ResponseEntity<>(ErrorResponse.builder()
                    .status("failed")
                    .message(Objects.requireNonNull(((WebExchangeBindException) ex).getBindingResult().getFieldError()).getDefaultMessage())
                    .build(), HttpStatus.BAD_REQUEST);
        else
        return new ResponseEntity<>(ErrorResponse.builder()
                .status("failed")
                .message(ex.getMessage())
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value= {StudentMgmtException.StudentNotFoundException.class,
            StudentMgmtException.NoResultFoundForStudent.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handleNotFound(StudentMgmtException ex) {
        log.error("StudentNotFoundException: ", ex);
        return new ResponseEntity<>(ErrorResponse.builder()
                .status("failed")
                .message(ex.getMessage())
                .build(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({StudentMgmtException.DuplicateRollNumberException.class,
            StudentMgmtException.InvalidRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> badRequest(StudentMgmtException ex) {
        log.error("DuplicateRollNumberException: ", ex);
        return new ResponseEntity<>(ErrorResponse.builder()
                .status("failed")
                .message(ex.getMessage())
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({StudentMgmtException.class, RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleAllExceptions(RuntimeException ex) {
        log.error("Exception Runtime: ", ex);
        return new ResponseEntity<>(ErrorResponse.builder()
                .status("failed")
                .message(ex.getMessage())
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @MessageExceptionHandler
    @SendTo("/queue/errors")
    public String handleWSException(Throwable exception) {
        log.error("Exception WS: ", exception);
        if(exception instanceof org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException)
            return Objects.requireNonNull(((org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException) exception)
                    .getBindingResult().getFieldError()).getDefaultMessage();
        else if(exception instanceof WebExchangeBindException)
            return Objects.requireNonNull(((WebExchangeBindException) exception).getBindingResult().getFieldError()).getDefaultMessage();
        else
         return exception.getLocalizedMessage();
    }
}
