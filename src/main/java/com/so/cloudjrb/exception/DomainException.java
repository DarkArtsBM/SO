package com.so.cloudjrb.exception;

// Esta anotação faz o Spring retornar um erro 400 (Bad Request)
// automaticamente quando esta exceção é lançada por um Controller.
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}