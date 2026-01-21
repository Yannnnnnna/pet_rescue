package com.wei.pet.pet_rescue.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author wyr on 2026/1/21
 */
@Data
public class ConsultationReplyDTO {
    @NotNull(message = "咨询记录ID不能为空")
    private Long id;

    @NotBlank(message = "回复内容不能为空")
    private String answer;
}