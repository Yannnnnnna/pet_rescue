package com.wei.pet.pet_rescue.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author wyr on 2026/1/25
 */
@Data
public class ConsultationSummaryVO {
    // 咨询者(潜在领养人)信息
    private Long askUserId;
    private String askUserNickname;
    private String askUserAvatar;

    // 最后一条沟通概览
    private String lastMessage;
    private LocalDateTime lastTime;

    // 统计信息 (可选)
    private Integer msgCount; // 总共聊了多少句
}
