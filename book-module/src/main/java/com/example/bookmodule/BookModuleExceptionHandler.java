package com.example.bookmodule;

import com.example.bookmodule.config.jwt.JwtAuthenticationException;
import com.example.bookmodule.exception.BookAlreadyExistsException;
import com.example.bookmodule.exception.NoSuchBookException;
import com.example.bookmodule.exception.NoSuchOrderException;
import com.example.bookmodule.exception.OrderAlreadyExistsException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class BookModuleExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value
            = { NoSuchOrderException.class, NoSuchBookException.class })
    public ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(),
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value
            = { OrderAlreadyExistsException.class, BookAlreadyExistsException.class })
    public ResponseEntity<Object> handleDuplicate(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value
            = {JwtAuthenticationException.class})
    public ResponseEntity<Object> handleAuthenticationException(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(),
                new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }
}
