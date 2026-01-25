package com.wei.pet.pet_rescue.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.SysUser;
import com.wei.pet.pet_rescue.entity.dto.*;
import com.wei.pet.pet_rescue.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(summary ="测试环境登录接口")
    @PostMapping("/login")
    public Result<String> devLogin(@RequestParam Long id){
        StpUtil.login(id);
        return Result.success("登录成功", StpUtil.getTokenValue());
    }

    // ================== Web端管理相关 ==================

    @Operation(summary = "分页查询用户列表", description = "支持按角色、状态、关键字筛选")
    @PostMapping("/list")
    public Result<IPage<SysUser>> getUserList(@RequestBody UserQueryDTO query) {
        return Result.success(sysUserService.getUserPage(query));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/detail/{id}")
    public Result<SysUser> getUserDetail(@PathVariable Long id) {
        return Result.success(sysUserService.getById(id));
    }



    @Operation(summary = "删除用户 (慎用)", description = "逻辑删除")
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        // 防止删除超级管理员 (ID=1)
        if (id == 1L) {
            return Result.error("超级管理员无法删除");
        }
        return Result.success(sysUserService.removeById(id));
    }

    // ================== 个人中心 (Web/小程序通用) ==================

    @Operation(summary = "获取我的个人信息")
    @GetMapping("/my/info")
    public Result<SysUser> getMyInfo() {
        return Result.success(sysUserService.getMyInfo());
    }

    @Operation(summary = "修改个人信息 (昵称/头像/手机)")
    @PostMapping("/update/info")
    public Result<Boolean> updateInfo(@RequestBody UserUpdateDTO dto) {
        return Result.success(sysUserService.updateMyInfo(dto));
    }

    @Operation(summary = "修改密码")
    @PostMapping("/update/password")
    public Result<Boolean> updatePassword(@RequestBody @Valid UserPasswordDTO dto) {
        try {
            return Result.success(sysUserService.updatePassword(dto));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
