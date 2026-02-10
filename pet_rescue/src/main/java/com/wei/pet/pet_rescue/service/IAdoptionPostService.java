package com.wei.pet.pet_rescue.service;

import com.wei.pet.pet_rescue.entity.AdoptionPost;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.pet.pet_rescue.entity.dto.adopt.AdoptionPostDTO;
import com.wei.pet.pet_rescue.entity.dto.adopt.AdoptionPostQueryDTO;
import com.wei.pet.pet_rescue.entity.dto.adopt.ReviewDTO;

import java.util.List;

/**
 * <p>
 * 领养生活记录贴 服务类
 * </p>
 *
 * @author yanna
 * @since 2026-02-09
 */
public interface IAdoptionPostService extends IService<AdoptionPost> {

    boolean saveAdoptionPost(AdoptionPostDTO adoptionPostDTO);

    boolean reviewAdoptionPost(ReviewDTO status);

    List<AdoptionPost> listAdoptionPosts(AdoptionPostQueryDTO queryDTO);
}
