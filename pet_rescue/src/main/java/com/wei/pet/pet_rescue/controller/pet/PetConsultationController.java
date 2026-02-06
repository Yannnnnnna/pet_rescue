package com.wei.pet.pet_rescue.controller.pet;


import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.dto.consult.ConsultationAskDTO;
import com.wei.pet.pet_rescue.entity.dto.consult.ConsultationReplyDTO;
import com.wei.pet.pet_rescue.entity.dto.consult.ReturnVisitDTO;
import com.wei.pet.pet_rescue.entity.vo.ChatSessionVO;
import com.wei.pet.pet_rescue.entity.vo.ConsultationSummaryVO;
import com.wei.pet.pet_rescue.entity.vo.ConsultationVO;
import com.wei.pet.pet_rescue.service.IPetConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 领养咨询表 前端控制器
 * </p>
 *
 * @author yanna
 * @since 2026-01-21
 */
@Tag(name = "领养咨询模块")
@RestController
@RequestMapping("/pet-consultation")
@RequiredArgsConstructor
public class PetConsultationController {

    private final IPetConsultationService consultationService;

    @Operation(summary = "发起提问 (领养人)")
    @PostMapping("/ask")
    public Result<Boolean> ask(@RequestBody @Validated ConsultationAskDTO dto) {
        return Result.success(consultationService.ask(dto));
    }

    @Operation(summary = "回复提问 (送养人)")
    @PostMapping("/reply")
    public Result<Boolean> reply(@RequestBody @Validated ConsultationReplyDTO dto) {
        return Result.success(consultationService.reply(dto));
    }

    @Operation(summary = "查询我收到的提问 (送养人视角)")
    @GetMapping("/my/received")
    public Result<List<ChatSessionVO>> getMyReceived() {
        return Result.success(consultationService.getMyReceivedList());
    }

    @Operation(summary = "查询我发起的提问 (领养人视角)")
    @GetMapping("/my/asked")
    public Result<List<ChatSessionVO>> getMyAsked() {
        return Result.success(consultationService.getMyAskedList());
    }

    @Operation(summary = "管理员-获取特定宠物的沟通记录")
    @GetMapping("/admin/history")
    public Result<List<ConsultationVO>> getHistoryForAdmin(
            @Parameter(description = "宠物ID") @RequestParam Long petId,
            @Parameter(description = "申请人ID(提问者)") @RequestParam Long applicantId) {

        List<ConsultationVO> list = consultationService.getPetConsultation(petId, applicantId);

        return Result.success(list);
    }

    @Operation(summary = "管理员-获取某只宠物的所有咨询用户列表")
    @GetMapping("/admin/summary")
    public Result<List<ConsultationSummaryVO>> getPetConsultationSummary(@RequestParam Long petId) {
        return Result.success(consultationService.getPetConsultationSummary(petId));
    }

    @Operation(summary = "发起回访 (送养人)")
    @PostMapping("/return-visit")
    public Result<Boolean> startReturnVisit(@RequestBody @Validated ReturnVisitDTO dto) {
        return Result.success(consultationService.startReturnVisit(dto));
    }
}
