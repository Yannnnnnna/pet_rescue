package com.wei.pet.pet_rescue.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 宠物投喂记录表
 * </p>
 *
 * @author yanna
 * @since 2026-02-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("pet_feed_record")
@Schema(description="宠物投喂记录表")
public class PetFeedRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    @Schema(description = "投喂人ID")
    private Long userId;
    @Schema(description = "宠物ID")
    private Long petId;
    @Schema(description = "投喂数量")
    private Integer amount;

    @Schema(description = "投喂时间")
    private LocalDateTime createTime;
}
