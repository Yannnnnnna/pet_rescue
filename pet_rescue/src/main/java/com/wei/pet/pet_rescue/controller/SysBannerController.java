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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    private final RedisTemplate<String, Object> redisTemplate;
    // 定义缓存的 Key
    private static final String BANNER_CACHE_KEY = "home:banner:list";
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
        // 如果更新后是显示状态，先删除缓存，等下次请求时重新加载最新数据
        if (banner.getStatus() == 1) redisTemplate.delete(BANNER_CACHE_KEY);
        return Result.success(sysBannerService.updateById(banner));

    }

    @DeleteMapping("/{id}")
    @SaCheckRole("admin")
    @Operation(summary = "删除轮播图")
    public Result<Boolean> deleteBanner(@PathVariable Long id) {
        // 如果更新后是显示状态，先删除缓存，等下次请求时重新加载最新数据
        if (sysBannerService.getById(id).getStatus() == 1) redisTemplate.delete(BANNER_CACHE_KEY);
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
        List<SysBanner> cacheList = (List<SysBanner>) redisTemplate.opsForValue().get(BANNER_CACHE_KEY);

        // 2. 【判】如果有数据，直接返回，不查数据库了（省流！）
        if (cacheList != null && !cacheList.isEmpty()) {
            System.out.println("🚀 走了 Redis 缓存，速度飞快！");
            return Result.success(cacheList);
        }

        // 3. 【库】Redis 没有，只能去查 MySQL
        System.out.println("🐢 Redis 没数据，正在查 MySQL...");
        List<SysBanner> dbList = sysBannerService.lambdaQuery()
                .eq(SysBanner::getStatus, 1) // 只查显示中的
                .orderByAsc(SysBanner::getSortOrder) // 按排序升序
                .list();
        if (dbList != null && !dbList.isEmpty()) {
            // 参数：Key, Value, 过期时间, 时间单位
            // 这里设置 30 分钟过期，保证数据不会永久不更新
            redisTemplate.opsForValue().set(BANNER_CACHE_KEY, dbList, 30, TimeUnit.MINUTES);
        }
        return Result.success(dbList);
    }



}
