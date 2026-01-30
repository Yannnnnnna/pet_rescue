package com.wei.pet.pet_rescue.mapper;

import com.wei.pet.pet_rescue.entity.PetConsultation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wei.pet.pet_rescue.entity.vo.ChatSessionVO;
import com.wei.pet.pet_rescue.entity.vo.ConsultationSummaryVO;
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
    // 1. 查询我收到的会话列表 (作为发布者)
    // 逻辑：按 宠物+提问者 分组，取最新的时间
    @Select("SELECT c.pet_id, c.ask_user_id, " +
            "MAX(c.create_time) as lastTime, " +
            "p.name as title, p.cover_img as avatar, " +
            "COUNT(CASE WHEN c.status = 0 THEN 1 END) as unreadCount " + // 统计待回复数量
            "FROM pet_consultation c " +
            "LEFT JOIN pet_info p ON c.pet_id = p.id " +
            "WHERE c.reply_user_id = #{userId} " +
            "GROUP BY c.pet_id, c.ask_user_id " +
            "ORDER BY lastTime DESC")
    List<ChatSessionVO> selectMyReceivedSessions(Long userId);

    // 2. 查询我发起的会话列表 (作为领养人)
    // 逻辑：按 宠物 分组 (通常一个用户对一只宠只会有一个会话)
    @Select("SELECT c.pet_id, c.reply_user_id, " +
            "MAX(c.create_time) as lastTime, " +
            "p.name as title, p.cover_img as avatar, " +
            "u.nickname as publisherName " + // 这里稍微特殊，可能想显示送养人名字，也可以直接显示宠物名
            "FROM pet_consultation c " +
            "LEFT JOIN pet_info p ON c.pet_id = p.id " +
            "LEFT JOIN sys_user u ON c.reply_user_id = u.id " +
            "WHERE c.ask_user_id = #{userId} " +
            "GROUP BY c.pet_id, c.reply_user_id " +
            "ORDER BY lastTime DESC")
    List<ChatSessionVO> selectMyAskedSessions(Long userId);

    List<ConsultationSummaryVO> selectConsultationSummary(Long petId);
}
