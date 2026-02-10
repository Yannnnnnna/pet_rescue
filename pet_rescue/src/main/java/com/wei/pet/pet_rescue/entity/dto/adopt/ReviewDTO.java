package com.wei.pet.pet_rescue.entity.dto.adopt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wyr on 2026/2/9
 */
@Data
@Schema(description = "审核领养生活记录贴")
public class ReviewDTO {
    @Schema(description = "领养生活记录贴ID")
    private Long postId;
    @Schema(description = "审核状态：1-通过，2-驳回")
    private Integer status;
}
