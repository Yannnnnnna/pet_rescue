package com.wei.pet.pet_rescue.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wei.pet.pet_rescue.entity.AdoptionPost;
import com.wei.pet.pet_rescue.entity.CmsArticle;
import com.wei.pet.pet_rescue.entity.CmsArticleLike;
import com.wei.pet.pet_rescue.entity.PostLike;
import com.wei.pet.pet_rescue.mapper.PostLikeMapper;
import com.wei.pet.pet_rescue.service.IAdoptionPostService;
import com.wei.pet.pet_rescue.service.IPostLikeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 领养日记点赞记录表 服务实现类
 * </p>
 *
 * @author yanna
 * @since 2026-02-14
 */
@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl extends ServiceImpl<PostLikeMapper, PostLike> implements IPostLikeService {

    private final IAdoptionPostService postService;
    /**
     * 切换点赞状态
     *
     * @param postId
     * @return 当前点赞数量
     */
    @Override
    public Integer toggleLike(Long postId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 1. 检查是否已经点赞
        PostLike exist = this.lambdaQuery()
                .eq(PostLike::getUserId, userId)
                .eq(PostLike::getPostId, postId)
                .one();

        AdoptionPost post = postService.getById(postId);
        if (post == null){
            throw new RuntimeException("文章不存在！");
        }

        if (exist != null) {
            // 2. 已存在 -> 执行取消点赞 (删除)
            this.removeById(exist.getId());
            postService.update(null, new LambdaUpdateWrapper<AdoptionPost>()
                    .setSql("like_count = like_count - 1")
                    .eq(AdoptionPost::getId, postId));

            return Math.max(0, post.getLikeCount() - 1);
        } else {
            // 3. 不存在 -> 执行点赞 (新增)
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            this.save(like);
            // B. 文章数 +1
            postService.update(null, new LambdaUpdateWrapper<AdoptionPost>()
                    .setSql("like_count = like_count + 1") // 使用 SQL 更新防止并发覆盖
                    .eq(AdoptionPost::getId, postId));

            // 返回前端 +1 后的结果（用于校准）
            return post.getLikeCount() + 1;
        }

    }
}
