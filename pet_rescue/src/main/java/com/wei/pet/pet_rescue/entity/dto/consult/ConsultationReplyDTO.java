package com.wei.pet.pet_rescue.entity.dto.consult;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author wyr on 2026/1/21
 */
@Data
public class ConsultationReplyDTO {
    @NotNull(message = "咨询记录ID不能为空")
    private Long id;

    @NotBlank(message = "回复内容不能为空")
    private String answer;

    @Schema(description = "配图列表")
    private List<String> images;
}