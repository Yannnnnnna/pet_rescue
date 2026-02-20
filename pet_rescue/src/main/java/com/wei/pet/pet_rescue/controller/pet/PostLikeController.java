package com.wei.pet.pet_rescue.controller.pet;


import cn.dev33.satoken.stp.StpUtil;
import com.wei.pet.pet_rescue.common.BizType;
import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.CmsArticleLike;
import com.wei.pet.pet_rescue.entity.PostLike;
import com.wei.pet.pet_rescue.entity.dto.article.CmsArticleFavoriteDTO;
import com.wei.pet.pet_rescue.entity.vo.CheckResultVO;
import com.wei.pet.pet_rescue.service.IPostLikeService;
import com.wei.pet.pet_rescue.service.impl.InteractionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
@Slf4j
public class PostLikeController {
    private final IPostLikeService postLikeService;
    private final InteractionServiceImpl interactionService;
    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/like/{postId}")
    public Result<Long> toggleFavorite(@PathVariable Long postId) {
        log.info("======通过redis实现点赞功能，postId=" + postId + ", userId=" + StpUtil.getLoginIdAsLong());
        return Result.success(interactionService.toggleLike(BizType.DIARY, postId, StpUtil.getLoginIdAsLong()));
    }
    @Operation(summary = "检查是否已点赞")
    @GetMapping("/check")
    public Result<CheckResultVO> checkFavorite(@RequestParam Long postId) {
        Long userId = StpUtil.getLoginIdAsLong();
//        boolean exists = postLikeService.lambdaQuery()
//                .eq(PostLike::getUserId, userId)
//                .eq(PostLike::getPostId,articleId)
//                .exists();
//        return Result.success(exists);
        log.info("======通过redis检查是否已点赞，postId=" + postId + ", userId=" + userId);
        return Result.success(interactionService.getLikeInfo(BizType.DIARY, postId, userId));
    }


}
