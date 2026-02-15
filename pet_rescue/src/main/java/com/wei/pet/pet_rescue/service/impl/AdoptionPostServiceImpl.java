package com.wei.pet.pet_rescue.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wei.pet.pet_rescue.entity.AdoptionPost;
import com.wei.pet.pet_rescue.entity.dto.adopt.AdoptionPostDTO;
import com.wei.pet.pet_rescue.entity.dto.adopt.AdoptionPostQueryDTO;
import com.wei.pet.pet_rescue.entity.dto.adopt.ReviewDTO;
import com.wei.pet.pet_rescue.mapper.AdoptionPostMapper;
import com.wei.pet.pet_rescue.service.IAdoptionPostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 领养生活记录贴 服务实现类
 * </p>
 *
 * @author yanna
 * @since 2026-02-09
 */
@Service
public class AdoptionPostServiceImpl extends ServiceImpl<AdoptionPostMapper, AdoptionPost> implements IAdoptionPostService {
    /**
     * 发布和修改领养生活记录贴
     * @param adoptionPostDTO
     * @return
     */
    @Override
    public boolean saveAdoptionPost(AdoptionPostDTO adoptionPostDTO) {
        AdoptionPost post = new AdoptionPost();
        BeanUtils.copyProperties(adoptionPostDTO, post);
        post.setUserId(StpUtil.getLoginIdAsLong());
        post.setCreateTime(LocalDateTime.now());
        post.setAuditStatus(0); // 默认待审核
        return this.save(post);
    }

    /**
     * 审核领养生活记录贴
     * @param status
     * @return
     */
    @Override
    public boolean reviewAdoptionPost(ReviewDTO status) {

        AdoptionPost post = this.getById(status.getPostId());
        if (post == null) {
            throw new RuntimeException("领养生活记录贴不存在");
        }
        if (post.getAuditStatus() != 0) {
            throw new RuntimeException("领养生活记录贴已审核");
        }
        post.setAuditStatus(status.getStatus());
        return this.updateById(post);
    }
    /**
     * 列出领养生活记录贴
     * @param queryDTO
     * @return
     */
    @Override
    public List<AdoptionPost> listAdoptionPosts(AdoptionPostQueryDTO queryDTO) {
        QueryWrapper<AdoptionPost> wrapper = new QueryWrapper<>();
        if (queryDTO.getUserId() != null) {
            wrapper.eq("user_id", queryDTO.getUserId());
        }
        if (queryDTO.getAuditStatus() != null) {
            wrapper.eq("audit_status", queryDTO.getAuditStatus());
        }
        if (queryDTO.getPetId() != null) {
            wrapper.eq("pet_id", queryDTO.getPetId());
        }
        return this.list(wrapper);
    }


}
