package com.wei.pet.pet_rescue.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author wyr on 2026/1/24
 */
@Data
@Schema(description = "后台管理-领养申请列表VO")
public class AdminAdoptionRecordVO {

    @Schema(description = "申请单ID")
    private Long id;

    // --- 宠物信息 ---
    @Schema(description = "宠物ID")
    private Long petId;
    @Schema(description = "宠物昵称")
    private String petName;
    @Schema(description = "宠物封面")
    private String petCover;

    // --- 送养人信息 (关键新增) ---
    @Schema(description = "送养人ID (宠物发布者)")
    private Long ownerId;
    @Schema(description = "送养人昵称")
    private String ownerName;

    // --- 申请人信息 ---
    @Schema(description = "申请人ID")
    private Long applicantId;
    @Schema(description = "申请人真实姓名")
    private String applicantName;
    @Schema(description = "申请人电话")
    private String applicantPhone;

    // --- 状态与时间 ---
    @Schema(description = "当前状态: 0-待审核 1-通过 2-驳回 3-取消")
    private Integer status;
    @Schema(description = "申请时间")
    private LocalDateTime createTime;
}
