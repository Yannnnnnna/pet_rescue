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
 * 领养生活记录贴
 * </p>
 *
 * @author yanna
 * @since 2026-02-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("adoption_post")
@Schema(description="领养生活记录贴")
public class AdoptionPost implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    @Schema(description = "发布人ID")
    private Long userId;
    @Schema(description = "关联的宠物ID")
    private Long petId;

    @Schema(description = "文字内容")
    private String content;

    @Schema(description = "图片地址(JSON数组或逗号分隔字符串)")
    private String images;

    @Schema(description = "点赞数(可选)")
    private Integer likeCount;

    @Schema(description = "状态: 0-待审核 1-正常 2-违规")
    private Integer auditStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;


}
