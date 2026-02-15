package com.wei.pet.pet_rescue.controller;


import cn.dev33.satoken.annotation.SaCheckRole;
import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.SysBanner;
import com.wei.pet.pet_rescue.service.ISysBannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Delete;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 首页轮播图表 前端控制器
 * </p>
 *
 * @author yanna
 * @since 2026-02-15
 */
@RestController
@RequestMapping("/sys-banner")
@RequiredArgsConstructor
@Tag(name = "首页轮播图接口")
public class SysBannerController {
    private final ISysBannerService sysBannerService;
    @PostMapping("/add")
    @SaCheckRole("admin") // 仅管理员可操作
    @Operation(summary = "添加轮播图")
    public Result<Boolean> addBanner(@RequestBody SysBanner banner) {
        // banner 对象里包含了前端传过来的 imgUrl
        // 默认排序可以设为 0，状态设为 1（显示）
        if (banner.getStatus() == null) banner.setStatus(1);
        if (banner.getSortOrder() == null) banner.setSortOrder(0);

        return Result.success(sysBannerService.save(banner));
    }
    @PostMapping("/update")
    @SaCheckRole("admin")
    @Operation(summary = "更新轮播图")
    public Result<Boolean> updateBanner(@RequestBody SysBanner banner) {
        if (banner.getId() == null) {
            return Result.error("ID不能为空");
        }
        return Result.success(sysBannerService.updateById(banner));
    }

    @DeleteMapping("/{id}")
    @SaCheckRole("admin")
    @Operation(summary = "删除轮播图")
    public Result<Boolean> deleteBanner(@PathVariable Long id) {
        return Result.success(sysBannerService.removeById(id));
    }

    @GetMapping("/list")
    @Operation(summary = "获取轮播图列表")
    @SaCheckRole("admin")
    public Result<List<SysBanner>> listBanners() {
        return Result.success(sysBannerService.list());
    }

    @GetMapping("/show")
    @Operation(summary = "获取显示中的轮播图列表")
    public Result<List<SysBanner>> showBanners() {
        return Result.success(sysBannerService.lambdaQuery()
                .eq(SysBanner::getStatus, 1)
                .orderByAsc(SysBanner::getSortOrder)
                .list());
    }



}
