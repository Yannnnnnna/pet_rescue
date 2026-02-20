package com.wei.pet.pet_rescue.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wei.pet.pet_rescue.common.BizType;
import com.wei.pet.pet_rescue.entity.CmsArticleLike;
import com.wei.pet.pet_rescue.entity.PostLike;
import com.wei.pet.pet_rescue.entity.vo.CheckResultVO;
import com.wei.pet.pet_rescue.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionServiceImpl {
    private final RedisTemplate<String, Object> redisTemplate;

    private final IPostLikeService postLikeService;
    private final StringRedisTemplate stringRedisTemplate;
    @Lazy
    @Autowired
    private ICmsArticleLikeService cmsArticleLikeService;
    @Lazy
    @Autowired
    private IPetInfoService petService;
    @Lazy
    @Autowired
    private ICmsArticleService articleService;

    /**
     * ==========================================
     * 第一部分：浏览量功能 (严格使用 stringRedisTemplate)
     * ==========================================
     */
    public void incrementView(String bizType, Long bizId) {
        String viewKey = "view:" + bizType;
        String hashKey = bizId.toString();
        String syncRecordKey = "sync:view_record:" + bizType;

        // 🌟 终极修复 1：使用 !Boolean.TRUE.equals()
        // 这样无论是返回 false 还是 null，都会被拦截，乖乖去数据库查数据
        Boolean hasKey = stringRedisTemplate.opsForHash().hasKey(viewKey, hashKey);

        if (!Boolean.TRUE.equals(hasKey)) {
            // 如果缓存未命中，从数据库查出真实基础浏览量
            Long dbViewCount = getDbViewCount(bizType, bizId);

            // 🌟 终极修复 2：使用 putIfAbsent 防并发
            // 万一同一毫秒内有两个人点进文章，可以防止他们把初始值覆盖两遍
            stringRedisTemplate.opsForHash().putIfAbsent(viewKey, hashKey, dbViewCount.toString());
        }

        // 安全进行 +1 操作 (存入的绝对是纯净的数字字符串)
        stringRedisTemplate.opsForHash().increment(viewKey, hashKey, 1);

        // 记录脏数据，交由定时任务落库
        stringRedisTemplate.opsForSet().add(syncRecordKey, hashKey);
    }

    public Long getViewCount(String bizType, Long bizId) {
        String viewKey = "view:" + bizType;
        Object countObj = stringRedisTemplate.opsForHash().get(viewKey, bizId.toString());

        if (countObj == null) {
            Long dbCount = getDbViewCount(bizType, bizId);
            // 🌟 修复2：缓存未命中兜底时，必须用 stringRedisTemplate
            stringRedisTemplate.opsForHash().put(viewKey, bizId.toString(), dbCount.toString());
            return dbCount;
        }
        return Long.valueOf(countObj.toString());
    }

    private Long getDbViewCount(String bizType, Long bizId) {
        if (BizType.PET.equals(bizType)) {
            var pet = petService.getById(bizId);
            return (pet != null && pet.getViewCount() != null) ? pet.getViewCount().longValue() : 0L;
        } else if (BizType.ARTICLE.equals(bizType)) {
            var article = articleService.getById(bizId);
            return (article != null && article.getViewCount() != null) ? article.getViewCount().longValue() : 0L;
        }
        return 0L;
    }


    /**
     * ==========================================
     * 第二部分：点赞功能 (保持使用 redisTemplate)
     * ==========================================
     */
    public CheckResultVO getLikeInfo(String bizType, Long bizId, Long currentUserId) {
        String likeKey = "like:" + bizType + ":" + bizId;
        CheckResultVO result = new CheckResultVO();

        if (Boolean.FALSE.equals(redisTemplate.hasKey(likeKey))) {
            List<Long> likedUserIds = getDbLikeUserIds(bizType, bizId);

            if (!likedUserIds.isEmpty()) {
                String[] userIdsArray = likedUserIds.stream().map(String::valueOf).toArray(String[]::new);
                redisTemplate.opsForSet().add(likeKey, userIdsArray);
                redisTemplate.expire(likeKey, 24, TimeUnit.HOURS);
            } else {
                redisTemplate.opsForSet().add(likeKey, "-1");
                redisTemplate.expire(likeKey, 24, TimeUnit.HOURS);
            }
        }

        Long totalLikes = redisTemplate.opsForSet().size(likeKey);

        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(likeKey, "-1"))) {
            totalLikes = (totalLikes != null && totalLikes > 0) ? totalLikes - 1 : 0L;
        }
        result.setCount(totalLikes != null ? totalLikes : 0L);

        if (currentUserId != null) {
            Boolean isLiked = redisTemplate.opsForSet().isMember(likeKey, currentUserId.toString());
            result.setChecked(isLiked);
        } else {
            result.setChecked(false);
        }

        return result;
    }

    public Long toggleLike(String bizType, Long bizId, Long userId) {
        String likeKey = "like:" + bizType + ":" + bizId;
        String userIdStr = userId.toString();

        String syncRecordKey = "sync:like_record:" + bizType;
        String hashField = bizId + "::" + userId;

        Boolean isMember = redisTemplate.opsForSet().isMember(likeKey, userIdStr);

        if (Boolean.TRUE.equals(isMember)) {
            redisTemplate.opsForSet().remove(likeKey, userIdStr);
            redisTemplate.opsForHash().put(syncRecordKey, hashField, 0);
        } else {
            redisTemplate.opsForSet().remove(likeKey, "-1");
            redisTemplate.opsForSet().add(likeKey, userIdStr);
            redisTemplate.opsForHash().put(syncRecordKey, hashField, 1);
        }

        Long totalLikes = redisTemplate.opsForSet().size(likeKey);
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(likeKey, "-1"))) {
            totalLikes = (totalLikes != null && totalLikes > 0) ? totalLikes - 1 : 0L;
        }
        return totalLikes;
    }

    private List<Long> getDbLikeUserIds(String bizType, Long bizId) {
        if (BizType.DIARY.equals(bizType)) {
            return postLikeService.list(new LambdaQueryWrapper<PostLike>()
                            .eq(PostLike::getPostId, bizId))
                    .stream().map(PostLike::getUserId).collect(Collectors.toList());
        } else if (BizType.ARTICLE.equals(bizType)) {
            return cmsArticleLikeService.list(new LambdaQueryWrapper<CmsArticleLike>()
                            .eq(CmsArticleLike::getArticleId, bizId))
                    .stream().map(CmsArticleLike::getUserId)
                    .collect(Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }

    /**
     * ==========================================
     * 第三部分：缓存清理
     * ==========================================
     */
    public void clearInteractionData(String bizType, Long bizId) {
        String bizIdStr = bizId.toString();

        String viewKey = "view:" + bizType;
        // 🌟 修复3：清理浏览量必须使用 stringRedisTemplate
        stringRedisTemplate.opsForHash().delete(viewKey, bizIdStr);

        String syncViewKey = "sync:view_record:" + bizType;
        // 🌟 修复4：清理浏览量同步队列必须使用 stringRedisTemplate
        stringRedisTemplate.opsForSet().remove(syncViewKey, bizIdStr);

        String likeKey = "like:" + bizType + ":" + bizId;
        redisTemplate.delete(likeKey);

        String syncLikeKey = "sync:like_record:" + bizType;
        try {
            Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(syncLikeKey,
                    ScanOptions.scanOptions().match(bizIdStr + "::*").build());

            while (cursor.hasNext()) {
                String fieldToDelete = cursor.next().getKey().toString();
                redisTemplate.opsForHash().delete(syncLikeKey, fieldToDelete);
            }
            cursor.close();
        } catch (Exception e) {
            log.error("清理待同步点赞队列失败, bizType:{}, bizId:{}", bizType, bizId, e);
        }
    }
}