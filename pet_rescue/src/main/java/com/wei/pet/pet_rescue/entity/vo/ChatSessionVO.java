package com.wei.pet.pet_rescue.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话列表专用轻量级对象
 */
@Schema(description = "会话列表展示")
@Data
public class ChatSessionVO {
    // 核心分组键
    @Schema(description = "宠物id")
    private Long petId;
    @Schema(description = "提问者id（如果是我发起的，这个就是我自己；如果是我收到的，这个是对方）")
    private Long askUserId; // 如果是我发起的，这个就是我自己；如果是我收到的，这个是对方
    @Schema(description = "回复人id")
    private Long replyUserId; // 发布人ID

    // 展示信息
    @Schema(description = "宠物名 或 对方昵称")
    private String title;   // 宠物名 或 对方昵称
    @Schema(description = "宠物头像 或 对方头像")
    private String avatar;  // 宠物头像 或 对方头像
    @Schema(description = "最新一条消息的内容")
    private String lastMessage; // 最新一条消息的内容 (用于展示摘要)
    @Schema(description ="最新交互时间" )
    private LocalDateTime lastTime; // 最新交互时间 (用于排序)

    // 状态标记
    @Schema(description = "未回复的条数")
    private Integer unreadCount;
}
