package com.wei.pet.pet_rescue.service;

import com.wei.pet.pet_rescue.entity.CmsArticleLike;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.pet.pet_rescue.entity.dto.CmsArticleFavoriteDTO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yanna
 * @since 2026-02-03
 */
public interface ICmsArticleLikeService extends IService<CmsArticleLike> {

    Integer toggleLike(CmsArticleFavoriteDTO dto);

}
