package com.technolearn.ms.kafka.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class LibraryEventsControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleRequestBody(MethodArgumentNotValidException e) {
        List<FieldError> errorList = e.getBindingResult().getFieldErrors();
        Map<String, String> errorMessage = errorList.stream()
                .collect(Collectors.toMap(
                        // Key mapper function: field name
                        fe -> fe.getField(),
                        // Value mapper function: error message
                        fe -> fe.getDefaultMessage(),
                        // Merge function in case of duplicate keys (not expected in this case)
                        (oldValue, newValue) -> newValue,
                        // Supplier for the Map implementation (HashMap in this case)
                        HashMap::new));

        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

}
