package com.wei.pet.pet_rescue.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wyr on 2026/1/17
 */
@Data
@Schema(description = "宠物列表查询条件")
public class PetQueryDTO {

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "搜索关键字(昵称/品种)")
    private String keyword;

    @Schema(description = "类型: 0-猫 1-狗...")
    private Integer type;

    @Schema(description = "状态: 0-待领养... (不传查所有)")
    private Integer status;

    @Schema(description = "城市", example = "重庆市")
    private String city;

}
