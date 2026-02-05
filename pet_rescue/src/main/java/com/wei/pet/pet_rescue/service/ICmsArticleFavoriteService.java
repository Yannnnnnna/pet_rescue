package com.wei.pet.pet_rescue.service;

import com.wei.pet.pet_rescue.entity.CmsArticle;
import com.wei.pet.pet_rescue.entity.CmsArticleFavorite;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.pet.pet_rescue.entity.dto.CmsArticleFavoriteDTO;

import java.util.List;

/**
 * <p>
 * 文章收藏表 服务类
 * </p>
 *
 * @author yanna
 * @since 2026-02-03
 */
public interface ICmsArticleFavoriteService extends IService<CmsArticleFavorite> {

    boolean toggleFavorite(CmsArticleFavoriteDTO dto);

    List<CmsArticle> myFavoriteList(Integer type);
}
