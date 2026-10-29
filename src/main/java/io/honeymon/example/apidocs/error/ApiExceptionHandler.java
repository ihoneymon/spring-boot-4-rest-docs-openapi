package io.honeymon.example.apidocs.error;

import java.time.Instant;

import io.honeymon.example.apidocs.product.ProductNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiError handleProductNotFound(ProductNotFoundException exception) {
        return new ApiError("PRODUCT_NOT_FOUND", exception.getMessage(), Instant.now());
    }
}
