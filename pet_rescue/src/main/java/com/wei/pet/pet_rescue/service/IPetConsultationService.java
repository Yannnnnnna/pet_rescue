package com.wei.pet.pet_rescue.service;

import com.wei.pet.pet_rescue.entity.PetConsultation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.pet.pet_rescue.entity.dto.ConsultationAskDTO;
import com.wei.pet.pet_rescue.entity.dto.ConsultationReplyDTO;
import com.wei.pet.pet_rescue.entity.vo.ConsultationSummaryVO;
import com.wei.pet.pet_rescue.entity.vo.ConsultationVO;

import java.util.List;

/**
 * <p>
 * 领养咨询表 服务类
 * </p>
 *
 * @author yanna
 * @since 2026-01-21
 */
public interface IPetConsultationService extends IService<PetConsultation> {

    boolean ask(ConsultationAskDTO dto);

    boolean reply(ConsultationReplyDTO dto);

    List<ConsultationVO> getMyReceivedList();

    List<ConsultationVO> getMyAskedList();

    List<PetConsultation> getPetConsultation(Long petId, Long applicantId);

    List<ConsultationSummaryVO> getPetConsultationSummary(Long petId);
}
