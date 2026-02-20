package com.wei.pet.pet_rescue.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 是否点赞，收藏的返回结果
 *
 * @author wyr on 2026/2/19
 */
@Data
@Schema(description = "是否点赞，收藏的返回结果")
public class CheckResultVO {
    @Schema(description = "是否点赞/收藏")
    private Boolean checked;
    @Schema(description = "数量")
    private Long count;
}
