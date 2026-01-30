package com.wei.pet.pet_rescue.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 收藏操作传输对象
 */
@Data
@Schema(description = "收藏操作参数")
public class PetFavoriteDTO implements Serializable {

    @Schema(description = "宠物ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "宠物ID不能为空")
    private Long petId;
}