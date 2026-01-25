package com.wei.pet.pet_rescue.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author wyr on 2026/1/24
 */
@Data
public class AdminChatRecordVO {
    @Schema(description = "宠物id")
    private Long id;
    @Schema(description = "发送者ID")
    private Long senderId;
    @Schema(description = "发送者昵称")
    private String senderName;
    @Schema(description = "发送者头像")
    private String senderAvatar;
    @Schema(description = "聊天内容")
    private String content;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "发送消息对象(1-申请人发的, 2-送养人(主人)发的)")
    private Integer direction;
}
