package com.wei.pet.pet_rescue.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author wyr on 2026/1/21
 */
@Data
@TableName("ai_chat_record")
@Schema(description = "AI对话记录")
public class AiChatRecord {

    @TableId
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "角色: 0-用户 1-AI")
    private Integer role;

    @Schema(description = "内容")
    private String content;

    private LocalDateTime createTime;

    @TableLogic
    private Integer isDeleted;
}
