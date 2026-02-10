package com.wei.pet.pet_rescue.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wyr on 2026/1/26
 */
@Data
@Schema(description = "用户信息展示对象")
public class UserInfoVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户名/账号")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "角色")
    private Integer role;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "小鱼干数量")
    private Integer coin;


    @Schema(description = "是否已设置密码")
    private Boolean hasPassword;
}
