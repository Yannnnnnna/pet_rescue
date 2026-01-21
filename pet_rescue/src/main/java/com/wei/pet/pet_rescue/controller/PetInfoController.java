package com.wei.pet.pet_rescue.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.PetInfo;
import com.wei.pet.pet_rescue.entity.dto.PetDTO;
import com.wei.pet.pet_rescue.entity.dto.PetQueryDTO;
import com.wei.pet.pet_rescue.service.IPetInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 宠物档案表 前端控制器
 * </p>
 *
 * @author yanna
 * @since 2026-01-10
 */
@Tag(name = "宠物信息管理")
@RestController
@RequestMapping("/pet-info")
public class PetInfoController {
    @Resource
    private IPetInfoService petInfoService;

    @Operation(summary = "新增宠物")
    @PostMapping("/add")
    public Result<Boolean> addPet(@RequestBody @Validated PetDTO petForm) {
        boolean success = petInfoService.savePet(petForm);
        return success ? Result.success(true) : Result.error("添加失败");
    }

    @Operation(summary = "修改宠物信息")
    @PostMapping("/update")
    public Result<Boolean> updatePet(@RequestBody @Validated PetDTO petForm) {
        if (petForm.getId() == null) {
            return Result.error("ID不能为空");
        }
        boolean success = petInfoService.updatePet(petForm);
        return success ? Result.success(true) : Result.error("修改失败");
    }

    @Operation(summary = "删除宠物", description = "逻辑删除")
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deletePet(@Parameter(description = "宠物ID") @PathVariable Long id) {
        boolean success = petInfoService.removeById(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @Operation(summary = "获取宠物详情", description = "用于编辑回显或前端详情页展示")
    @GetMapping("/{id}")
    public Result<PetDTO> getDetail(@Parameter(description = "宠物ID") @PathVariable Long id) {
        PetDTO detail = petInfoService.getPetDetail(id);
        return Result.success(detail);
    }

    @Operation(summary = "分页查询宠物列表", description = "支持按状态、类型、关键字筛选")
    @PostMapping("/list")
    public Result<IPage<PetInfo>> getList(@RequestBody PetQueryDTO query) {
        // todo 修改管理员和用户能够访问的宠物状态
        // 普通用户接口建议强制 status=0 (待领养)，管理员接口可以查所有
        // 这里为了演示方便，直接透传查询
        IPage<PetInfo> page = petInfoService.getPetPage(query);
        return Result.success(page);
    }

}
