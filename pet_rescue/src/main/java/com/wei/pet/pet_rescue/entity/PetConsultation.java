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
@TableName("pet_consultation")
@Schema(description = "领养咨询实体")
public class PetConsultation {

    @TableId
    private Long id;

    @Schema(description = "宠物ID")
    private Long petId;

    @Schema(description = "提问者ID")
    private Long askUserId;

    @Schema(description = "回答者ID (发布人)")
    private Long replyUserId;

    @Schema(description = "问题内容")
    private String question;

    @Schema(description = "回复内容")
    private String answer;

    @Schema(description = "状态: 0-待回复 1-已回复")
    private Integer status;

    @Schema(description = "提问时间")
    private LocalDateTime createTime;

    @Schema(description = "回复时间")
    private LocalDateTime replyTime;

    @TableLogic
    private Integer isDeleted;
}
