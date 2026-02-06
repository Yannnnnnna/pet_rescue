package com.wei.pet.pet_rescue.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wei.pet.pet_rescue.entity.PetInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.pet.pet_rescue.entity.dto.adopt.AdoptPetsDTO;
import com.wei.pet.pet_rescue.entity.dto.pet.PetDTO;
import com.wei.pet.pet_rescue.entity.dto.pet.PetQueryDTO;

import java.util.List;

/**
 * <p>
 * 宠物档案表 服务类
 * </p>
 *
 * @author yanna
 * @since 2026-01-10
 */
public interface IPetInfoService extends IService<PetInfo> {

    boolean savePet(PetDTO petForm);

    boolean updatePet(PetDTO petForm);

    PetDTO getPetDetail(Long id);

    IPage<PetInfo> getPetPage(PetQueryDTO query);

    List<PetInfo> getMyPets(Long id);

    List<PetInfo> getMyChattedPets(Long id);

    List<AdoptPetsDTO> getAdoptedPets(Long id);
}
