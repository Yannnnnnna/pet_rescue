package com.wei.pet.pet_rescue.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author wyr on 2026/1/21
 */
@Data
@Schema(description = "领养审核表单")
public class AdoptionAuditDTO {

    @Schema(description = "申请记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "申请ID不能为空")
    private Long id;

    @Schema(description = "操作人ID (模拟当前登录用户，必须是该宠物的发布者)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    @Schema(description = "是否通过: true-同意领养 false-婉拒", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "审核结果不能为空")
    private Boolean pass;

    @Schema(description = "备注/拒绝原因")
    private String remark;
}