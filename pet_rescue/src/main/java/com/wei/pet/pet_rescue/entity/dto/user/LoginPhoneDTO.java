package com.wei.pet.pet_rescue.entity.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wyr on 2026/1/26
 */
@Data
@Schema(description = "手机号密码登录请求参数")
public class LoginPhoneDTO {

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "验证码")
    private String code;
}