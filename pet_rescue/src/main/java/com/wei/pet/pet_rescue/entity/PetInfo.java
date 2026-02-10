package com.wei.pet.pet_rescue.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 宠物档案表
 * </p>
 *
 * @author yanna
 * @since 2026-01-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("pet_info")
@Schema(name ="PetInfo对象", description="宠物档案表")
public class PetInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    @Schema(description = "宠物昵称")
    private String name;

    @Schema(description = "类型: 0-猫 1-狗 2-鸟类 3-异宠 4-其他")
    private Integer type;

    @Schema(description = "具体品种(如:橘猫,金毛)")
    private String breed;

    @Schema(description = "年龄(如:3个月, 2岁)")
    private String age;
    @Schema(description = "性别: 0-母 1-公 2-未知")
    private Integer sex;

    @Schema(description = "状态: 0-待领养 1-申请中 2-已领养")
    private Integer status;
    @Schema(description = "封面图")
    private String coverImg;

    @Schema(description = "详情图集(逗号分隔的URL字符串)")
    private String detailImgs;
    @Schema(description = "标签(逗号分隔,如:粘人,安静,已绝育)")
    private String tags;

    @Schema(description = "救助故事/详细介绍")
    private String description;
    @Schema(description = "是否绝育: 0-否 1-是")
    private Integer isSterilized;

    @Schema(description = "是否接种疫苗: 0-否 1-是")
    private Integer isVaccinated;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "入库时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除")
    @TableLogic
    private Integer isDeleted;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "详细地点")
    private String address;
    @Schema(description = "发布人ID")
    private Long publisherId;

    @Schema(description = "投喂小鱼干数量")
    private Integer lovePoint;
}
