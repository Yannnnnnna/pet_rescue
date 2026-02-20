package com.wei.pet.pet_rescue.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wei.pet.pet_rescue.common.BizType;
import com.wei.pet.pet_rescue.entity.PetAdoption;
import com.wei.pet.pet_rescue.entity.PetInfo;
import com.wei.pet.pet_rescue.entity.SysUser;
import com.wei.pet.pet_rescue.entity.dto.adopt.AdoptPetsDTO;
import com.wei.pet.pet_rescue.entity.dto.pet.PetDTO;
import com.wei.pet.pet_rescue.entity.dto.pet.PetQueryDTO;
import com.wei.pet.pet_rescue.mapper.PetInfoMapper;
import com.wei.pet.pet_rescue.service.IPetAdoptionService;
import com.wei.pet.pet_rescue.service.IPetInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.pet.pet_rescue.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

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
@RequiredArgsConstructor
public class PetInfoServiceImpl extends ServiceImpl<PetInfoMapper, PetInfo> implements IPetInfoService {
    private final ISysUserService sysUserService;
    @Lazy
    @Autowired
    private  IPetAdoptionService petAdoptionService;

    private final RedisTemplate<String, Object> redisTemplate;
    private final InteractionServiceImpl interactionService;
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
        // 浏览量增加1
        interactionService.incrementView(BizType.PET, id);
        log.info("🚀 通过 InteractionServiceImpl 增加宠物浏览量，ID: {}", id);
//        pet.setViewCount(pet.getViewCount() + 1); // 浏览量加1
//        this.updateById(pet);
        return dto;
    }
    /**
     * 分页查询宠物列表
     * @param query
     * @return
     */
    @Override
    public IPage<PetInfo> getPetPage(PetQueryDTO query) {
        Page<PetInfo> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<PetInfo> wrapper = new LambdaQueryWrapper<>();

        if (query.getStatus() != null) {
            wrapper.eq(PetInfo::getStatus, query.getStatus());
        }

        // 必须加 if 判断！只有 keyword 不为空时，才拼接 AND (...)
        String keyword = query.getKeyword();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(PetInfo::getName, keyword)
                    .or()
                    .like(PetInfo::getBreed, keyword));
        }

        // 3. 其他动态条件
        wrapper.eq(query.getType() != null, PetInfo::getType, query.getType());
        wrapper.eq(StringUtils.hasText(query.getCity()), PetInfo::getCity, query.getCity());

        // 4. 排序
        wrapper.orderByDesc(PetInfo::getCreateTime);

        return this.page(page, wrapper);
    }

    /**
     * 获取我创建的宠物
     * @param id
     * @return
     */
    @Override
    public List<PetInfo> getMyPets(Long id) {
        return this.list(new LambdaQueryWrapper<PetInfo>().eq(PetInfo::getPublisherId, id));
    }

    /**
     * 查询我沟通过的宠物
     * @param id
     * @return
     */
    @Override
    public List<PetInfo> getMyChattedPets(Long id) {
        // 逻辑翻译：查询 PetInfo，条件是 ID 必须存在于 (SELECT DISTINCT pet_id FROM pet_consultation WHERE ask_user_id = userId)
        return this.list(new LambdaQueryWrapper<PetInfo>()
                // 使用 inSql 生成子查询，直接在数据库层面去重
                .inSql(PetInfo::getId, "SELECT DISTINCT pet_id FROM pet_consultation WHERE ask_user_id = " + id + " AND is_deleted = 0")
                .orderByDesc(PetInfo::getCreateTime)); // 可选：按时间倒序

    }

    /**
     * 获取我领养的宠物
     * @param id
     * @return
     */
    @Override
    public List<AdoptPetsDTO> getAdoptedPets(Long id) {
        // 1. 查询“我”名下状态为“1-已通过”的领养记录
        // 注意：这里查的是 pet_adoption 表
        List<PetAdoption> adoptions = petAdoptionService.lambdaQuery()
                .eq(PetAdoption::getUserId, id)
                .eq(PetAdoption::getStatus, 4) // 重点：只查签署协议的
                .orderByDesc(PetAdoption::getUpdateTime) // 按领养时间倒序
                .list();

        if (adoptions.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 收集关键 ID
        // 宠物ID集合
        Set<Long> petIds = adoptions.stream().map(PetAdoption::getPetId).collect(Collectors.toSet());
        // 建立 宠物ID -> 领养记录 的映射 (为了下面方便取 adoptionTime)
        Map<Long, PetAdoption> adoptionMap = adoptions.stream()
                .collect(Collectors.toMap(PetAdoption::getPetId, a -> a, (k1, k2) -> k1));

        // 3. 批量查询宠物基本信息 (pet_info 表)
        List<PetInfo> pets = this.listByIds(petIds);

        // 4. 收集发布人 ID
        Set<Long> publisherIds = pets.stream().map(PetInfo::getPublisherId).collect(Collectors.toSet());

        // 5. 批量查询发布人信息 (sys_user 表)
        Map<Long, SysUser> publisherMap = sysUserService.listByIds(publisherIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        // 6. 组装最终 DTO
        List<AdoptPetsDTO> result = new ArrayList<>();

        for (PetInfo pet : pets) {
            AdoptPetsDTO dto = new AdoptPetsDTO();

            // 6.1 填充宠物信息
            BeanUtils.copyProperties(pet, dto); // 复制 name, breed, sex 等同名属性
            dto.setCover(pet.getCoverImg()); // 如果字段名不一致(cover vs coverImg)需手动set

            // 6.2 填充领养时间 (从 map 取)
            PetAdoption adoption = adoptionMap.get(pet.getId());
            if (adoption != null) {
                // 使用 updateTime 作为领养成功的时刻
                dto.setAdoptionTime(adoption.getUpdateTime());
                dto.setSignatureImg(adoption.getSignatureImg());
                dto.setSignTime(adoption.getSignTime());
            }

            // 6.3 填充送养人信息
            SysUser publisher = publisherMap.get(pet.getPublisherId());
            if (publisher != null) {
                dto.setPublisherId(publisher.getId());
                dto.setPublisherNickname(publisher.getNickname());
                dto.setPublisherAvatar(publisher.getAvatar());
            } else {
                // 防止送养人账号注销导致报错
                dto.setPublisherNickname("未知用户");
            }

            result.add(dto);
        }

        return result;
    }

}
