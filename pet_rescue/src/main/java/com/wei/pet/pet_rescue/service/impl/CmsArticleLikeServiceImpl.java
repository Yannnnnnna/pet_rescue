package com.wei.pet.pet_rescue.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wei.pet.pet_rescue.entity.CmsArticle;
import com.wei.pet.pet_rescue.entity.CmsArticleLike;
import com.wei.pet.pet_rescue.entity.dto.article.CmsArticleFavoriteDTO;
import com.wei.pet.pet_rescue.mapper.CmsArticleLikeMapper;
import com.wei.pet.pet_rescue.service.ICmsArticleLikeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.pet.pet_rescue.service.ICmsArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yanna
 * @since 2026-02-03
 */
@Service
@RequiredArgsConstructor
public class CmsArticleLikeServiceImpl extends ServiceImpl<CmsArticleLikeMapper, CmsArticleLike> implements ICmsArticleLikeService {
    private final ICmsArticleService articleService;

    /**
     * 点赞/取消点赞
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer toggleLike(CmsArticleFavoriteDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long id = dto.getArticleId();

        // 1. 检查是否已经点赞
        CmsArticleLike exist = this.lambdaQuery()
                .eq(CmsArticleLike::getUserId, userId)
                .eq(CmsArticleLike::getArticleId, id)
                .one();

        CmsArticle article = articleService.getById(id);
        if (article == null){
            throw new RuntimeException("文章不存在！");
        }

        if (exist != null) {
            // 2. 已存在 -> 执行取消点赞 (删除)
            this.removeById(exist.getId());
            articleService.update(null, new LambdaUpdateWrapper<CmsArticle>()
                    .setSql("like_count = like_count - 1")
                    .eq(CmsArticle::getId, id));

            return Math.max(0, article.getLikeCount() - 1);
        } else {
            // 3. 不存在 -> 执行点赞 (新增)
            CmsArticleLike like = new CmsArticleLike();
            like.setArticleId(id);
            like.setUserId(userId);
            this.save(like);
            // B. 文章数 +1
            articleService.update(null, new LambdaUpdateWrapper<CmsArticle>()
                    .setSql("like_count = like_count + 1") // 使用 SQL 更新防止并发覆盖
                    .eq(CmsArticle::getId, id));

            // 返回前端 +1 后的结果（用于校准）
            return article.getLikeCount() + 1;
        }
    }

}
