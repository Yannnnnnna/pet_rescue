package com.wei.pet.pet_rescue.controller.cms;


import cn.dev33.satoken.stp.StpUtil;
import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.CmsArticleLike;
import com.wei.pet.pet_rescue.entity.dto.article.CmsArticleFavoriteDTO;
import com.wei.pet.pet_rescue.service.ICmsArticleLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yanna
 * @since 2026-02-03
 */
@Tag(name = "文章点赞管理")
@RestController
@RequestMapping("/cms-article-like")
@RequiredArgsConstructor
public class CmsArticleLikeController {
    private final ICmsArticleLikeService articleLikeService;

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/like")
    public Result<Integer> toggleFavorite(@RequestBody CmsArticleFavoriteDTO dto) {
        return Result.success(articleLikeService.toggleLike(dto));
    }
    @Operation(summary = "检查是否已点赞")
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(@RequestParam Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean exists = articleLikeService.lambdaQuery()
                .eq(CmsArticleLike::getUserId, userId)
                .eq(CmsArticleLike::getArticleId,articleId)
                .exists();
        return Result.success(exists);
    }
}
