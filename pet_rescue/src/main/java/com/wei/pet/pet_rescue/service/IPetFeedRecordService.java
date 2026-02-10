package com.wei.pet.pet_rescue.service;

import com.wei.pet.pet_rescue.entity.PetFeedRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.pet.pet_rescue.entity.dto.pet.PetFeedDTO;

/**
 * <p>
 * 宠物投喂记录表 服务类
 * </p>
 *
 * @author yanna
 * @since 2026-02-09
 */
public interface IPetFeedRecordService extends IService<PetFeedRecord> {

    Integer feedPet(PetFeedDTO feedDTO);
}
