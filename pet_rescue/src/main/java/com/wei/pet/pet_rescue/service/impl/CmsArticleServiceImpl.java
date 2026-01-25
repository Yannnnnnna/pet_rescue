package com.wei.pet.pet_rescue.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wei.pet.pet_rescue.entity.CmsArticle;
import com.wei.pet.pet_rescue.entity.dto.ArticleFormDTO;
import com.wei.pet.pet_rescue.entity.dto.ArticleQueryDTO;
import com.wei.pet.pet_rescue.mapper.CmsArticleMapper;
import com.wei.pet.pet_rescue.service.ICmsArticleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
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
public class CmsArticleServiceImpl extends ServiceImpl<CmsArticleMapper, CmsArticle> implements ICmsArticleService {
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

        // TODO: (Phase 4) 这里后续需要调用 AI 接口，将文章内容向量化并在 VectorDB 中新增/更新

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
}
