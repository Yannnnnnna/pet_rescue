package com.wei.pet.pet_rescue.controller.cms;


import cn.dev33.satoken.stp.StpUtil;
import com.wei.pet.pet_rescue.common.BizType;
import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.CmsArticleLike;
import com.wei.pet.pet_rescue.entity.dto.article.CmsArticleFavoriteDTO;
import com.wei.pet.pet_rescue.entity.vo.CheckResultVO;
import com.wei.pet.pet_rescue.service.ICmsArticleLikeService;
import com.wei.pet.pet_rescue.service.impl.InteractionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CmsArticleLikeController {
    private final ICmsArticleLikeService articleLikeService;
    private final InteractionServiceImpl interactionService;

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/like")
    public Result<Integer> toggleFavorite(@RequestBody CmsArticleFavoriteDTO dto) {
        log.info("======通过redis实现点赞功能，articleId=" + dto.getArticleId() + ", userId=" + StpUtil.getLoginIdAsLong());
        return Result.success(interactionService
                .toggleLike(BizType.ARTICLE, dto.getArticleId(), StpUtil.getLoginIdAsLong())
                .intValue());
//        return Result.success(articleLikeService.toggleLike(dto));
    }
    @Operation(summary = "检查是否已点赞")
    @GetMapping("/check")
    public Result<CheckResultVO> checkFavorite(@RequestParam Long articleId) {

        Long userId = StpUtil.getLoginIdAsLong();
        CheckResultVO checkResultVO = interactionService.getLikeInfo(BizType.ARTICLE, articleId, userId);
//        boolean exists = articleLikeService.lambdaQuery()
//                .eq(CmsArticleLike::getUserId, userId)
//                .eq(CmsArticleLike::getArticleId,articleId)
//                .exists();
//        return Result.success(exists);
        log.info("=========通过redis检查点赞状态，articleId=" + articleId + ", userId=" + userId + ", checkResult=" + checkResultVO);
        return Result.success(checkResultVO);
    }
}
