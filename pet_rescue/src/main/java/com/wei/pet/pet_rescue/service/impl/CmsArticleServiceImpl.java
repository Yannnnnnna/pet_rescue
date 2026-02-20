package com.wei.pet.pet_rescue.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wei.pet.pet_rescue.common.BizType;
import com.wei.pet.pet_rescue.entity.CmsArticle;
import com.wei.pet.pet_rescue.entity.CmsArticleLike;
import com.wei.pet.pet_rescue.entity.dto.article.ArticleFormDTO;
import com.wei.pet.pet_rescue.entity.dto.article.ArticleQueryDTO;
import com.wei.pet.pet_rescue.entity.vo.CheckResultVO;
import com.wei.pet.pet_rescue.mapper.CmsArticleMapper;
import com.wei.pet.pet_rescue.service.ICmsArticleLikeService;
import com.wei.pet.pet_rescue.service.ICmsArticleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 文章/百科表 服务实现类
 * </p>
 *
 * @author yanna
 * @since 2026-01-10
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CmsArticleServiceImpl extends ServiceImpl<CmsArticleMapper, CmsArticle> implements ICmsArticleService {
    @Autowired
    @Lazy
    private ICmsArticleLikeService likeService;

    private final InteractionServiceImpl interactionService;


    /**
     * 保存文章
     * @param dto
     * @return
     */
    @Override
    public boolean saveArticle(ArticleFormDTO dto) {
        CmsArticle article = new CmsArticle();
        BeanUtils.copyProperties(dto, article);

        // 自动生成摘要：如果前端没传摘要，且内容不为空，截取前50个字
        if (!StringUtils.hasText(article.getSummary()) && StringUtils.hasText(article.getContent())) {
            // 简单去除HTML标签（正则）
            String text = article.getContent().replaceAll("<[^>]*>", "");
            article.setSummary(text.length() > 50 ? text.substring(0, 50) + "..." : text);
        }
        return this.saveOrUpdate(article); // MyBatis-Plus 提供的 saveOrUpdate，根据ID是否有值自动判断
    }

    /**
     * 查询百科/壁纸
     * @param query
     * @return
     */
    @Override
    public IPage<CmsArticle> getArticlePage(ArticleQueryDTO query) {
        Page<CmsArticle> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<CmsArticle> wrapper = new LambdaQueryWrapper<>();

        // 动态查询条件
        wrapper.like(StringUtils.hasText(query.getKeyword()), CmsArticle::getTitle, query.getKeyword())
                .or()
                .like(StringUtils.hasText(query.getKeyword()), CmsArticle::getSummary, query.getKeyword());

        // 关键：如果要查“百科”，前端传 type=0；查“壁纸”，前端传 type=3
        wrapper.eq(query.getType() != null, CmsArticle::getType, query.getType());
        wrapper.like(StringUtils.hasText(query.getCategory()), CmsArticle::getCategory, query.getCategory());

        // 按创建时间倒序
        wrapper.orderByDesc(CmsArticle::getCreateTime);

        return this.page(page, wrapper);
    }

    /**
     * 获取文章详细
     * @param id
     * @return
     */
    @Override
    public CmsArticle getDetail(Long id) {
        CmsArticle article = this.getById(id);

        // 增加阅读量 (简单的 +1 操作，高并发下需优化，毕设这样写没问题)
        interactionService.incrementView(BizType.ARTICLE, id);
//        if (article != null) {
//            article.setViewCount(article.getViewCount() + 1);
//            this.updateById(article);
//        }
        // 2. 查用户是否点赞
        // (如果没登录，直接设为 false)
        CheckResultVO result = interactionService.getLikeInfo(BizType.ARTICLE, id, StpUtil.getLoginIdAsLong());
        if (result != null) {
            log.info("通过redis获取文章点赞信息: {}", result);
            article.setIsLiked(result.getChecked());
            article.setLikeCount(Math.toIntExact(result.getCount()));
        } else {
            log.info("通过数据库获取文章点赞信息");
            Long userId = StpUtil.getLoginIdAsLong();
            boolean isLiked = false;
            if (userId != null) {
                long count = likeService.lambdaQuery()
                        .eq(CmsArticleLike::getArticleId, article.getId())
                        .eq(CmsArticleLike::getUserId, userId)
                        .count();
                isLiked = count > 0;
            }
            article.setIsLiked(isLiked);
            article.setLikeCount(article.getLikeCount() == null ? 0 : article.getLikeCount());
        }

        return article;
    }
}
