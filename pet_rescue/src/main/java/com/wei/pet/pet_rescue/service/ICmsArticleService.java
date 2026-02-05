package com.wei.pet.pet_rescue.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wei.pet.pet_rescue.entity.CmsArticle;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.pet.pet_rescue.entity.dto.ArticleFormDTO;
import com.wei.pet.pet_rescue.entity.dto.ArticleQueryDTO;

/**
 * <p>
 * 文章/百科表 服务类
 * </p>
 *
 * @author yanna
 * @since 2026-01-10
 */
public interface ICmsArticleService extends IService<CmsArticle> {

    boolean saveArticle(ArticleFormDTO dto);

    IPage<CmsArticle> getArticlePage(ArticleQueryDTO query);

    CmsArticle getDetail(Long id);
}
