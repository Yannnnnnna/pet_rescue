package com.wei.pet.pet_rescue.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author wyr on 2026/1/21
 */
@Data
@Schema(description = "领养申请记录展示对象")
public class AdoptionRecordVO {

    @Schema(description = "申请单ID")
    private Long id;

    @Schema(description = "宠物ID")
    private Long petId;

    @Schema(description = "宠物昵称")
    private String petName;

    @Schema(description = "宠物封面图")
    private String petCover;

    @Schema(description = "申请人ID")
    private Long userId;

    @Schema(description = "申请人真实姓名")
    private String realName;

    @Schema(description = "申请人性别 (可选)")
    private Integer userSex; // 如果你需要展示申请人性别

    @Schema(description = "申请状态: 0-待审核 1-通过 2-驳回")
    private Integer status;

    @Schema(description = "申请时间")
    private LocalDateTime createTime;
}
