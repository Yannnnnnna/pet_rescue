package com.wei.pet.pet_rescue.service;

import com.wei.pet.pet_rescue.entity.PostLike;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 领养日记点赞记录表 服务类
 * </p>
 *
 * @author yanna
 * @since 2026-02-14
 */
public interface IPostLikeService extends IService<PostLike> {

    Integer toggleLike(Long postId);
}
