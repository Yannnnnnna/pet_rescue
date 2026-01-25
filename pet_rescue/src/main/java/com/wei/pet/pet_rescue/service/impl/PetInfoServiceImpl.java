package com.wei.pet.pet_rescue.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wei.pet.pet_rescue.entity.PetInfo;
import com.wei.pet.pet_rescue.entity.dto.PetDTO;
import com.wei.pet.pet_rescue.entity.dto.PetQueryDTO;
import com.wei.pet.pet_rescue.mapper.PetInfoMapper;
import com.wei.pet.pet_rescue.service.IPetInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * <p>
 * 宠物档案表 服务实现类
 * </p>
 *
 * @author yanna
 * @since 2026-01-10
 */
@Service
@Slf4j
public class PetInfoServiceImpl extends ServiceImpl<PetInfoMapper, PetInfo> implements IPetInfoService {
    /**
     * 新增宠物
     * @param petForm
     * @return
     */
    @Override
    public boolean savePet(PetDTO petForm) {
        PetInfo petInfo = new PetInfo();
        BeanUtils.copyProperties(petForm, petInfo); // 复制基本属性

        // 特殊处理详情图 List -> String
        if (petForm.getDetailImgList() != null && !petForm.getDetailImgList().isEmpty()) {
            petInfo.setDetailImgs(String.join(",", petForm.getDetailImgList()));
        }
        // 默认状态处理
        if (petInfo.getStatus() == null) {
            petInfo.setStatus(0); // 默认为待领养
        }
        // todo：待确定是不是这么获取当前用户的id
        petInfo.setPublisherId( StpUtil.getLoginIdAsLong());
        log.info("当前用户ID{}", StpUtil.getLoginIdAsLong());
        return this.save(petInfo);
    }

    /**
     * 更新宠物信息
     * @param petForm
     * @return
     */
    @Override
    public boolean updatePet(PetDTO petForm) {
        PetInfo petInfo = new PetInfo();
        BeanUtils.copyProperties(petForm, petInfo);

        // 特殊处理详情图 List -> String
        if (petForm.getDetailImgList() != null) {
            petInfo.setDetailImgs(String.join(",", petForm.getDetailImgList()));
        }

        return this.updateById(petInfo);
    }

    /**
     * 获取宠物详细信息
     * @param id
     * @return
     */
    @Override
    public PetDTO getPetDetail(Long id) {
        PetInfo pet = this.getById(id);
        if(pet == null) return null;

        PetDTO dto = new PetDTO();
        BeanUtils.copyProperties(pet, dto);
        // String -> List
        if (StringUtils.hasText(pet.getDetailImgs())) {
            dto.setDetailImgList(Arrays.asList(pet.getDetailImgs().split(",")));
        }
        return dto;
    }

    /**
     * 分页查询
     * @param query
     * @return
     */
    @Override
    public IPage<PetInfo> getPetPage(PetQueryDTO query) {
        Page<PetInfo> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<PetInfo> wrapper = new LambdaQueryWrapper<>();

        // 动态拼接查询条件
        wrapper.like(StringUtils.hasText(query.getKeyword()), PetInfo::getName, query.getKeyword())
                .or()
                .like(StringUtils.hasText(query.getKeyword()), PetInfo::getBreed, query.getKeyword());

        wrapper.eq(query.getType() != null, PetInfo::getType, query.getType());
        wrapper.eq(query.getStatus() != null, PetInfo::getStatus, query.getStatus());
        wrapper.eq(StringUtils.hasText(query.getCity()), PetInfo::getCity, query.getCity());

        // 只查未删除的 (MyBatis-Plus配置了逻辑删除会自动加，这里可不写)
        wrapper.orderByDesc(PetInfo::getCreateTime);

        return this.page(page, wrapper);
    }
}
