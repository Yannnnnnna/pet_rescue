package com.wei.pet.pet_rescue.service;

import com.wei.pet.pet_rescue.entity.PetAdoption;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.pet.pet_rescue.entity.dto.AdoptionApplyDTO;
import com.wei.pet.pet_rescue.entity.dto.AdoptionAuditDTO;
import com.wei.pet.pet_rescue.entity.vo.AdoptionRecordVO;

import java.util.List;

/**
 * <p>
 * 领养申请表 服务类
 * </p>
 *
 * @author yanna
 * @since 2026-01-10
 */
public interface IPetAdoptionService extends IService<PetAdoption> {

    boolean apply(AdoptionApplyDTO dto);

    boolean audit(AdoptionAuditDTO dto);

    List<AdoptionRecordVO> applyMyPet(Long userId);

    boolean cancel(Long id);
}
