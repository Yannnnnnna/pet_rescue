package com.wei.pet.pet_rescue.entity.dto.pet;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wyr on 2026/2/9
 */
@Data
@Schema(description = "投喂小鱼干")
public class PetFeedDTO {
    @Schema(description = "宠物ID")
    private Long petId;

    @Schema(description = "投喂数量(单位:小鱼干颗数)")
    private Integer feedCount;
}
