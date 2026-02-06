package com.wei.pet.pet_rescue.controller.ai;


import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.entity.PetInfo;
import com.wei.pet.pet_rescue.entity.PetMatchRecord;
import com.wei.pet.pet_rescue.entity.SysAiMessage;
import com.wei.pet.pet_rescue.entity.SysAiSession;
import com.wei.pet.pet_rescue.entity.dto.ai.AiChatRequestDTO;
import com.wei.pet.pet_rescue.entity.dto.ai.AiRecommendRequestDTO;
import com.wei.pet.pet_rescue.entity.dto.ai.MatchRequestDTO;
import com.wei.pet.pet_rescue.service.AiService;
import com.wei.pet.pet_rescue.service.IPetInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * AI对话历史表 前端控制器
 * </p>
 *
 * @author yanna
 * @since 2026-01-21
 */
@RestController
@RequestMapping("/ai")
@Tag(name = "AI 对话管理")
@CrossOrigin(origins = "*")
@Slf4j
public class AiController {
    @Autowired
    private AiService aiService;
    @Autowired
    private IPetInfoService petInfoService;

    // 1. 核心对话接口 (SSE)
    @Operation(summary = "AI 对话接口", description = "使用 Server-Sent Events (SSE) 实现实时响应的 AI 对话接口")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public SseEmitter chat(@RequestBody AiChatRequestDTO req) {
        // 如果没登录，StpUtil.getLoginIdAsLong() 会抛出异常，Sa-Token 会自动拦截
        return aiService.chat(req);
    }

    // 2. 获取会话列表 (左侧侧边栏)
    @Operation(summary = "获取会话列表", description = "获取当前用户的 AI 会话列表")
    @GetMapping("/session/list")
    public Result<List<SysAiSession>> getSessionList() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.success(aiService.getSessionList(userId));
    }

    // 3. 获取某个会话的历史记录 (点击会话时)
    @Operation(summary = "获取会话消息列表", description = "获取指定会话的 AI 消息列表")
    @GetMapping("/message/list")
    public Result<List<SysAiMessage>> getMessageList(@RequestParam Long sessionId) {
        return Result.success(aiService.getMessageList(sessionId));
    }

    @PostMapping("/recommend")
    @Operation(summary = "AI 推荐接口", description = "根据用户提供的信息，推荐合适的宠物领养选项")
    public Result<String> recommend(@RequestBody AiRecommendRequestDTO req) {
        String jsonResult = aiService.recommend(req);
        log.info("后端发给前端的内容: {}", jsonResult);
        // 这里前端拿到的是 String 类型的 JSON，前端自己 JSON.parse 即可
        return Result.success(jsonResult);
    }

    @Operation(summary = "生成/获取文章摘要")
    @GetMapping("/article/summary")
    public Result<String> getArticleSummary(
            @RequestParam Long articleId,
            @RequestParam(required = false, defaultValue = "false") Boolean refresh // 新增参数
    ) {
        String summary = aiService.generateArticleSummary(articleId, refresh);
        return Result.success(summary);
    }

    @PostMapping("/match/analyze")
    public Result<PetMatchRecord> analyzeMatch(@RequestBody MatchRequestDTO req) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 1. 查宠物信息，确认权限
        PetInfo pet = petInfoService.getById(req.getPetId());
        if (pet == null) return Result.error("宠物不存在");

        // 权限校验：
        // 1. 当前用户是这只宠物的发布者 (publisher_id)
        // 2. 或者当前用户是管理员
        boolean isOwner = pet.getPublisherId().equals(currentUserId);
        boolean isAdmin = StpUtil.hasRole("admin"); // 假设你有角色管理

        // 如果你自己就是申请人，想看匹配度（可选功能），也可以放开
        boolean isSelf = req.getUserId().equals(currentUserId);

        if (!isOwner && !isAdmin && !isSelf) {
            return Result.error("您无权查看此匹配分析");
        }

        return Result.success(aiService.analyzeMatch(req));
    }

}
