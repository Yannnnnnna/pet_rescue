package com.wei.pet.pet_rescue.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.wei.pet.pet_rescue.entity.PetFeedRecord;
import com.wei.pet.pet_rescue.entity.PetInfo;
import com.wei.pet.pet_rescue.entity.SysUser;
import com.wei.pet.pet_rescue.entity.dto.pet.PetFeedDTO;
import com.wei.pet.pet_rescue.mapper.PetFeedRecordMapper;
import com.wei.pet.pet_rescue.service.IPetFeedRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.pet.pet_rescue.service.IPetInfoService;
import com.wei.pet.pet_rescue.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 宠物投喂记录表 服务实现类
 * </p>
 *
 * @author yanna
 * @since 2026-02-09
 */
@Service
@RequiredArgsConstructor
public class PetFeedRecordServiceImpl extends ServiceImpl<PetFeedRecordMapper, PetFeedRecord> implements IPetFeedRecordService {
    private final IPetInfoService petInfoService;
    private final ISysUserService sysUserService;

    /**
     * 投喂宠物小鱼干
     *
     * @param feedDTO
     * @return
     */
    @Transactional
    @Override
    public Integer feedPet(PetFeedDTO feedDTO) {
        //1. 检查账户余额
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserService.getById(userId);
        PetInfo petInfo = petInfoService.getById(feedDTO.getPetId());
        if (petInfo == null) {
            throw new RuntimeException("宠物不存在");
        }
        if (user.getCoin() >= feedDTO.getFeedCount()) {
            user.setCoin(user.getCoin() - feedDTO.getFeedCount());
            sysUserService.updateById(user);

            // 2. 更新宠物的爱心值
            petInfo.setLovePoint(petInfo.getLovePoint() + feedDTO.getFeedCount());
            petInfoService.updateById(petInfo);

            // 3. 创建投喂记录
            PetFeedRecord record = new PetFeedRecord();
            record.setPetId(feedDTO.getPetId());
            record.setUserId(StpUtil.getLoginIdAsLong());
            record.setAmount(feedDTO.getFeedCount());
            record.setCreateTime(LocalDateTime.now());
            this.save(record);
        }
        return petInfo.getLovePoint();

    }
}
