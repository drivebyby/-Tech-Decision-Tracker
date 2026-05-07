package com.example.fullstack.exception;

import com.example.fullstack.common.ApiResponse;
import com.example.fullstack.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(Exception exception) {
        String message = "参数校验失败";
        if (exception instanceof MethodArgumentNotValidException validException
                && validException.getBindingResult().hasErrors()) {
            message = validException.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        log.warn("参数校验失败: {}", message);
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusiness(BusinessException exception) {
        log.warn("业务异常: code={}, message={}", exception.getCode(), exception.getMessage());
        return ApiResponse.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception exception) {
        log.error("系统异常: {}", exception.getMessage(), exception);
        return ApiResponse.fail(500, exception.getMessage());
    }
}
