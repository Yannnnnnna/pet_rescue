package com.wei.pet.pet_rescue.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author wyr on 2026/1/30
 */
@Data
@Schema(description = "回访DTO")
public class ReturnVisitDTO {
    @Schema(description = "宠物ID")
    @NotNull(message = "宠物ID不能为空")
    private Long petId;

    @Schema(description = "回访内容")
    @NotBlank(message = "回访内容不能为空")
    private String question; // 复用 question 字段作为回访的问题

    @Schema(description = "配图列表")
    private List<String> images; // 配图
}
