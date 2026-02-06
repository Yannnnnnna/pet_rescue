package com.wei.pet.pet_rescue.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.wei.pet.pet_rescue.entity.PetFavorite;
import com.wei.pet.pet_rescue.entity.PetInfo;
import com.wei.pet.pet_rescue.entity.dto.pet.PetFavoriteDTO;
import com.wei.pet.pet_rescue.mapper.PetFavoriteMapper;
import com.wei.pet.pet_rescue.service.IPetFavoriteService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.pet.pet_rescue.service.IPetInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 宠物收藏表 服务实现类
 * </p>
 *
 * @author yanna
 * @since 2026-01-27
 */
@Service
@RequiredArgsConstructor
public class PetFavoriteServiceImpl extends ServiceImpl<PetFavoriteMapper, PetFavorite> implements IPetFavoriteService {
    private final IPetInfoService petInfoService;
    @Override
    public boolean toggleFavorite(PetFavoriteDTO dto) {

        Long userId = StpUtil.getLoginIdAsLong();
        Long petId = dto.getPetId();

        // 1. 检查是否已经收藏
        PetFavorite exist = this.lambdaQuery()
                .eq(PetFavorite::getUserId, userId)
                .eq(PetFavorite::getPetId, petId)
                .one();

        if (exist != null) {
            // 2. 已存在 -> 执行取消收藏 (删除)
            this.removeById(exist.getId());
            return false; // 返回 false 表示当前未收藏
        } else {
            // 3. 不存在 -> 执行收藏 (新增)
            PetFavorite favorite = new PetFavorite();
            favorite.setUserId(userId);
            favorite.setPetId(petId);
            this.save(favorite);
            return true; // 返回 true 表示当前已收藏
        }
    }

    @Override
    public List<PetInfo> myFavoriteList() {
        long userId = StpUtil.getLoginIdAsLong();

        // 使用 MyBatis Plus 的 inSql 子查询，一行代码搞定关联查询
        // 逻辑：查询 PetInfo，条件是 ID 存在于收藏表中
        List<PetInfo> list = petInfoService.lambdaQuery()
                .inSql(PetInfo::getId, "SELECT pet_id FROM pet_favorite WHERE user_id = " + userId)
                .orderByDesc(PetInfo::getCreateTime)
                .list();

        return list;
    }
}
