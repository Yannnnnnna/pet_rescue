package com.wei.pet.pet_rescue.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.PetAdoption;
import com.wei.pet.pet_rescue.entity.dto.AdoptionApplyDTO;
import com.wei.pet.pet_rescue.entity.dto.AdoptionAuditDTO;
import com.wei.pet.pet_rescue.entity.vo.AdoptionRecordVO;
import com.wei.pet.pet_rescue.service.IPetAdoptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 领养申请表 前端控制器
 * </p>
 *
 * @author yanna
 * @since 2026-01-10
 */
@Tag(name = "领养管理模块")
@RestController
@RequestMapping("/pet-adoption")
@RequiredArgsConstructor
public class PetAdoptionController {

    private final IPetAdoptionService adoptionService;

    @Operation(summary = "提交领养申请")
    @PostMapping("/apply")
    public Result<Boolean> apply(@RequestBody @Validated AdoptionApplyDTO dto) {
        try {
            return Result.success(adoptionService.apply(dto));
        } catch (RuntimeException e) {
            System.out.println(e);
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "送养人审核申请")
    @PostMapping("/audit")
    public Result<Boolean> audit(@RequestBody @Validated AdoptionAuditDTO dto) {
        try {
            dto.setOperatorId(StpUtil.getLoginIdAsLong());
            return Result.success(adoptionService.audit(dto));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "查询我提交的申请 (我是领养人)")
    @GetMapping("/my/apply")
    public Result<IPage<PetAdoption>> listMyApply(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<PetAdoption> page = new Page<>(pageNum, pageSize);
        Long userId = StpUtil.getLoginIdAsLong();
        // 查询 user_id = me
        adoptionService.lambdaQuery()
                .eq(PetAdoption::getUserId, userId)
                .orderByDesc(PetAdoption::getCreateTime)
                .page(page);

        return Result.success(page);
    }

    @Operation(summary = "查询谁申请了我的宠物 (我是送养人)")
    @GetMapping("/my/receive")
    public Result<List<AdoptionRecordVO>> listMyReceive() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<AdoptionRecordVO> list = adoptionService.applyMyPet(userId);
        return Result.success(list);
    }

    /**
     * 取消申请
     * @param id
     * @return
     */
    @Operation(summary = "申请人取消申请")
    @PostMapping("/cancel/{id}")
    public Result<Boolean> cancel(@Parameter(description = "申请记录ID") @PathVariable Long id) {
        try {
            return Result.success(adoptionService.cancel(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
