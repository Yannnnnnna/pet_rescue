package com.wei.pet.pet_rescue.entity.dto.article;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author wyr on 2026/1/19
 */
@Data
@Schema(description = "文章/壁纸发布表单")
public class ArticleFormDTO {

    @Schema(description = "ID (修改时必填)", example = "1")
    private Long id;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "新手养猫指南：第一天该做什么？")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "摘要 (列表页展示)", example = "接猫回家前的准备工作全解析...")
    private String summary;

    @Schema(description = "标签 (逗号分隔)", example = "新手,猫咪,健康")
    private String tags;

    @Schema(description = "类型: 0-养宠百科 1-救助公告 2-活动 3-壁纸", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "类型不能为空")
    private Integer type;

    @Schema(description = "分类 (如: 疾病医疗, 喂养指南, 萌宠壁纸)", example = "喂养指南")
    private String category;

    @Schema(description = "封面图URL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "封面图不能为空")
    private String coverImg;

    @Schema(description = "文章内容 (富文本HTML) 或 壁纸描述", example = "<p>正文内容...</p>")
    private String content;


    @Schema(description = "活动开始时间 (类型为活动时必填)")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime activityStartTime;

    @Schema(description = "活动结束时间 (类型为活动时必填)")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime activityEndTime;

    @Schema(description = "活动地点 (类型为活动时必填)")
    private String activityAddress;

    @Schema(description = "壁纸高清大图 (类型为壁纸时必填)")
    private String wallpaperUrl;
}
