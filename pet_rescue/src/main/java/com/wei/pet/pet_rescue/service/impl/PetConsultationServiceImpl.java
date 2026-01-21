package com.wei.pet.pet_rescue.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.wei.pet.pet_rescue.entity.PetConsultation;
import com.wei.pet.pet_rescue.entity.PetInfo;
import com.wei.pet.pet_rescue.entity.dto.ConsultationAskDTO;
import com.wei.pet.pet_rescue.entity.dto.ConsultationReplyDTO;
import com.wei.pet.pet_rescue.entity.vo.ConsultationVO;
import com.wei.pet.pet_rescue.mapper.PetConsultationMapper;
import com.wei.pet.pet_rescue.service.IPetConsultationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.pet.pet_rescue.service.IPetInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 领养咨询表 服务实现类
 * </p>
 *
 * @author yanna
 * @since 2026-01-21
 */
@Service
public class PetConsultationServiceImpl extends ServiceImpl<PetConsultationMapper, PetConsultation> implements IPetConsultationService {
    @Autowired
    private IPetInfoService petInfoService;

    // 1. 发起提问
    public boolean ask(ConsultationAskDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        PetInfo pet = petInfoService.getById(dto.getPetId());
        if (pet == null) throw new RuntimeException("宠物不存在");

        // 禁止自问自答
        if (pet.getPublisherId().equals(currentUserId)) {
            throw new RuntimeException("不能向自己提问");
        }
        // =====================================================
        // 【新增】防重复提问校验
        // 逻辑：查询是否存在 (同宠物 + 同用户 + 同问题 + 待回复) 的记录
        // =====================================================
        Long count = this.lambdaQuery()
                .eq(PetConsultation::getPetId, dto.getPetId())
                .eq(PetConsultation::getAskUserId, currentUserId)
                .eq(PetConsultation::getQuestion, dto.getQuestion()) // 内容完全一致
                .eq(PetConsultation::getStatus, 0) // 且对方还没回
                .count();

        if (count > 0) {
            throw new RuntimeException("您已问过这个问题，请等待送养人回复后再试");
        }
        // =====================================================

        PetConsultation consultation = new PetConsultation();
        consultation.setPetId(dto.getPetId());
        consultation.setQuestion(dto.getQuestion());
        consultation.setAskUserId(currentUserId);
        consultation.setReplyUserId(pet.getPublisherId()); // 自动填入回答者ID
        consultation.setStatus(0); // 待回复

        return this.save(consultation);
    }

    // 2. 回复提问
    public boolean reply(ConsultationReplyDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        PetConsultation consultation = this.getById(dto.getId());
        if (consultation == null) throw new RuntimeException("咨询记录不存在");

        // 权限校验：只有被提问的人（送养人）才能回复
        if (!consultation.getReplyUserId().equals(currentUserId)) {
            throw new RuntimeException("无权回复此问题");
        }

        consultation.setAnswer(dto.getAnswer());
        consultation.setStatus(1); // 已回复
        consultation.setReplyTime(LocalDateTime.now());

        return this.updateById(consultation);
    }

    // 3. 列表查询
    public List<ConsultationVO> getMyReceivedList() {
        return baseMapper.selectReceived(StpUtil.getLoginIdAsLong());
    }

    public List<ConsultationVO> getMyAskedList() {
        return baseMapper.selectAsked(StpUtil.getLoginIdAsLong());
    }
}
