package com.wei.pet.pet_rescue.service;

import com.wei.pet.pet_rescue.entity.PetConsultation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.pet.pet_rescue.entity.dto.ConsultationAskDTO;
import com.wei.pet.pet_rescue.entity.dto.ConsultationReplyDTO;
import com.wei.pet.pet_rescue.entity.dto.ReturnVisitDTO;
import com.wei.pet.pet_rescue.entity.vo.ChatSessionVO;
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

    List<ChatSessionVO> getMyReceivedList();

    List<ChatSessionVO> getMyAskedList();

    List<ConsultationVO> getPetConsultation(Long petId, Long applicantId);

    List<ConsultationSummaryVO> getPetConsultationSummary(Long petId);

    boolean startReturnVisit(ReturnVisitDTO dto);
}
