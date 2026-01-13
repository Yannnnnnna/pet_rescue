package com.wei.pet.pet_rescue.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果类
 * @param <T> 数据类型
 */
@Data
@Schema(description = "统一API响应结果")
public class Result<T> implements Serializable {

    @Schema(description = "状态码: 200-成功, 500-失败, 401-未登录")
    private Integer code;

    @Schema(description = "提示消息")
    private String msg;

    @Schema(description = "返回数据")
    private T data;

    // 定义常用状态码
    public static final Integer CODE_SUCCESS = 200;
    public static final Integer CODE_ERROR = 500;
    public static final Integer CODE_NO_LOGIN = 401;

    // --- 成功的方法 ---

    public static <T> Result<T> success() {
        return new Result<>(CODE_SUCCESS, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(CODE_SUCCESS, "操作成功", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(CODE_SUCCESS, msg, data);
    }

    // --- 失败的方法 ---

    public static <T> Result<T> error() {
        return new Result<>(CODE_ERROR, "操作失败", null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(CODE_ERROR, msg, null);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    // 私有构造方法
    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}