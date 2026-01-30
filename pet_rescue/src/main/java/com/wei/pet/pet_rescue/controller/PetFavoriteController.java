package com.wei.pet.pet_rescue.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.PetFavorite;
import com.wei.pet.pet_rescue.entity.PetInfo;
import com.wei.pet.pet_rescue.entity.dto.PetFavoriteDTO;
import com.wei.pet.pet_rescue.service.IPetFavoriteService;
import com.wei.pet.pet_rescue.service.IPetInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 宠物收藏表 前端控制器
 * </p>
 *
 * @author yanna
 * @since 2026-01-27
 */
@Tag(name = "收藏管理")
@RestController
@RequestMapping("/pet-favorite")
@RequiredArgsConstructor
public class PetFavoriteController {
    private final IPetFavoriteService favoriteService;
    private final IPetInfoService petInfoService;
    @Operation(summary = "收藏/取消收藏")
    @PostMapping("/toggle")
    public Result<Boolean> toggleFavorite(@RequestBody PetFavoriteDTO dto) {
        return Result.success(favoriteService.toggleFavorite(dto));
    }
    @Operation(summary = "查询我的收藏列表")
    @GetMapping("/my")
    public Result<List<PetInfo>> myFavoriteList() {
        return Result.success(favoriteService.myFavoriteList());

    }
    @Operation(summary = "检查是否已收藏")
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(@RequestParam Long petId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean exists = favoriteService.lambdaQuery()
                .eq(PetFavorite::getUserId, userId)
                .eq(PetFavorite::getPetId, petId)
                .exists();
        return Result.success(exists);
    }
}
