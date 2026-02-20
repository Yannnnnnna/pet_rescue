package com.wei.pet.pet_rescue.controller;

import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.common.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wyr on 2026/2/19
 */
@RestController
@RequestMapping("/api/sms")
@Tag(name = "短信验证码")
public class SmsController {

    @Autowired
    private SmsService smsService;

    /**
     * 发送验证码
     * @param phone 手机号
     * @param type 场景类型(1注册登录 2修改绑定 3重置密码 4绑定新手机)
     */
    @GetMapping("/send")
    @Operation(summary = "发送验证码", description = "type: 1注册登录 2修改绑定 3重置密码 4绑定新手机")
    public Result<String> send(@RequestParam String phone, @RequestParam Integer type) {
        try {
            smsService.sendCode(phone, type);
            return Result.success("验证码发送成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}