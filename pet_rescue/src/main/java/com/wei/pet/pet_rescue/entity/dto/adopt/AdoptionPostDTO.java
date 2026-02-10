package com.wei.pet.pet_rescue.entity.dto.adopt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wyr on 2026/2/9
 */
@Data
@Schema(description = "领养生活记录贴")
public class AdoptionPostDTO {
    @Schema(description = "领养生活记录贴ID")
    private Long id;
    @Schema(description = "宠物ID")
    private Long petId;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "图片URL列表，逗号分隔")
    private String images;

}
