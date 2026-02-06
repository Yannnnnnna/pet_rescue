package com.wei.pet.pet_rescue.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 文章/百科表
 * </p>
 *
 * @author yanna
 * @since 2026-01-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cms_article")
@Schema(name = "CmsArticle对象", description="文章/百科表")
public class CmsArticle implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章摘要(列表页展示用)")
    private String summary;

    @Schema(description= "标签(逗号分隔,如:猫,感冒)")
    private String tags;

    @Schema(description= "类型: 0-养宠百科 1-救助公告 2-活动")
    private Integer type;

    @Schema(description= "分类(如: 疾病医疗, 喂养指南)")
    private String category;

    @Schema(description = "封面图")
    private String coverImg;

    @Schema(description = "文章内容(富文本HTML)")
    private String content;

    @Schema(description = "发布者")
    private String author;

    @Schema(description = "阅读量")
    private Integer viewCount;

    @Schema(description = "是否已同步AI向量库: 0-否 1-是")
    private Integer isSynced;

    @Schema(description = "发布时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除")
    @TableLogic
    private Integer isDeleted;

    // === 新增字段 ===

    @Schema(description = "活动开始时间")
    private LocalDateTime activityStartTime;

    @Schema(description = "活动结束时间")
    private LocalDateTime activityEndTime;

    @Schema(description = "活动地点")
    private String activityAddress;

    @Schema(description = "壁纸高清大图链接")
    private String wallpaperUrl;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "是否已经点赞")
    @TableField(exist = false)
    private Boolean isLiked;   // 当前用户是否已点赞 (查询 cms_article_like 表是否存在记录)

    @Schema(description = "AI摘要")
    private String aiSummary;
}
