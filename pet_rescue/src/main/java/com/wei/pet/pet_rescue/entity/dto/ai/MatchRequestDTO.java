package com.wei.pet.pet_rescue.entity.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wyr on 2026/2/6
 */
@Data
public class MatchRequestDTO {
    @Schema(description = "宠物ID")
    private Long petId;

    @Schema(description = "申请记录id")
    private Long adoptId;

    @Schema(description = "申请人ID")
    private Long userId;

    /**
     * 评价风格：
     * "strict" - 严格犀利 (默认)
     * "gentle" - 温柔鼓励
     * "humorous" - 幽默风趣
     */
    @Schema(description = "评价风格", example = "strict 严格犀利 gentle 温柔鼓励 humorous 幽默风趣")
    private String style = "strict";

    /**
     * 是否强制重新生成
     */
    @Schema(description = "是否强制重新生成", example = "false")
    private Boolean refresh = false;
}
