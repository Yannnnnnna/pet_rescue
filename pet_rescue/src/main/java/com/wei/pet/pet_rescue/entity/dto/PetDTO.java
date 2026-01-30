package com.wei.pet.pet_rescue.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wyr on 2026/1/17
 */
@Data
@Schema(description = "宠物信息表单（新增/修改）")
public class PetDTO {

    @Schema(description = "宠物ID (修改时必填)", example = "1")
    private Long id;

    @Schema(description = "宠物昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "大黄")
    @NotBlank(message = "宠物昵称不能为空")
    private String name;

    @Schema(description = "类型: 0-猫 1-狗 2-鸟 3-异宠 4-其他", example = "0")
    @NotNull(message = "宠物类型不能为空")
    private Integer type;

    @Schema(description = "品种", example = "中华田园猫")
    private String breed;

    @Schema(description = "年龄", example = "3个月")
    private String age;

    @Schema(description = "性别: 0-母 1-公 2-未知", example = "0")
    private Integer sex;

    @Schema(description = "状态: 0-待领养 1-申请中 2-已领养", example = "0")
    private Integer status;

    @Schema(description = "封面图URL", example = "https://oss.../1.jpg")
    @NotBlank(message = "封面图不能为空")
    private String coverImg;

    @Schema(description = "详情图列表(数组)", example = "[\"url1\", \"url2\"]")
    private List<String> detailImgList;

    @Schema(description = "标签(逗号分隔)", example = "粘人,已绝育")
    private String tags;

    @Schema(description = "详细介绍/救助故事")
    private String description;

    @Schema(description = "是否绝育: 0-否 1-是")
    private Integer isSterilized;

    @Schema(description = "是否接种疫苗: 0-否 1-是")
    private Integer isVaccinated;

    @Schema(description = "省份", example = "重庆市")
    private String province;

    @Schema(description = "城市", example = "重庆市")
    private String city;

    @Schema(description = "详细地点", example = "花溪校区北门")
    private String address;

    @Schema(description = "发布时间")
    private LocalDateTime createTime;
}