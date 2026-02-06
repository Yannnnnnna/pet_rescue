package com.wei.pet.pet_rescue.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AI消息记录表
 * </p>
 *
 * @author yanna
 * @since 2026-02-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_ai_message")
@Schema( description="AI消息记录表")
public class SysAiMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "会话ID")
    private Long sessionId;
    @Schema(description = "角色: user/assistant/system")
    private String role;

    @Schema(description = "消息内容")
    private String content;
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


}
