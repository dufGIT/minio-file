package com.file.resource.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author df
 * @Description: 统一返回对象
 * @Date 2023/10/17 14:06
 */
@Data
@AllArgsConstructor
public class Result<T> implements Serializable {
    private static final long serialVersionUID = -3826891916021780628L;
    private String code;
    private String msg;
    private T data;

    // 成功只传数据
    public static <T> Result<T> success() {
        return new Result<>(Constants.ResponseCode.SUCCESS.getCode(), Constants.ResponseCode.SUCCESS.getMsg(), null);
    }

    // 成功只传数据
    public static <T> Result<T> success(T data) {
        return new Result<>(Constants.ResponseCode.SUCCESS.getCode(), Constants.ResponseCode.SUCCESS.getMsg(), data);
    }

    // 成功传数据和信息
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(Constants.ResponseCode.SUCCESS.getCode(), msg, data);
    }

    //  全部参数都传
    public static <T> Result<T> success(String code, String msg, T data) {
        return new Result<>(code, msg, data);
    }


    public static Result<?> failed(String msg) {
        return new Result<>(Constants.ResponseCode.UN_ERROR.getCode(), msg, null);
    }

    public static Result<?> failed(String code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> failed(String code, String msg, T data) {
        return new Result<>(code, msg, data);
    }

    public static <T> Result<T> failed() {
        return new Result<>(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getMsg(), null);
    }

}
