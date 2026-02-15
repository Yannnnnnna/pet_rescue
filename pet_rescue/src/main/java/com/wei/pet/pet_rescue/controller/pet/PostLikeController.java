package com.wei.pet.pet_rescue.controller.pet;


import cn.dev33.satoken.stp.StpUtil;
import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.CmsArticleLike;
import com.wei.pet.pet_rescue.entity.PostLike;
import com.wei.pet.pet_rescue.entity.dto.article.CmsArticleFavoriteDTO;
import com.wei.pet.pet_rescue.service.IPostLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 领养日记点赞记录表 前端控制器
 * </p>
 *
 * @author yanna
 * @since 2026-02-14
 */
@RestController
@RequestMapping("/post-like")
@Tag(name = "领养日记点赞记录表")
@RequiredArgsConstructor
public class PostLikeController {
    private final IPostLikeService postLikeService;
    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/like/{postId}")
    public Result<Integer> toggleFavorite(@PathVariable Long postId) {
        return Result.success(postLikeService.toggleLike(postId));
    }
    @Operation(summary = "检查是否已点赞")
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(@RequestParam Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean exists = postLikeService.lambdaQuery()
                .eq(PostLike::getUserId, userId)
                .eq(PostLike::getPostId,articleId)
                .exists();
        return Result.success(exists);
    }


}
