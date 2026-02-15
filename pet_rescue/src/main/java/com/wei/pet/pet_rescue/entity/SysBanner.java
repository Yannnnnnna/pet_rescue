package com.wei.pet.pet_rescue.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 首页轮播图表
 * </p>
 *
 * @author yanna
 * @since 2026-02-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_banner")
@Schema(description="首页轮播图表")
public class SysBanner implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "图片地址(你的壁纸URL)")
    private String imgUrl;
    @Schema(description = "点击跳转的链接/页面路径(可选)")
    private String targetUrl;
    @Schema(description = "排序(数字越小越靠前)")
    private Integer sortOrder;
    @Schema(description = "状态: 0-隐藏 1-显示")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
