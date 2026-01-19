package com.wei.pet.pet_rescue.controller;


import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.dto.AdminLoginDto;
import com.wei.pet.pet_rescue.entity.dto.WechatLoginDto;
import com.wei.pet.pet_rescue.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author yanna
 * @since 2026-01-10
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/sys-user")
public class SysUserController {
    @Resource
    private ISysUserService sysUserService;

    @Operation(summary = "Web端-管理员登录")
    @PostMapping("/admin/login")
    public Result<String> adminLogin(@RequestBody @Valid AdminLoginDto dto) {
        String token = sysUserService.loginAdmin(dto);
        return Result.success("登录成功", token);
    }

    @Operation(summary = "小程序-微信一键登录")
    @PostMapping("/mini/login")
    public Result<String> miniLogin(@RequestBody @Valid WechatLoginDto dto) {
        String token = sysUserService.loginByWechat(dto);
        return Result.success("登录成功", token);
    }
}
