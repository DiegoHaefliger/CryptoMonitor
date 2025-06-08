package com.haefliger.cryptomonitor.exception;

import com.haefliger.cryptomonitor.exception.dto.ApiErrorDetailResponse;
import com.haefliger.cryptomonitor.exception.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZonedDateTime;
import java.util.List;

//@ControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
//
//        List<ApiErrorDetailResponse> errors = ex.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .map(error -> ApiErrorDetailResponse.builder().defaultMessage(error.getDefaultMessage()).build())
//                .toList();
//
//        ApiErrorResponse response = ApiErrorResponse
//                .builder()
//                .timestamp(ZonedDateTime.now())
//                .status(HttpStatus.BAD_REQUEST.value())
//                .error("Bad Request")
//                .errors(errors)
//                .path(request.getRequestURI())
//                .build();

//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//        return null;
//    }

}
