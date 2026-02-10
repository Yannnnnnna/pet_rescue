package com.wei.pet.pet_rescue.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author wyr on 2026/2/9
 */
@Data
@Schema(description = "首页统计数据")
public class AdminDashboardVO {
    // === 顶部核心卡片数据 ===
    @Schema(description = "宠物总数")
    private Long totalRescueCount;      // 累计救助数量 (宠物总数)
    @Schema(description = "领养总数")
    private Long totalAdoptionCount;    // 已被领养数量
    @Schema(description = "领养率")
    private BigDecimal adoptionRate;    // 领养率 (百分比)
    @Schema(description = "待审核的申请数量")
    private Long pendingAuditCount;     // 待审核的申请数量 (给管理员的红点提醒)

    // === 图表数据 ===
    // 1. 热门品种分布 (用于饼图: name=品种, value=数量)
    @Schema(description = "热门品种分布 name=品种, value=数量")
    private List<Map<String, Object>> breedDistribution;

    // === 图表数据 ===
    @Schema(description = "近7天趋势-X轴日期")
    private List<String> dates;

    @Schema(description = "近7天趋势-Y轴数据 (救助量)")
    private List<Integer> dailyRescueData;

    @Schema(description = "近7天趋势-Y轴数据 (领养申请量)")
    private List<Integer> dailyAdoptionData;
}
