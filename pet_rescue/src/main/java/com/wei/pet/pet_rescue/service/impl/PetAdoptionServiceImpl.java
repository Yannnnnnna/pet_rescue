package com.wei.pet.pet_rescue.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wei.pet.pet_rescue.entity.PetAdoption;
import com.wei.pet.pet_rescue.entity.PetInfo;
import com.wei.pet.pet_rescue.entity.SysUser;
import com.wei.pet.pet_rescue.entity.dto.adopt.AdoptionApplyDTO;
import com.wei.pet.pet_rescue.entity.dto.adopt.AdoptionAuditDTO;
import com.wei.pet.pet_rescue.entity.vo.AdminAdoptionRecordVO;
import com.wei.pet.pet_rescue.entity.vo.AdoptionDetailVO;
import com.wei.pet.pet_rescue.entity.vo.AdoptionRecordVO;
import com.wei.pet.pet_rescue.entity.vo.UserInfoVO;
import com.wei.pet.pet_rescue.mapper.PetAdoptionMapper;
import com.wei.pet.pet_rescue.mapper.SysUserMapper;
import com.wei.pet.pet_rescue.service.IPetAdoptionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.pet.pet_rescue.service.IPetInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 领养申请表 服务实现类
 * </p>
 *
 * @author yanna
 * @since 2026-01-10
 */
@Service
@RequiredArgsConstructor
public class PetAdoptionServiceImpl extends ServiceImpl<PetAdoptionMapper, PetAdoption> implements IPetAdoptionService {
    private final IPetInfoService petInfoService;
    private final SysUserMapper userMapper;


    /**
     * 1. 提交申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean apply(AdoptionApplyDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        // A. 校验宠物状态
        PetInfo pet = petInfoService.getById(dto.getPetId());
        if (pet == null) throw new RuntimeException("宠物不存在");

        // 只能申请 "0-待领养" 的宠物
        if (pet.getStatus() != 0) {
            throw new RuntimeException("手慢了，该宠物已被领养或暂停申请");
        }

        // B. 禁止自己申请自己发布的宠物 (可选逻辑)
        if (pet.getPublisherId().equals(userId)) {
            throw new RuntimeException("您不能申请领养自己发布的宠物");
        }

        // C. 防重复申请
        Long count = this.lambdaQuery()
                .eq(PetAdoption::getUserId, userId)
                .eq(PetAdoption::getPetId, dto.getPetId())
                .in(PetAdoption::getStatus, 1) // 申请中
                .count();
        if (count > 0) {
            throw new RuntimeException("您已申请过该宠物，请耐心等待送养人回复");
        }

        // D. 入库
        PetAdoption adoption = new PetAdoption();
        BeanUtils.copyProperties(dto, adoption);
        adoption.setUserId(userId);
        adoption.setStatus(0); // 设置为申请中
        return this.save(adoption);
    }

    /**
     * 2. 审核申请 (由发布者操作)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(AdoptionAuditDTO dto) {
        // A. 获取申请记录
        PetAdoption adoption = this.getById(dto.getId());
        if (adoption == null) throw new RuntimeException("申请记录不存在");
        if (adoption.getStatus() != 0) throw new RuntimeException("该记录已审核，请勿重复操作");

        // B. 获取宠物并校验权限 (关键！)
        PetInfo pet = petInfoService.getById(adoption.getPetId());
        if (!pet.getPublisherId().equals(dto.getOperatorId())) {
            throw new RuntimeException("无权操作！只有该宠物的发布者才能审核。");
        }

        if (dto.getPass()) {
            // === 通过流程 ===

            // 再次校验宠物是否还是待领养 (防止并发)
            if (pet.getStatus() != 0) {
                throw new RuntimeException("操作失败，该宠物状态异常");
            }

            // 1. 更新当前申请 -> 1 (通过)
            adoption.setStatus(1);
            adoption.setAdminRemark("恭喜！送养人同意了您的领养申请。");
            this.updateById(adoption);

            // 2. 更新宠物状态 -> 2 (已领养)
            pet.setStatus(2);
            petInfoService.updateById(pet);

            // 3. 自动驳回其他竞争者
            List<PetAdoption> others = this.lambdaQuery()
                    .eq(PetAdoption::getPetId, pet.getId())
                    .ne(PetAdoption::getId, adoption.getId()) // 排除自己
                    .eq(PetAdoption::getStatus, 0) // 只处理待审核的
                    .list();

            for (PetAdoption other : others) {
                other.setStatus(2); // 驳回
                other.setAdminRemark("送养人已选择其他领养家庭");
                this.updateById(other);
            }

        } else {
            // === 拒绝流程 ===
            adoption.setStatus(2); // 驳回
            adoption.setAdminRemark(dto.getRemark() == null ? "不符合领养条件" : dto.getRemark());
            this.updateById(adoption);
            // 宠物状态保持不变
        }
        return true;
    }

    /**
     * 查找申请领养我的宠物的申请列表
     * @param userId
     * @return
     */
    @Override
    public List<AdoptionRecordVO> applyMyPet(Long userId) {
        // 直接调用 Mapper 自定义的 SQL
        return baseMapper.selectReceivedApplications(userId);
    }

    /**
     * 取消申请
     * @param id
     * @return
     */
    @Override
    public boolean cancel(Long id) {
        // 1. 查询申请记录
        PetAdoption adoption = this.getById(id);
        if (adoption == null) {
            throw new RuntimeException("申请记录不存在");
        }

        // 2. 权限校验：只能取消自己的申请
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (!adoption.getUserId().equals(currentUserId)) {
            throw new RuntimeException("非法操作：您无权取消他人的申请");
        }

        // 3. 状态校验：只有“待审核”状态允许取消
        // 如果状态是 1(已通过)，则宠物状态已变更，不能简单取消，需联系送养人
        // 如果状态是 2(已驳回) 或 3(已取消)，则无需操作
        if (adoption.getStatus() != 1) {
            throw new RuntimeException("当前状态不可取消（仅待审核申请可取消）");
        }

        // 4. 执行更新
        adoption.setStatus(3); // 设置为 3-已取消
        return this.updateById(adoption);
    }

    @Override
    public AdoptionDetailVO getAdoptionDetail(Long id) {
        // 1. 查询申请记录是否存在
        PetAdoption adoption = this.getById(id);
        if (adoption == null) {
            throw new RuntimeException("申请记录不存在");
        }

        // 2. 初始化 VO 并复制基础属性 (利用BeanUtil简化代码)
        AdoptionDetailVO vo = new AdoptionDetailVO();
        BeanUtils.copyProperties(adoption, vo);

        // 3. 补全：申请人账号信息 (SysUser)
        SysUser user = userMapper.selectById(adoption.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }

        // 4. 补全：宠物信息 (PetInfo)
        // 假设你的宠物实体叫 PetInfo
        PetInfo pet = petInfoService.getById(adoption.getPetId());
        if (pet != null) {
            vo.setPetName(pet.getName());
            vo.setPetCover(pet.getCoverImg());
        }

        return vo;

    }

    /**
     * 分页查询领养申请信息
     * @param page
     * @param status
     * @param petName
     * @return
     */
    @Override
    public IPage<AdminAdoptionRecordVO> getAdminPage(Page<AdminAdoptionRecordVO> page, Integer status, String petName) {
        return baseMapper.selectAdminPage(page, status, petName);
    }

    @Override
    public UserInfoVO getByPetId(Long petId) {
        // 构造查询条件
        LambdaQueryWrapper<PetAdoption> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetAdoption::getPetId, petId)
                .eq(PetAdoption::getStatus, 1); // 关键：只查询状态为 1 (通过) 的记录

        // 查询数据库
        PetAdoption adoptionInfo = this.getOne(queryWrapper);

        if (adoptionInfo == null) {
            throw new RuntimeException("该宠物暂无成功领养记录");
        }
        SysUser user = userMapper.selectById(adoptionInfo.getUserId());
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        return userInfoVO;
    }
}
