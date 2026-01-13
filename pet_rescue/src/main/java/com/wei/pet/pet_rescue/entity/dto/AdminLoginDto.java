package com.wei.pet.pet_rescue.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Web后台登录参数")
public class AdminLoginDto {
    @Schema(description = "账号")
    @NotBlank(message = "账号不能为空")
    private String username;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    private String password;
}
