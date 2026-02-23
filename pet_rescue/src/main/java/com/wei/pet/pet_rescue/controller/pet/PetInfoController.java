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
        // 1. 判断当前请求是否是“首页默认请求”
        // (例如：第一页，且没有传任何搜索关键字和类型)
        boolean isDefaultPage = (query.getPageNum() == 1) &&
                (query.getPageSize() == 10) && // 默认分页大小
                (query.getKeyword() == null || query.getKeyword().isEmpty()) &&
                (query.getType() == null) &&
                query.getStatus() != null && query.getStatus() == 0 &&
                query.getCity() == null;

        String defaultCacheKey = "pet:list:default_page_1";

        // 2. 如果是默认首页，优先走 Redis 缓存
        if (isDefaultPage) {
            IPage<PetInfo> cachePage = (IPage<PetInfo>) redisTemplate.opsForValue().get(defaultCacheKey);
            if (cachePage != null) {
                log.info("🚀 首页默认列表：走了 Redis 缓存，速度飞快！");
                return Result.success(cachePage);
            }
        }

        // 3. 如果不是首页（带了搜索条件），或者缓存失效了，走数据库查询
        log.info("🚀 复杂搜索或缓存未命中：走数据库查询...");
        IPage<PetInfo> page = petInfoService.getPetPage(query);

        // 4. 查询完之后，如果是首页请求，顺手把它塞进 Redis 缓存起来
        if (isDefaultPage) {
            // 设置较短的过期时间（例如 5-10 分钟），保证数据的相对实时性
            redisTemplate.opsForValue().set(defaultCacheKey, page, 10, TimeUnit.MINUTES);
        }

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
