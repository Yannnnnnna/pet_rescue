package com.wei.pet.pet_rescue.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author wyr on 2026/2/3
 */
@Schema(description = "文章收藏传输对象")
@Data
public class CmsArticleFavoriteDTO {
    @Schema(description = "文章ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
}
