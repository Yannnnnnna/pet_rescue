package com.wei.pet.pet_rescue.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wei.pet.pet_rescue.entity.CmsArticle;
import com.wei.pet.pet_rescue.entity.CmsArticleFavorite;
import com.wei.pet.pet_rescue.entity.dto.article.CmsArticleFavoriteDTO;
import com.wei.pet.pet_rescue.mapper.CmsArticleFavoriteMapper;
import com.wei.pet.pet_rescue.service.ICmsArticleFavoriteService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.pet.pet_rescue.service.ICmsArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 文章收藏表 服务实现类
 * </p>
 *
 * @author yanna
 * @since 2026-02-03
 */
@Service
@RequiredArgsConstructor
public class CmsArticleFavoriteServiceImpl extends ServiceImpl<CmsArticleFavoriteMapper, CmsArticleFavorite> implements ICmsArticleFavoriteService {
    private final ICmsArticleService articleService;

    /**
     * 收藏、取消收藏
     * @param dto
     * @return
     */
    @Override
    public boolean toggleFavorite(CmsArticleFavoriteDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long id = dto.getArticleId();

        // 1. 检查是否已经收藏
        CmsArticleFavorite exist = this.lambdaQuery()
                .eq(CmsArticleFavorite::getUserId, userId)
                .eq(CmsArticleFavorite::getArticleId, id)
                .one();
        if (exist != null) {
            // 2. 已存在 -> 执行取消收藏 (删除)
            this.removeById(exist.getId());
            return false; // 返回 false 表示当前未收藏
        } else {
            // 3. 不存在 -> 执行收藏 (新增)
            CmsArticleFavorite favorite = new CmsArticleFavorite();
            favorite.setArticleId(id);
            favorite.setUserId(userId);
            this.save(favorite);
            return true; // 返回 true 表示当前已收藏
        }
    }

    /**
     * 查询收藏列表
     * @return
     */
    @Override
    public List<CmsArticle> myFavoriteList(Integer type) {
        long userId = StpUtil.getLoginIdAsLong();

        // 1. 构造 Wrapper
        LambdaQueryWrapper<CmsArticle> wrapper = new LambdaQueryWrapper<>();


        // 3. 动态类型 (如果前端不传 type，就查所有类型的文章收藏，比如把百科和资讯混在一起)
        wrapper.eq(type != null, CmsArticle::getType, type);

        // 4. 子查询 (关联收藏表)
        // 这里的 SQL 拼装虽然简单，但在 MyBatis-Plus 中是安全的，因为 userId 是 long 类型
        wrapper.inSql(CmsArticle::getId,
                "SELECT article_id FROM cms_article_favorite WHERE user_id = " + userId);

        // 5. 排序 (目前是按文章发布时间，如果想按收藏时间需要手写 XML SQL)
        wrapper.orderByDesc(CmsArticle::getCreateTime);

        return articleService.list(wrapper);
    }
}
