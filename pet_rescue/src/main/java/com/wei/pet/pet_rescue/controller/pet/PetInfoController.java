package com.wei.pet.pet_rescue.controller.pet;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hankcs.hanlp.classification.features.IFeatureWeighter;
import com.wei.pet.pet_rescue.common.BizType;
import com.wei.pet.pet_rescue.common.NoRepeatSubmit;
import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.PetInfo;
import com.wei.pet.pet_rescue.entity.dto.adopt.AdoptPetsDTO;
import com.wei.pet.pet_rescue.entity.dto.pet.PetDTO;
import com.wei.pet.pet_rescue.entity.dto.pet.PetFeedDTO;
import com.wei.pet.pet_rescue.entity.dto.pet.PetQueryDTO;
import com.wei.pet.pet_rescue.service.IPetInfoService;
import com.wei.pet.pet_rescue.service.impl.InteractionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
@RequiredArgsConstructor
@Slf4j
public class PetInfoController {
    private final IPetInfoService petInfoService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final InteractionServiceImpl interactionService;
    // 定义缓存的 Key
    private static final String PET_INFO_CACHE_KEY = "pet:info:list";


    @Operation(summary = "新增宠物")
    @PostMapping("/add")
    @NoRepeatSubmit(lockTime = 5)
    public Result<Boolean> addPet(@RequestBody  PetDTO petForm) {
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
        interactionService.clearInteractionData(BizType.PET, id); // 删除宠物时，清除相关的浏览量和点赞数据
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
        IPage<PetInfo> cachePage = (IPage<PetInfo>) redisTemplate.opsForValue().get(PET_INFO_CACHE_KEY);
        if (cachePage != null) {
            System.out.println("🚀 走了 Redis 缓存，速度飞快！");
            return Result.success(cachePage);
        }
        System.out.println("🚀 走数据库查询，请稍等...");
        IPage<PetInfo> page = petInfoService.getPetPage(query);
        // 将查询结果缓存到 Redis，设置过期时间为 30 分钟
        redisTemplate.opsForValue().set(PET_INFO_CACHE_KEY, page, 30, TimeUnit.MINUTES);
        return Result.success(page);
    }
    @Operation(summary = "查询我发布的宠物")
    @PostMapping("/myPets")
    public Result<List<PetInfo>> getMyPets(){
        Long id = StpUtil.getLoginIdAsLong();
        List<PetInfo> pets = petInfoService.getMyPets(id);
        return Result.success(pets);
    }

    @Operation(summary ="查询我沟通过的宠物")
    @GetMapping("/myChattedPets")
    public Result<List<PetInfo>> getMyChattedPets() {
        Long id = StpUtil.getLoginIdAsLong();
        List<PetInfo> pets = petInfoService.getMyChattedPets(id);
        return Result.success(pets);
    }
    private static final String  MY_ADOPTED_PETS_CACHE = "my:adopted:pets";
    @Operation(summary = "我领养的宠物")
    @GetMapping("/my-adopted-pets")
    public Result<List<AdoptPetsDTO>> getAdoptPets(){
        System.out.println("🚀 获取我领养的宠物");
        List<AdoptPetsDTO> cachePets = (List<AdoptPetsDTO>) redisTemplate.opsForValue().get(MY_ADOPTED_PETS_CACHE);
        if (cachePets != null) {
            System.out.println("🚀 走了 Redis 缓存，速度飞快！");
            return Result.success(cachePets);
        }
        System.out.println("🐢 走数据库查询，请稍等...");
        Long id = StpUtil.getLoginIdAsLong();
        List<AdoptPetsDTO> pets = petInfoService.getAdoptedPets(id);
        System.out.println("🚀 缓存我领养的宠物");
        redisTemplate.opsForValue().set(MY_ADOPTED_PETS_CACHE, pets, 30, TimeUnit.MINUTES);
        return Result.success(pets);
    }

}
