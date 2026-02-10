package com.wei.pet.pet_rescue.controller.pet;


import cn.dev33.satoken.annotation.SaCheckRole;
import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.AdoptionPost;
import com.wei.pet.pet_rescue.entity.dto.adopt.AdoptionPostDTO;
import com.wei.pet.pet_rescue.entity.dto.adopt.AdoptionPostQueryDTO;
import com.wei.pet.pet_rescue.entity.dto.adopt.ReviewDTO;
import com.wei.pet.pet_rescue.service.IAdoptionPostService;
import com.wei.pet.pet_rescue.service.IPetFeedRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 领养生活记录贴 前端控制器
 * </p>
 *
 * @author yanna
 * @since 2026-02-09
 */
@RestController
@Tag(name = "领养生活记录贴管理")
@RequestMapping("/adoption-post")
@RequiredArgsConstructor
public class AdoptionPostController {
    private final IAdoptionPostService adoptionPostService;
    @Operation(summary = "新增领养生活记录贴")
    @PostMapping("/add")
    public Result<Boolean> addAdoptionPost(@RequestBody AdoptionPostDTO adoptionPostDTO) {
        boolean success = adoptionPostService.saveAdoptionPost(adoptionPostDTO);
        return success ? Result.success(true) : Result.error("添加失败");
    }

    @Operation(summary = "审核领养生活记录贴")
    @SaCheckRole("admin")
    @PostMapping("/review")
    public Result<Boolean> reviewAdoptionPost(@RequestBody ReviewDTO status) {
        // 这里可以根据status进行审核逻辑处理，例如通过、驳回等
        boolean success = adoptionPostService.reviewAdoptionPost(status);
        return success ? Result.success(true) : Result.error("审核失败");
    }
    @Operation(summary = "获取领养生活记录贴")
    @GetMapping("/{postId}")
    public Result<?> getAdoptionPost(@PathVariable Long postId) {
        return Result.success(adoptionPostService.getById(postId));
    }
    @Operation(summary = "删除领养生活记录贴")
    @DeleteMapping("/{postId}")
    public Result<Boolean> deleteAdoptionPost(@PathVariable Long postId) {
        boolean success = adoptionPostService.removeById(postId);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @Operation(summary = "列表展示领养生活记录贴")
    @GetMapping("/list")
    public Result<List<AdoptionPost>> listAdoptionPosts(@RequestBody AdoptionPostQueryDTO queryDTO) {
        return Result.success(adoptionPostService.listAdoptionPosts(queryDTO));
    }
}
