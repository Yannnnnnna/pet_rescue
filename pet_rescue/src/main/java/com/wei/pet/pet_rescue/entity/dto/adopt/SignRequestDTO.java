package com.wei.pet.pet_rescue.entity.dto.adopt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wyr on 2026/2/14
 */
@Data
@Schema(description = "领养协议签名请求")
public class SignRequestDTO {
    @Schema(description = "领养申请单ID")
    private Long adoptionId;    // 申请单ID
    @Schema(description = "签名图片URL")
    private String signatureImg; // 前端上传OSS后的签名图片地址
}

