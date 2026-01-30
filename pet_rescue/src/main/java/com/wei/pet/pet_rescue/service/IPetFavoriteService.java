package com.wei.pet.pet_rescue.service;

import com.wei.pet.pet_rescue.entity.PetFavorite;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.pet.pet_rescue.entity.PetInfo;
import com.wei.pet.pet_rescue.entity.dto.PetFavoriteDTO;

import java.util.List;

/**
 * <p>
 * 宠物收藏表 服务类
 * </p>
 *
 * @author yanna
 * @since 2026-01-27
 */
public interface IPetFavoriteService extends IService<PetFavorite> {

    boolean toggleFavorite(PetFavoriteDTO dto);

    List<PetInfo> myFavoriteList();
}
