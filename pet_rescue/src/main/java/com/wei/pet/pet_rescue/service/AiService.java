package com.wei.pet.pet_rescue.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wei.pet.pet_rescue.common.NlpUtils;
import com.wei.pet.pet_rescue.entity.*;
import com.wei.pet.pet_rescue.entity.dto.ai.AiChatRequestDTO;
import com.wei.pet.pet_rescue.entity.dto.ai.AiRecommendRequestDTO;
import com.wei.pet.pet_rescue.entity.dto.ai.MatchRequestDTO;
import com.wei.pet.pet_rescue.mapper.PetMatchRecordMapper;
import com.wei.pet.pet_rescue.service.ICmsArticleService;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class AiService {

    // 读取配置文件中的模型名称
    @Value("${ai.qwen.model}")
    private String modelName;

    @Autowired
    private ICmsArticleService articleService;
    @Autowired
    private ISysAiSessionService sessionService;
    @Autowired
    private ISysAiMessageService messageService;
    @Autowired
    private PetMatchRecordMapper petMatchRecordMapper;
    @Autowired
    private IPetInfoService petInfoService;
    @Autowired
    private IPetAdoptionService petAdoptionService;

    // 线程池
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 核心对话方法 (流式)
     */
    public SseEmitter chat(AiChatRequestDTO req) {
        long userId = StpUtil.getLoginIdAsLong();
        SseEmitter emitter = new SseEmitter(180000L); // 3分钟超时

        executor.execute(() -> {
            StringBuilder fullAiResponse = new StringBuilder();
            Long finalSessionId = req.getSessionId();

            try {
                // === Step 1: 会话管理 ===
                if (finalSessionId == null || finalSessionId == 0) {
                    SysAiSession session = new SysAiSession();
                    session.setUserId(userId);
                    session.setTitle(req.getQuestion().length() > 10 ? req.getQuestion().substring(0, 10) : req.getQuestion());
                    session.setCreateTime(LocalDateTime.now());
                    session.setUpdateTime(LocalDateTime.now());
                    sessionService.save(session);
                    finalSessionId = session.getId();
                } else {
                    SysAiSession update = new SysAiSession();
                    update.setId(finalSessionId);
                    update.setUpdateTime(LocalDateTime.now());
                    sessionService.updateById(update);
                }

                // === Step 2: 保存用户提问 ===
                SysAiMessage userMsg = new SysAiMessage();
                userMsg.setSessionId(finalSessionId);
                userMsg.setRole("user");
                userMsg.setContent(req.getQuestion());
                userMsg.setCreateTime(LocalDateTime.now());
                messageService.save(userMsg);
                // === 核心修改：构建上下文 (Context) ===
                // === 核心修改：构建上下文 (Context) ===
                StringBuilder contextBuilder = new StringBuilder();

// 1. 如果选择了宠物，查询宠物档案并注入
                if (req.getPetId() != null) {
                    PetInfo pet = petInfoService.getById(req.getPetId());
                    if (pet != null) {
                        // 计算时间差辅助信息
                        Date date = Date.from(pet.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant());
                        String updateTimeStr = DateUtil.formatDate(date);
                        String todayStr = cn.hutool.core.date.DateUtil.today();

                        String petProfile = String.format(
                                "【当前咨询的宠物档案】\n" +
                                        "- 昵称：%s\n" +
                                        "- 品种：%s\n" +
                                        "- 初始登记年龄：%s (登记于 %s)\n" + // 🔥 注入时间
                                        "- 初始性别：%s\n" +
                                        "- 绝育状态(登记时)：%s\n" +
                                        "- 疫苗状态(登记时)：%s\n" +
                                        "- 既往情况：%s\n" +
                                        "⚠️ 重要提示：\n" +
                                        "1. 当前日期是 %s。请根据‘登记日期’和‘初始年龄’，自动推算宠物现在的实际年龄。\n" +
                                        "2. 绝育/疫苗状态为登记时的数据，如果用户在提问中提到了最新的状态变化，请以用户的提问为准。\n\n",

                                pet.getName(),
                                pet.getBreed(),
                                pet.getAge(),
                                updateTimeStr, // 告诉 AI 这是什么时候的数据
                                pet.getSex() == 0 ? "母" : "公",
                                pet.getIsSterilized() == 1 ? "已绝育" : "未绝育",
                                pet.getIsVaccinated() == 1 ? "已接种" : "未接种",
                                pet.getDescription(),
                                todayStr // 告诉 AI 今天是多少号
                        );
                        contextBuilder.append(petProfile);
                    }
                }

                // === Step 3: 准备 Context ===
                List<Message> messages = new ArrayList<>();
                String basePrompt = "你是一个专业的宠物医生助手。请根据参考资料回答。如果开启了深度思考，请利用联网能力。";
                String systemPrompt = basePrompt + contextBuilder.toString();
                // 检索相关文章作为参考资料
                List<String> keywords = NlpUtils.extractKeywords(req.getQuestion());
                if (!keywords.isEmpty()) {
                    List<CmsArticle> articles = articleService.lambdaQuery()
                            .eq(CmsArticle::getType, 0)
                            .and(w -> { for (String key : keywords) w.like(CmsArticle::getContent, key).or(); })
                            .last("LIMIT 2")
                            .list();
                    if (!articles.isEmpty()) {
                        StringBuilder ref = new StringBuilder("\n【参考资料】：\n");
                        for (CmsArticle art : articles) {
                            ref.append(art.getContent().substring(0, Math.min(art.getContent().length(), 200))).append("\n");
                        }
                        systemPrompt += ref.toString();
                    }
                }
                messages.add(createMessage(Role.SYSTEM, systemPrompt));

                List<SysAiMessage> history = messageService.lambdaQuery()
                        .eq(SysAiMessage::getSessionId, finalSessionId)
                        .orderByDesc(SysAiMessage::getId)
                        .last("LIMIT 6")
                        .list();
                Collections.reverse(history);

                for (SysAiMessage m : history) {
                    if(m.getId().equals(userMsg.getId())) continue;
                    Role role = "user".equals(m.getRole()) ? Role.USER : Role.ASSISTANT;
                    messages.add(createMessage(role, m.getContent()));
                }

                messages.add(createMessage(Role.USER, req.getQuestion()));

                // === Step 4: 调用 Qwen ===
                GenerationParam param = GenerationParam.builder()
                        .model(modelName)
                        .messages(messages)
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .topP(0.8)
                        .enableSearch(req.getEnableThinking())
                        .incrementalOutput(true)
                        .build();

                Generation gen = new Generation();
                Flowable<GenerationResult> result = gen.streamCall(param);

                // === Step 5: 流式处理 ===
                Long finalIdForSave = finalSessionId;
                result.blockingForEach(message -> {
                    String content = message.getOutput().getChoices().get(0).getMessage().getContent();
                    if (content != null) {
                        fullAiResponse.append(content);
                        Map<String, String> dataMap = new HashMap<>();
                        dataMap.put("content", content);
                        emitter.send(SseEmitter.event().data(dataMap));
                    }
                });

                // === Step 6: 保存 AI 回复 ===
                SysAiMessage aiMsg = new SysAiMessage();
                aiMsg.setSessionId(finalIdForSave);
                aiMsg.setRole("assistant");
                aiMsg.setContent(fullAiResponse.toString());
                aiMsg.setCreateTime(LocalDateTime.now());
                messageService.save(aiMsg);

                emitter.complete();

            } catch (Exception e) {
                log.error("AI 对话异常", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 智能选宠推荐
     */
    public String recommend(AiRecommendRequestDTO req) {
        try {
            String userProfile = String.format(
                    "{ 居住: %s, 时间: %s, 经验: %s, 偏好: %s }",
                    req.getHousing(), req.getTime(), req.getExperience(), req.getPreference()
            );

            String systemPrompt = "你是一个专业的宠物匹配专家。请根据用户的画像，推荐 3 种最适合的宠物类型。\n" +
                    "⚠️ 重要：请务必只返回纯 JSON 格式数据，不要包含 Markdown 代码块（如 ```json），不要包含其他废话。\n" +
                    "JSON 格式要求如下：\n" +
                    "{\n" +
                    "  \"analysis\": \"对用户的整体分析建议（100字以内）\",\n" +
                    "  \"recommendations\": [\n" +
                    "    { \"petName\": \"宠物/品种名称\", \"reason\": \"推荐理由\" }\n" +
                    "  ]\n" +
                    "}";

            String userPrompt = "用户画像数据：" + userProfile;
            return callQwenCommon(systemPrompt, userPrompt);

        } catch (Exception e) {
            log.error("AI 推荐失败", e);
            return "{\"analysis\":\"AI 大脑开小差了，请稍后再试。\",\"recommendations\":[]}";
        }
    }

    /**
     * 文章摘要生成
     */
    public String generateArticleSummary(Long articleId, Boolean refresh) {
        CmsArticle article = articleService.getById(articleId);
        if (article == null) throw new RuntimeException("文章不存在");

        if (!refresh && StrUtil.isNotBlank(article.getAiSummary())) {
            return article.getAiSummary();
        }

        String content = article.getContent();
        if (content.length() > 10000) content = content.substring(0, 10000);

        String systemPrompt = "你是一个专业的编辑。请用简练的语言（100字以内）总结这篇文章的核心观点。直接输出内容，不要包含'这篇文章'等废话。";
        String userPrompt = "文章内容：" + content;

        try {
            String summary = callQwenCommon(systemPrompt, userPrompt);
            if (StrUtil.isNotBlank(summary)) {
                CmsArticle updateBean = new CmsArticle();
                updateBean.setId(articleId);
                updateBean.setAiSummary(summary);
                updateBean.setUpdateTime(LocalDateTime.now());
                articleService.updateById(updateBean);
            }
            return summary;
        } catch (Exception e) {
            log.error("生成摘要失败", e);
            return "AI 正在偷懒，暂时无法生成摘要。";
        }
    }

    /**
     * 匹配分析
     */
    public PetMatchRecord analyzeMatch(MatchRequestDTO req) {
        // 1. 检查缓存
        // 这里先用前端传的 userId 查缓存，能省一次数据库查询
        if (!req.getRefresh()) {
            PetMatchRecord exist = petMatchRecordMapper.selectOne(new LambdaQueryWrapper<PetMatchRecord>()
                    .eq(PetMatchRecord::getPetId, req.getPetId())
                    .eq(PetMatchRecord::getUserId, req.getUserId()));
            if (exist != null) return exist;
        }

        // 2. 获取数据源
        PetInfo pet = petInfoService.getById(req.getPetId());
        if (pet == null) throw new RuntimeException("宠物不存在");

        // ✅ 改动点：直接通过 adoptId 获取申请记录 (更精准)
        PetAdoption adoption = petAdoptionService.getById(req.getAdoptId());
        if (adoption == null) throw new RuntimeException("领养申请记录不存在");

        // 🛡️【关键安全校验】🛡️
        // 确保这张申请单确实属于当前 Pet 和当前 User
        // 防止前端传错了 ID，或者恶意分析别人的申请单
        if (!adoption.getPetId().equals(req.getPetId())) {
            throw new RuntimeException("申请单与当前宠物不匹配");
        }
        // 这里建议以数据库查出来的 adoption.getUserId() 为准，或者校验两者是否一致
        if (!adoption.getUserId().equals(req.getUserId())) {
            throw new RuntimeException("申请单与当前用户不匹配");
        }

        // 3. 组装 Prompt
        String styleDesc = switch (req.getStyle()) {
            case "gentle" -> "语气温柔、以鼓励为主，委婉指出不足";
            case "humorous" -> "幽默风趣、带点调侃和梗";
            default -> "客观、严格、犀利，一针见血地指出潜在风险（如退养风险）";
        };

        String systemPrompt = "你是一个资深的宠物领养审核专家。请根据【宠物档案】和【申请人填写的资料】，计算匹配度（0-100分），并给出评价。\n" +
                "评价风格要求：" + styleDesc + "。\n" +
                "⚠️ 必须返回纯 JSON 格式，不要包含 Markdown 格式，JSON结构如下：\n" +
                "{\"score\": 85, \"reason\": \"这里写评价内容...\"}";

        String userPrompt = String.format(
                """
                【宠物档案】
                品种: %s, 年龄: %s, 性别: %s, 性格标签: %s
                特殊情况: %s (是否绝育:%s, 是否疫苗:%s)
                救助故事: %s
    
                【申请人资料】
                住房情况: %s
                工作状况: %s
                养宠经验: %s
                申请理由: %s
                """,
                pet.getBreed(), pet.getAge(), getSexStr(pet.getSex()), pet.getTags(),
                "无", pet.getIsSterilized() == 1 ? "是" : "否", pet.getIsVaccinated() == 1 ? "是" : "否",
                pet.getDescription(),
                adoption.getHousingCondition(), adoption.getJobStatus(), adoption.getExperience(), adoption.getReason()
        );

        // 4. 调用 AI
        String jsonResult = callQwenCommon(systemPrompt, userPrompt);

        // 5. 解析并存库
        try {
            JSONObject res = new JSONObject(jsonResult);
            Integer score = res.getInt("score");
            String reason = res.getStr("reason");

            PetMatchRecord record = new PetMatchRecord();
            record.setPetId(req.getPetId());
            // ✅ 建议：直接使用 adoption 对象里的 userId，保证数据绝对准确
            record.setUserId(adoption.getUserId());
            record.setMatchScore(score);
            record.setAnalysisResult(reason);
            record.setCreateTime(LocalDateTime.now());

            // 删除旧记录 (使用 adoption.getUserId() 确保删对人)
            petMatchRecordMapper.delete(new LambdaQueryWrapper<PetMatchRecord>()
                    .eq(PetMatchRecord::getPetId, req.getPetId())
                    .eq(PetMatchRecord::getUserId, adoption.getUserId()));

            petMatchRecordMapper.insert(record);
            return record;

        } catch (Exception e) {
            log.error("AI 分析解析失败: {}", jsonResult, e);
            throw new RuntimeException("AI 分析结果格式异常，请重试");
        }
    }

    // ================== 私有辅助方法 ==================

    /**
     * ✅ 新增：通用 AI 调用方法 (非流式/同步)
     */
    private String callQwenCommon(String systemPrompt, String userPrompt) {
        try {
            Message systemMsg = createMessage(Role.SYSTEM, systemPrompt);
            Message userMsg = createMessage(Role.USER, userPrompt);

            GenerationParam param = GenerationParam.builder()
                    .model(modelName)
                    .messages(Arrays.asList(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .topP(0.8)
                    .enableSearch(false)
                    .build();

            Generation gen = new Generation();
            GenerationResult result = gen.call(param);

            // 清洗结果，去掉可能的 Markdown 标记
            String content = result.getOutput().getChoices().get(0).getMessage().getContent();
            return content.replace("```json", "").replace("```", "").trim();

        } catch (Exception e) {
            log.error("AI 通用调用失败", e);
            throw new RuntimeException("AI 服务暂时不可用: " + e.getMessage());
        }
    }

    /**
     * 创建消息对象
     * @param role
     * @param content
     * @return
     */
    private Message createMessage(Role role, String content) {
        return Message.builder().role(role.getValue()).content(content).build();
    }

    /**
     * 性别转换
     * @param sex
     * @return
     */
    private String getSexStr(Integer sex) {
        if (sex == 0) return "母";
        if (sex == 1) return "公";
        return "未知";
    }

    /**
     * 获取某个用户的会话列表
     * @param userId
     * @return
     */
    public List<SysAiSession> getSessionList(Long userId) {
        return sessionService.lambdaQuery()
                .eq(SysAiSession::getUserId, userId)
                .orderByDesc(SysAiSession::getUpdateTime)
                .list();
    }

    /**
     * 获取某个会话的消息列表
     * @param sessionId
     * @return
     */
    public List<SysAiMessage> getMessageList(Long sessionId) {
        return messageService.lambdaQuery()
                .eq(SysAiMessage::getSessionId, sessionId)
                .orderByAsc(SysAiMessage::getId)
                .list();
    }
}