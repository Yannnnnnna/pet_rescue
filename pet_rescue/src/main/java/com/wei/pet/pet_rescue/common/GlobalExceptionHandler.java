package com.wei.pet.pet_rescue.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice // 核心注解：表示这是一个增强版的 Controller，用于处理全局异常
public class GlobalExceptionHandler {



    /**
     * 2. 捕获 Sa-Token 未登录异常
     * 当用户没登录就调用接口时触发
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLoginException(NotLoginException e) {
        log.warn("用户未登录：{}", e.getMessage());
        // 返回 401 状态码，前端收到后应该跳转到登录页
        return Result.error(Result.CODE_NO_LOGIN, "您尚未登录，请登录后操作");
    }

    /**
     * 1. 捕获业务异常 (RuntimeException)
     * 例如：throw new RuntimeException("库存不足");
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.error("业务异常信息：{}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 3. 捕获 Sa-Token 无权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<?> handleNotPermissionException(NotPermissionException e) {
        log.warn("无权限访问：{}", e.getMessage());
        return Result.error("您没有权限进行此操作");
    }

    /**
     * 4. 捕获系统级未知异常 (Exception)
     * 防止代码有 Bug 导致前端没响应，兜底策略
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统未知错误：", e); // 打印完整堆栈到控制台，方便排查
        return Result.error("系统繁忙，请稍后再试");
    }
}