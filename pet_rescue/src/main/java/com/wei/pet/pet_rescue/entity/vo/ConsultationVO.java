package com.wei.pet.pet_rescue.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author wyr on 2026/1/21
 */
@Data
public class ConsultationVO {
    private Long id;
    private String question;
    private String answer;
    private Integer status; // 0-待回复 1-已回复
    private LocalDateTime createTime;
    private LocalDateTime replyTime;

    // 宠物信息 (用于让送养人知道是问哪只猫)
    private Long petId;
    private String petName;
    private String petCover;

    // 提问者信息 (用于让送养人知道是谁问的)
    private Long askUserId;
    private String askUserNickname;
    private String askUserAvatar;
}