package com.wei.pet.pet_rescue.controller.pet;


import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.wei.pet.pet_rescue.common.BizType;
import com.wei.pet.pet_rescue.common.NoRepeatSubmit;
import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.AdoptionPost;
import com.wei.pet.pet_rescue.entity.dto.adopt.AdoptionPostDTO;
import com.wei.pet.pet_rescue.entity.dto.adopt.AdoptionPostQueryDTO;
import com.wei.pet.pet_rescue.entity.dto.adopt.ReviewDTO;
import com.wei.pet.pet_rescue.service.IAdoptionPostService;
import com.wei.pet.pet_rescue.service.IPetFeedRecordService;
import com.wei.pet.pet_rescue.service.impl.InteractionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate<String, Object> redisTemplate;
    private final InteractionServiceImpl interactionService;


    @Operation(summary = "新增领养生活记录贴")
    @PostMapping("/add")
    @NoRepeatSubmit(lockTime = 5)
    public Result<Boolean> addAdoptionPost(@RequestBody AdoptionPostDTO adoptionPostDTO) {
        boolean success = adoptionPostService.saveAdoptionPost(adoptionPostDTO);
        redisTemplate.delete(ADOPTION_POST_KEY);
        return success ? Result.success(true) : Result.error("添加失败");
    }

    @Operation(summary = "审核领养生活记录贴")
    @SaCheckRole("admin")
    @PostMapping("/review")
    public Result<Boolean> reviewAdoptionPost(@RequestBody ReviewDTO status) {
        // 这里可以根据status进行审核逻辑处理，例如通过、驳回等
        boolean success = adoptionPostService.reviewAdoptionPost(status);
        redisTemplate.delete(ADOPTION_POST_KEY);
        return success ? Result.success(true) : Result.error("审核失败");
    }
    @Operation(summary = "获取领养生活记录贴")
    @GetMapping("/{postId}")
    public Result<AdoptionPost> getAdoptionPost(@PathVariable Long postId) {
        // 1. 从数据库查出基础信息
        AdoptionPost adoptionPost = adoptionPostService.getById(postId);
        if (adoptionPost == null) {
            return Result.error("帖子不存在");
        }

        // 2. 安全获取当前用户ID (游客未登录则为 null)
        Long userId = null;
        if (StpUtil.isLogin()) {
            userId = StpUtil.getLoginIdAsLong();
        }

        // 3. 从 Redis 获取点赞信息
        Long redisLikeCount = interactionService.getLikeInfo(BizType.DIARY, postId, userId).getCount();

        // 4. 解决 Long 和 int 的转换！直接 .intValue()
        adoptionPost.setLikeCount(redisLikeCount.intValue());

        // 5. 🔥 返回我们刚才修改过的 adoptionPost 对象！
        return Result.success(adoptionPost);
    }
    @Operation(summary = "删除领养生活记录贴")
    @DeleteMapping("/{postId}")
    public Result<Boolean> deleteAdoptionPost(@PathVariable Long postId) {
        boolean success = adoptionPostService.removeById(postId);
        redisTemplate.delete(ADOPTION_POST_KEY);
        return success ? Result.success(true) : Result.error("删除失败");
    }
    private static final String ADOPTION_POST_KEY = "adoption_post:list";
    @Operation(summary = "列表展示领养生活记录贴")
    @GetMapping("/list")
    public Result<List<AdoptionPost>> listAdoptionPosts( AdoptionPostQueryDTO queryDTO) {
        List<AdoptionPost> cachedPosts = (List<AdoptionPost>) redisTemplate.opsForValue().get(ADOPTION_POST_KEY);
        if (cachedPosts != null) {
            System.out.println("🚀 走 Redis 缓存");
            return Result.success(cachedPosts);
        }
        System.out.println("🐢 走数据库查询");
        List<AdoptionPost> posts = adoptionPostService.listAdoptionPosts(queryDTO);
        System.out.println("🚀 缓存领养生活记录贴");
        redisTemplate.opsForValue().set(ADOPTION_POST_KEY, posts);
        return Result.success(posts);
    }
}
