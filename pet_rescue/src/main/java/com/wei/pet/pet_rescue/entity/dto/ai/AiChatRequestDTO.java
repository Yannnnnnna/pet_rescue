package com.wei.pet.pet_rescue.entity.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wyr on 2026/2/5
 */
@Schema(description = "AI 对话请求参数")
@Data
public class AiChatRequestDTO {
    /*
    * 会话ID。
    * 如果是新会话，传 null；
    * 如果是继续聊天，传具体的 ID。
    */
    @Schema(description = "会话ID")
    private Long sessionId;

    /**
     * 用户的问题
     */
    @Schema(description = "用户问题")
    private String question;

    /**
     * 是否开启深度思考/联网搜索 (true/false)
     */
    @Schema(description = "是否开启深度思考")
    private Boolean enableThinking;


    /**
     * 当前咨询的宠物ID (可选)
     */
    @Schema(description = "当前咨询的宠物ID")
    private Long petId;
}
