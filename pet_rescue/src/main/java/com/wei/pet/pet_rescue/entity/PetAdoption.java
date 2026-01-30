package com.wei.pet.pet_rescue.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 领养申请表
 * </p>
 *
 * @author yanna
 * @since 2026-01-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("pet_adoption")
@Schema(name ="PetAdoption对象", description="领养申请表")
public class PetAdoption implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "申请人ID")
    private Long userId;

    @Schema(description = "宠物ID")
    private Long petId;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "居住地址")
    private String address;

    @Schema(description = "养宠经验")
    private String experience;

    @Schema(description = "住房情况(自有/租房)")
    private String housingCondition;

    @Schema(description = "工作状况")
    private String jobStatus;

    @Schema(description = "审核状态: 0-待审核 1-通过 2-驳回 3-已取消")
    private Integer status;

    @Schema(description = "管理员审核备注(驳回原因)")
    private String adminRemark;

    @Schema(description = "申请时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除")
    @TableLogic
    private Integer isDeleted;


}
