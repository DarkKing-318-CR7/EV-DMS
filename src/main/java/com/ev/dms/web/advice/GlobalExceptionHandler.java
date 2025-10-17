package com.ev.dms.web.advice;

import com.ev.dms.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusiness(BusinessException ex) {
        return ex.getMessage();
    }
}
