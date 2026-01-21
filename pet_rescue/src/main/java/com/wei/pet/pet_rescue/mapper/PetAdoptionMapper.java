package com.wei.pet.pet_rescue.mapper;

import com.wei.pet.pet_rescue.entity.PetAdoption;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wei.pet.pet_rescue.entity.vo.AdoptionRecordVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 领养申请表 Mapper 接口
 * </p>
 *
 * @author yanna
 * @since 2026-01-10
 */
public interface PetAdoptionMapper extends BaseMapper<PetAdoption> {
    /**
     * 查询指定送养人(publisherId)收到的所有领养申请
     * 并关联查询出宠物的基础信息
     */
    @Select("SELECT " +
            "  a.id, " +
            "  a.pet_id, " +
            "  p.name AS petName, " +
            "  p.cover_img AS petCover, " +
            "  a.user_id, " +
            "  a.real_name, " +
            "  a.status, " +
            "  a.create_time " +
            "FROM pet_adoption a " +
            "LEFT JOIN pet_info p ON a.pet_id = p.id " +
            "WHERE p.publisher_id = #{publisherId} " +
            "ORDER BY a.create_time DESC")
    List<AdoptionRecordVO> selectReceivedApplications(Long userId);
}
