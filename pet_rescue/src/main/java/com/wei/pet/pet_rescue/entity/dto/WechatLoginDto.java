package com.wei.pet.pet_rescue.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "小程序登录参数")
public class WechatLoginDto {
    @Schema(description = "微信临时登录凭证(code)")
    @NotBlank(message = "code不能为空")
    private String code;

    // 毕设里手机号通常是选填，或者后面再补，登录时只要code就够了
}