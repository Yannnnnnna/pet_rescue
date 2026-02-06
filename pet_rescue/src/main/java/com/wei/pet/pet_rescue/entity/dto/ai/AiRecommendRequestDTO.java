package com.wei.pet.pet_rescue.entity.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wyr on 2026/2/6
 */
@Data
@Schema(description = "AI 推荐请求参数")
public class AiRecommendRequestDTO {
    @Schema(description = "居住环境", example = "公寓")
    private String housing;     // 居住环境 (如: 租房/公寓/别墅)
    @Schema(description = "闲暇时间", example = "工作忙")
    private String time;        // 闲暇时间 (如: 工作忙/空闲多)
    @Schema(description = "养宠经验", example = "有经验")
    private String experience;  // 养宠经验 (如: 新手/有经验)
    @Schema(description = "偏好", example = "粘人")
    private String preference;  // 偏好 (如: 喜欢安静/粘人)
    @Schema(description = "是否开启深度思考", example = "true")
    private Boolean enableThinking; // 是否开启深度思考
}
