package com.wei.pet.pet_rescue.mapper;

import com.wei.pet.pet_rescue.entity.PetConsultation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wei.pet.pet_rescue.entity.vo.ConsultationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 领养咨询表 Mapper 接口
 * </p>
 *
 * @author yanna
 * @since 2026-01-21
 */
@Mapper
public interface PetConsultationMapper extends BaseMapper<PetConsultation> {
    // 查询我收到的咨询 (关联宠物表和用户表)
    @Select("SELECT c.*, p.name as petName, p.cover_img as petCover, " +
            "u.nickname as askUserNickname, u.avatar as askUserAvatar " +
            "FROM pet_consultation c " +
            "LEFT JOIN pet_info p ON c.pet_id = p.id " +
            "LEFT JOIN sys_user u ON c.ask_user_id = u.id " +
            "WHERE c.reply_user_id = #{userId} " +
            "ORDER BY c.status ASC, c.create_time DESC") // 未回复的排前面
    List<ConsultationVO> selectReceived(Long userId);

    // 查询我发出的咨询
    @Select("SELECT c.*, p.name as petName, p.cover_img as petCover " +
            "FROM pet_consultation c " +
            "LEFT JOIN pet_info p ON c.pet_id = p.id " +
            "WHERE c.ask_user_id = #{userId} " +
            "ORDER BY c.create_time DESC")
    List<ConsultationVO> selectAsked(Long userId);
}
