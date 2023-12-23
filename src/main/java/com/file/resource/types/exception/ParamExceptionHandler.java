package com.file.resource.types.exception;

import com.file.resource.types.common.Constants;
import com.file.resource.types.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @Author df
 * @Description: 参数异常统一处理
 * @Date 2023/11/30 15:24
 */
@ControllerAdvice
@Slf4j
public class ParamExceptionHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ValidationException.class, MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public Result exceptionHandler(Exception e) {
        if (e instanceof MethodArgumentNotValidException) {

            // 封装实体，参数没有传递的校验
            // 参数检验异常
            MethodArgumentNotValidException methodArgumentNotValidException = (MethodArgumentNotValidException) e;
            Map<String, String> map = new HashMap<>();
            BindingResult result = methodArgumentNotValidException.getBindingResult();

            StringJoiner joiner = new StringJoiner(",");
            for (FieldError fieldError : result.getFieldErrors()) {
                String message = fieldError.getDefaultMessage();
                joiner.add(message);
            }
            log.error("数据校验出现错误：", e);
            return Result.failed(Constants.ResponseCode.VALIDATE_BAD_PARAM.getCode(), joiner.toString());
        } else if (e instanceof ConstraintViolationException) {
            ConstraintViolationException ex = (ConstraintViolationException) e;
            return Result.failed(Constants.ResponseCode.VALIDATE_BAD_PARAM.getCode(), Constants.ResponseCode.VALIDATE_BAD_PARAM.getMsg() + "," + ex.getMessage());
        } else if (e instanceof MethodArgumentTypeMismatchException) {
            log.error("请求参数类型错误：", e);
            MethodArgumentTypeMismatchException ex = (MethodArgumentTypeMismatchException) e;
            return Result.failed(Constants.ResponseCode.VALIDATE_BAD_PARAM_TYPE.getCode(),
                    Constants.ResponseCode.VALIDATE_BAD_PARAM_TYPE.getMsg());
        } else if (e instanceof MissingServletRequestParameterException) {
            log.error("请求参数类缺失：", e);
            return Result.failed(Constants.ResponseCode.VALIDATE_BAD_PARAM.getCode(), Constants.ResponseCode.VALIDATE_BAD_PARAM.getMsg());
        }

        log.error(e.getMessage());
        return Result.failed();
    }

}
