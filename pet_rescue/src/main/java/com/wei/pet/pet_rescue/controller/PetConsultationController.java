package com.wei.pet.pet_rescue.controller;


import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.dto.ConsultationAskDTO;
import com.wei.pet.pet_rescue.entity.dto.ConsultationReplyDTO;
import com.wei.pet.pet_rescue.entity.vo.ConsultationVO;
import com.wei.pet.pet_rescue.service.IPetConsultationService;
import io.swagger.v3.oas.annotations.Operation;
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
    public Result<List<ConsultationVO>> getMyReceived() {
        return Result.success(consultationService.getMyReceivedList());
    }

    @Operation(summary = "查询我发起的提问 (领养人视角)")
    @GetMapping("/my/asked")
    public Result<List<ConsultationVO>> getMyAsked() {
        return Result.success(consultationService.getMyAskedList());
    }
}
