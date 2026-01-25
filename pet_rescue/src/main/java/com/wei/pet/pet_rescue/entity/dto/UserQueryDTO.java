package com.wei.pet.pet_rescue.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wyr on 2026/1/23
 */
@Data
@Schema(description = "用户列表查询条件")
public class UserQueryDTO {

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "搜索关键字(用户名/昵称/手机号)")
    private String keyword;

    @Schema(description = "角色: 0-普通用户 1-管理员")
    private Integer role;

}