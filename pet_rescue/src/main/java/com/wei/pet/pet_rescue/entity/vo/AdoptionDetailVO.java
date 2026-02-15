package com.wei.pet.pet_rescue.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author wyr on 2026/1/24
 */
@Data
@Schema(description = "领养申请详情对象 (用于审核页)")
public class AdoptionDetailVO {

    // --- 1. 基础信息 ---
    @Schema(description = "申请单ID")
    private Long id;

    @Schema(description = "申请状态: 0-待审核 1-通过 2-驳回 3-取消")
    private Integer status;

    @Schema(description = "申请时间")
    private LocalDateTime createTime;

    @Schema(description = "管理员审核备注(驳回原因)")
    private String adminRemark;

    // --- 2. 关联的宠物信息 ---
    @Schema(description = "宠物ID")
    private Long petId;

    @Schema(description = "宠物昵称")
    private String petName;

    @Schema(description = "宠物封面图")
    private String petCover;

    // --- 3. 关联的申请人账号信息 (来自 sys_user) ---
    @Schema(description = "申请人ID")
    private Long userId;

    @Schema(description = "申请人昵称")
    private String userNickname;

    @Schema(description = "申请人头像")
    private String userAvatar;

    // --- 4. 详细表单数据 (来自 pet_adoption 表) ---
    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "居住地址")
    private String address;

    @Schema(description = "养宠经验 (核心审核依据)")
    private String experience;

    @Schema(description = "住房情况 (自有/租房)")
    private String housingCondition;

    @Schema(description = "工作状况")
    private String jobStatus;

    @Schema(description = "领养理由")
    private String reason;

    @Schema(description = "电子签名图片URL")
    private String signatureImg;

    @Schema(description = "签署时间")
    private LocalDateTime signTime;

    @Schema(description = "协议状态: 0-未签署 1-已签署")
    private Integer agreementStatus;
}
