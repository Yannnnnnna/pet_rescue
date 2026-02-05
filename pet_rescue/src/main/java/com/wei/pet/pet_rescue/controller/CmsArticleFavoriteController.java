package com.wei.pet.pet_rescue.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.CmsArticle;
import com.wei.pet.pet_rescue.entity.CmsArticleFavorite;
import com.wei.pet.pet_rescue.entity.CmsArticleLike;
import com.wei.pet.pet_rescue.entity.dto.CmsArticleFavoriteDTO;
import com.wei.pet.pet_rescue.service.ICmsArticleFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 文章收藏表 前端控制器
 * </p>
 *
 * @author yanna
 * @since 2026-02-03
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms-article-favorite")
@Tag(name = "文章收藏管理")
public class CmsArticleFavoriteController {
    private final ICmsArticleFavoriteService articleFavoriteService;
    @Operation(summary = "收藏/取消收藏")
    @PostMapping("/toggle")
    public Result<Boolean> toggleFavorite(@RequestBody CmsArticleFavoriteDTO dto) {
        return Result.success(articleFavoriteService.toggleFavorite(dto));
    }
    @Operation(summary = "查询我的收藏列表")
    @GetMapping("/my")
    public Result<List<CmsArticle>> myFavoriteList(@RequestParam Integer type) {
        return Result.success(articleFavoriteService.myFavoriteList(type));

    }
    @Operation(summary = "检查是否已收藏")
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(@RequestParam Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean exists = articleFavoriteService.lambdaQuery()
                .eq(CmsArticleFavorite::getUserId, userId)
                .eq(CmsArticleFavorite::getArticleId,articleId)
                .exists();
        return Result.success(exists);
    }
}
