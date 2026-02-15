package com.wei.pet.pet_rescue.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 领养日记点赞记录表
 * </p>
 *
 * @author yanna
 * @since 2026-02-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("post_like")
@Schema(description="领养日记点赞记录表")
public class PostLike implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "帖子ID")
    private Long postId;

    @Schema(description = "点赞人ID")
    private Long userId;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "点赞时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


}
