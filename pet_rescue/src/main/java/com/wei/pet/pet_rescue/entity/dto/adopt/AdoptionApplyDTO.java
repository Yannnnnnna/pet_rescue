package com.wei.pet.pet_rescue.entity.dto.adopt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author wyr on 2026/1/19
 */
@Data
@Schema(description = "领养申请表单")
public class AdoptionApplyDTO {

    @Schema(description = "宠物ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "宠物ID不能为空")
    private Long petId;

//    // ⚠️ 注意：正式上线时 userId 应从 Token 获取，毕设测试期间允许前端传
//    @Schema(description = "申请人ID (测试用)", example = "1")
//    @NotNull(message = "申请人ID不能为空")
//    private Long userId;

    @Schema(description = "真实姓名", example = "张三")
    @NotBlank(message = "姓名不能为空")
    private String realName;

    @Schema(description = "联系电话", example = "13800138000")
    @NotBlank(message = "电话不能为空")
    private String phone;

    @Schema(description = "居住地址", example = "重庆市巴南区...")
    private String address;

    @Schema(description = "住房情况: 自有/租房", example = "租房")
    private String housingCondition;

    @Schema(description = "工作状况", example = "在职")
    private String jobStatus;

    @Schema(description = "养宠经验", example = "养过一只橘猫")
    private String experience;

    @NotBlank(message = "领养理由不能为空") // 可以加校验注解
    @Schema(description = "领养理由", example = "我非常喜欢宠物，想给它一个温暖的家")
    private String reason;
}
