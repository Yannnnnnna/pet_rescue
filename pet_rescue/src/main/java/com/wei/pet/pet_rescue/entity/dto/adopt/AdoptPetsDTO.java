package com.wei.pet.pet_rescue.entity.dto.adopt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author wyr on 2026/1/27
 */
@Data
@Schema(description = "我的领养宠物卡片信息")
public class AdoptPetsDTO {

    @Schema(description = "宠物ID")
    private Long id;

    @Schema(description = "宠物昵称")
    private String name;

    @Schema(description = "宠物封面图")
    private String cover;

    @Schema(description = "品种")
    private String breed;

    @Schema(description = "性别: 0-母 1-公")
    private Integer sex;

    @Schema(description = "疫苗状态: 0-未接种 1-已接种")
    private Integer isVaccinated;

    // --- 领养相关 ---
    @Schema(description = "领养/通过时间 (用于计算陪伴天数)")
    private LocalDateTime adoptionTime;

    // --- 送养人相关 (用于回访联系) ---
    @Schema(description = "送养人/发布人ID")
    private Long publisherId;

    @Schema(description = "送养人昵称")
    private String publisherNickname;

    @Schema(description = "送养人头像")
    private String publisherAvatar;
}
