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
 * 人宠匹配度记录
 * </p>
 *
 * @author yanna
 * @since 2026-02-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("pet_match_record")
@Schema(description="人宠匹配度记录")
public class PetMatchRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "宠物ID")
    private Long petId;
    @Schema(description = "申请人/领养人ID")
    private Long userId;

    @Schema(description  = "匹配分数(0-100)")
    private Integer matchScore;
    @Schema(description = "AI分析理由")
    private String analysisResult;
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


}
