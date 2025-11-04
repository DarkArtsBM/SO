package com.so.cloudjrb.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

/**
 * Captura exceções globalmente para formatar a resposta de erro
 * exatamente como a API antiga fazia (um JSON com a chave "erro").
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, String>> handleDomainException(DomainException ex) {
        // Retorna um JSON: { "erro": "Mensagem da exceção" }
        return new ResponseEntity<>(Map.of("erro", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    // Exceção de "Não Encontrado"
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(Map.of("erro", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    // Captura qualquer outra exceção inesperada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        ex.printStackTrace(); // Loga o erro no console
        return new ResponseEntity<>(Map.of("erro", "Erro interno do servidor."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}