package com.wei.pet.pet_rescue.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author wyr on 2026/1/21
 */
@Data
public class ConsultationAskDTO {
    @NotNull(message = "宠物ID不能为空")
    private Long petId;

    @NotBlank(message = "问题内容不能为空")
    private String question;
}
