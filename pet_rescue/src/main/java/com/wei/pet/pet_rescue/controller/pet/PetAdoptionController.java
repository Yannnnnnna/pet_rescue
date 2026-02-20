package com.wei.pet.pet_rescue.controller.pet;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wei.pet.pet_rescue.common.NoRepeatSubmit;
import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.PetAdoption;
import com.wei.pet.pet_rescue.entity.dto.adopt.AdoptionApplyDTO;
import com.wei.pet.pet_rescue.entity.dto.adopt.AdoptionAuditDTO;
import com.wei.pet.pet_rescue.entity.dto.adopt.SignRequestDTO;
import com.wei.pet.pet_rescue.entity.vo.AdminAdoptionRecordVO;
import com.wei.pet.pet_rescue.entity.vo.AdoptionDetailVO;
import com.wei.pet.pet_rescue.entity.vo.AdoptionRecordVO;
import com.wei.pet.pet_rescue.entity.vo.UserInfoVO;
import com.wei.pet.pet_rescue.service.IPetAdoptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate redisTemplate;
    private static final String  MY_ADOPTED_PETS_CACHE = "my:adopted:pets";

    @Operation(summary = "提交领养申请")
    @PostMapping("/apply")
    @NoRepeatSubmit(lockTime = 5) // 5秒内同一用户不能重复提交
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

    /**
     * 获取申请详细信息
     * @param id
     * @return
     */
    @Operation(summary = "获取申请详情 (用于审核查看)")
    @GetMapping("/detail/{id}")
    public Result<AdoptionDetailVO> getDetail(@Parameter(description = "申请ID") @PathVariable Long id) {
       return Result.success(adoptionService.getAdoptionDetail(id));

    }

    // 接口1：全平台领养记录分页查询
    @Operation(summary = "管理员-全平台领养记录分页")
    @GetMapping("/admin/list")
    public Result<IPage<AdminAdoptionRecordVO>> adminList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String petName) {

        Page<AdminAdoptionRecordVO> page = new Page<>(pageNum, pageSize);
        // 调用 Service -> Mapper 的逻辑
        return Result.success(adoptionService.getAdminPage(page, status, petName));
    }

    /**
     * 获取某只宠物的成功领养人信息（用于回访）
     * @param petId 宠物ID
     * @return 领养申请记录（包含联系人、电话、地址）
     */
    @Operation(summary = "获取某只宠物的成功领养人信息（用于回访）")
    @GetMapping("/adopter/{petId}")
    public Result<UserInfoVO> getAdopterInfo(@PathVariable Long petId) {

        UserInfoVO userInfoVO = adoptionService.getByPetId(petId);
        return Result.success(userInfoVO);
    }
    @PostMapping("/sign")
    @Operation(summary = "用户签署领养协议")
    public Result<Boolean> signAgreement(@RequestBody SignRequestDTO req) {
        redisTemplate.delete(MY_ADOPTED_PETS_CACHE);
        return Result.success(adoptionService.signAgreement(req));
    }



}
