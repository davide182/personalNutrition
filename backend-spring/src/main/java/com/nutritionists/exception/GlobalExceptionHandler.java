package com.nutritionists.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Gestisce le eccezioni del nostro AuthService (registrazione, login, stato account)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        String message = ex.getMessage();
        error.put("error", message);
        error.put("status", HttpStatus.BAD_REQUEST.toString());
        
        //  Determina lo status code in base al tipo di errore
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        if (message.contains("disabilitato")) {
            error.put("error", message);
            status = HttpStatus.UNAUTHORIZED;
        } else if (message.contains("attesa di revisione")) {
            error.put("error", message);
            status = HttpStatus.UNAUTHORIZED;
        } else if (message.contains("attesa di approvazione")) {
            error.put("error", message);
            status = HttpStatus.UNAUTHORIZED;
        } else if (message.contains("Account sospeso")) {
            error.put("error", message);
            status = HttpStatus.UNAUTHORIZED;
        } else if (message.contains("Email già in uso")) {
            error.put("error", message);
            status = HttpStatus.CONFLICT;
        } else if (message.contains("Email o password non validi")) {
            error.put("error", message);
            status = HttpStatus.UNAUTHORIZED;
        }
        
        return ResponseEntity.status(status).body(error);
    }

    // Gestisce errori di validazione (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        errors.put("error", "Dati non validi: " + String.join(", ", errors.values()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
    
    // Gestisce errori di autenticazione di Spring Security (opzionale)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Credenziali non valide. Verifica email e password.");
        error.put("status", HttpStatus.UNAUTHORIZED.toString());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}