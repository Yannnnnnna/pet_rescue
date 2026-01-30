package com.wei.pet.pet_rescue.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "沟通记录展示")
public class ConsultationVO {
    @Schema(description = "id")
    private Long id;

    // --- 提问部分 ---
    @Schema(description = "问题")
    private String question;
    @Schema(description = "提问配图列表")
    private List<String> askImgList; // 【修改】提问配图数组
    @Schema(description = "提问时间")
    private LocalDateTime createTime;

    // --- 回复部分 ---
    @Schema(description = "回答")
    private String answer;
    @Schema(description = "回复配图列表")
    private List<String> replyImgList; // 【修改】回复配图数组
    @Schema(description = "回复时间")
    private LocalDateTime replyTime;
    @Schema(description = "状态: 0-待回复 1-已回复")
    private Integer status; // 0-待回复 1-已回复

    // --- 辅助信息 (用于展示头像昵称) ---
    @Schema(description = "宠物ID")
    private Long petId;
    @Schema(description = "宠物名称")
    private String petName;
    @Schema(description = "宠物封面图URL")
    private String petCover;
    @Schema(description = "提问者ID")
    private Long askUserId;
    @Schema(description = "回答者ID")
    private String askUserNickname;
    @Schema(description = "提问者头像URL")
    private String askUserAvatar;

    // 如果需要显示回复人(管理员/送养人)的信息也可以加上
    @Schema(description = "回复者ID")
    private String replyUserNickname;
    @Schema(description = "回复者头像URL")
    private String replyUserAvatar;
}