package com.wei.pet.pet_rescue.entity.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wyr on 2026/1/19
 */
@Data
@Schema(description = "文章列表查询条件")
public class ArticleQueryDTO {

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "搜索关键字(标题/摘要)")
    private String keyword;

    @Schema(description = "类型: 0-百科 1-公告 2-活动 3-壁纸")
    private Integer type;

    @Schema(description = "分类筛选")
    private String category;
}
