package com.wei.pet.pet_rescue.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wei.pet.pet_rescue.entity.CmsArticle;
import com.wei.pet.pet_rescue.entity.CmsArticleLike;
import com.wei.pet.pet_rescue.entity.PetInfo;
import com.wei.pet.pet_rescue.entity.PostLike;
import com.wei.pet.pet_rescue.service.ICmsArticleLikeService;
import com.wei.pet.pet_rescue.service.ICmsArticleService;
import com.wei.pet.pet_rescue.service.IPetInfoService;
import com.wei.pet.pet_rescue.service.IPostLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * 点赞更新定时任务
 * @author wyr on 2026/2/20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LikeSyncTask {

    private final RedisTemplate<String, Object> redisTemplate;

    // 用于真正操作 MySQL
     private final IPostLikeService postLikeService;
     private final ICmsArticleLikeService cmsArticleLikeService;
     private final IPetInfoService petInfoService;
     private final ICmsArticleService cmsArticleService;
    private final StringRedisTemplate stringRedisTemplate;


    /**
     * 定时同步点赞状态到 MySQL
     * 答辩演示建议用 "0/30 * * * * ?" (每30秒执行一次)
     * 实际上线建议用 "0 0/5 * * * ?" (每5分钟执行一次)
     */
    /**
     * 同步文章点赞
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void syncArticleLikeRecords() {
        log.info("⏳ 开启文章异步点赞落库任务...");

        // 这里的 bizType 以 diary 为例，如果是文章就再写一个 sync:like_record:article 的处理逻辑
        String syncRecordKey_article = "sync:like_record:article";

        // 获取小本本里所有的待办事项
        Map<Object, Object> article_entries =  redisTemplate.opsForHash().entries(syncRecordKey_article);

        // 处理文章点赞的逻辑，和上面日记类似
        if (!article_entries.isEmpty()){
            // 遍历待办事项，批量操作数据库
            for (Map.Entry<Object, Object> entry : article_entries.entrySet()) {
                String field = entry.getKey().toString(); // 例如 "101::5"
                Integer status = Integer.valueOf(entry.getValue().toString());

                String[] parts = field.split("::");
                Long bizId = Long.valueOf(parts[0]);
                Long userId = Long.valueOf(parts[1]);

                try {
                    if (status == 1) {
                        // 执行 INSERT INTO post_like (post_id, user_id) VALUES (...)
                        cmsArticleLikeService.save(new CmsArticleLike()
                                .setArticleId(bizId)
                                .setUserId(userId));
                    } else if (status == 0) {
                        // 执行 DELETE FROM post_like WHERE post_id = ? AND user_id = ?
                        cmsArticleLikeService.remove(new LambdaQueryWrapper<CmsArticleLike>()
                                .eq(CmsArticleLike::getArticleId, bizId)
                                .eq(CmsArticleLike::getUserId, userId));
                    }

                    // 🔥 关键：数据库同步成功后，把这条记录从小本本上划掉
                    redisTemplate.opsForHash().delete(syncRecordKey_article, field);

                } catch (Exception e) {
                    // 如果插入报错（比如主键冲突 uk_post_user），说明已经有了，可以忽略，同样把任务划掉
                    log.error("同步点赞异常, bizId:{}, userId:{}", bizId, userId, e);
                    redisTemplate.opsForHash().delete(syncRecordKey_article, field);
                }
            }
            log.info("✅ 异步点赞落库完成！处理了 {} 条记录", article_entries.size());
        } else {
            log.info("没有待同步的文章点赞记录");
        }

    }

    /**
     * 同步日记点赞
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void syncPostLikeRecords(){
        log.info("⏳ 开启养宠日记异步点赞落库任务...");

        // 这里的 bizType 以 diary 为例，如果是文章就再写一个 sync:like_record:article 的处理逻辑
        String syncRecordKey_diary = "sync:like_record:diary";

        // 获取小本本里所有的待办事项
        Map<Object, Object> diary_entries = redisTemplate.opsForHash().entries(syncRecordKey_diary);

        if (!diary_entries.isEmpty()) {
            log.info("待同步的日记点赞记录数量: {}", diary_entries.size());
            // 遍历待办事项，批量操作数据库
            for (Map.Entry<Object, Object> entry : diary_entries.entrySet()) {
                String field = entry.getKey().toString();

                // 🌟 修正 1：更安全的类型转换方式
                Integer status = Integer.valueOf(entry.getValue().toString());

                String[] parts = field.split("::");
                Long bizId = Long.valueOf(parts[0]);
                Long userId = Long.valueOf(parts[1]);

                try {
                    if (status == 1) {
                        postLikeService.save(new PostLike()
                                .setPostId(bizId)
                                .setUserId(userId)
                                .setCreateTime(LocalDateTime.now()));
                    } else if (status == 0) {
                        postLikeService.remove(new LambdaQueryWrapper<PostLike>()
                                .eq(PostLike::getPostId, bizId)
                                .eq(PostLike::getUserId, userId));
                    }
                    // 数据库同步成功后，把这条记录从小本本上划掉
                    redisTemplate.opsForHash().delete(syncRecordKey_diary, field);

                } catch (Exception e) {
                    log.error("同步点赞异常, bizId:{}, userId:{}", bizId, userId, e);
                    redisTemplate.opsForHash().delete(syncRecordKey_diary, field);
                }
            }
            // 🌟 修正 2：将完成日志移到 for 循环外部
            log.info("✅ 异步日记点赞落库完成！处理了 {} 条记录", diary_entries.size());
        } else {
            log.info("没有待同步的日记点赞记录");
        }
    }

    /**
     * 同步宠物浏览量
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void syncPetViewRecords(){
        log.info("⏳ 开启宠物浏览量异步落库任务...");
        String syncViewKeyPet = "sync:view_record:pet";

        // 🌟 替换为 stringRedisTemplate，并且接收 Set<String>
        Set<String> dirtyPetIds = stringRedisTemplate.opsForSet().members(syncViewKeyPet);

        if (dirtyPetIds != null && !dirtyPetIds.isEmpty()) {
            for (String bizIdStr : dirtyPetIds) { // 🌟 直接是 String，不需要转换了
                try {
                    // 🌟 替换为 stringRedisTemplate
                    Object latestViewCount = stringRedisTemplate.opsForHash().get("view:pet", bizIdStr);

                    if (latestViewCount != null) {
                        petInfoService.update(new LambdaUpdateWrapper<PetInfo>()
                                .set(PetInfo::getViewCount, Integer.valueOf(latestViewCount.toString()))
                                .eq(PetInfo::getId, Long.valueOf(bizIdStr)));
                    }
                    // 🌟 替换为 stringRedisTemplate
                    stringRedisTemplate.opsForSet().remove(syncViewKeyPet, bizIdStr);
                } catch (Exception e) {
                    log.error("同步宠物浏览量异常, petId: {}", bizIdStr, e);
                }
            }
            log.info("✅ 宠物浏览量异步落库完成，处理了 {} 条数据", dirtyPetIds.size());
        }
    }

    /**
     * 同步文章浏览量
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void syncArticleViewRecords(){
        log.info("⏳ 开启文章浏览量异步落库任务...");
        String syncViewKeyArticle = "sync:view_record:article";

        // 🌟 替换为 stringRedisTemplate
        Set<String> dirtyArticleIds = stringRedisTemplate.opsForSet().members(syncViewKeyArticle);

        if (dirtyArticleIds != null && !dirtyArticleIds.isEmpty()) {
            for (String bizIdStr : dirtyArticleIds) {
                try {
                    // 🌟 替换为 stringRedisTemplate
                    Object latestViewCount = stringRedisTemplate.opsForHash().get("view:article", bizIdStr);

                    if (latestViewCount != null) {
                        cmsArticleService.update(new LambdaUpdateWrapper<CmsArticle>()
                                .set(CmsArticle::getViewCount, Integer.valueOf(latestViewCount.toString()))
                                .eq(CmsArticle::getId, Long.valueOf(bizIdStr)));
                    }
                    // 🌟 替换为 stringRedisTemplate
                    stringRedisTemplate.opsForSet().remove(syncViewKeyArticle, bizIdStr);
                } catch (Exception e) {
                    log.error("同步文章浏览量异常, articleId: {}", bizIdStr, e);
                }
            }
            log.info("✅ 文章浏览量异步落库完成，处理了 {} 条数据", dirtyArticleIds.size());
        }
    }
}
