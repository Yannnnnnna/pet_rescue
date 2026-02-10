package com.wei.pet.pet_rescue.entity.dto.adopt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wyr on 2026/2/9
 */
@Data
@Schema(description = "领养生活记录贴查询参数")
public class AdoptionPostQueryDTO {
    @Schema(description = "宠物ID")
    private Long petId;

    @Schema(description = "发布人ID")
    private Long userId;

    @Schema(description = "审核状态：0-待审核 1-正常 2-违规")
    private Integer auditStatus;
}
