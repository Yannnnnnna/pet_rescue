package com.wei.pet.pet_rescue.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.wei.pet.pet_rescue.entity.PetAdoption;
import com.wei.pet.pet_rescue.entity.PetConsultation;
import com.wei.pet.pet_rescue.entity.PetInfo;
import com.wei.pet.pet_rescue.entity.SysUser;
import com.wei.pet.pet_rescue.entity.dto.ConsultationAskDTO;
import com.wei.pet.pet_rescue.entity.dto.ConsultationReplyDTO;
import com.wei.pet.pet_rescue.entity.dto.ReturnVisitDTO;
import com.wei.pet.pet_rescue.entity.vo.ChatSessionVO;
import com.wei.pet.pet_rescue.entity.vo.ConsultationSummaryVO;
import com.wei.pet.pet_rescue.entity.vo.ConsultationVO;
import com.wei.pet.pet_rescue.mapper.PetConsultationMapper;
import com.wei.pet.pet_rescue.service.IPetAdoptionService;
import com.wei.pet.pet_rescue.service.IPetConsultationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wei.pet.pet_rescue.service.IPetInfoService;
import com.wei.pet.pet_rescue.service.ISysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 领养咨询表 服务实现类
 * </p>
 *
 * @author yanna
 * @since 2026-01-21
 */
@Service
public class PetConsultationServiceImpl extends ServiceImpl<PetConsultationMapper, PetConsultation> implements IPetConsultationService {
    @Autowired
    private IPetInfoService petInfoService;
    @Autowired
    private ISysUserService userService;

    // 1. 发起提问
    public boolean ask(ConsultationAskDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        PetInfo pet = petInfoService.getById(dto.getPetId());
        if (pet == null) throw new RuntimeException("宠物不存在");

        // 禁止自问自答
        if (pet.getPublisherId().equals(currentUserId)) {
            throw new RuntimeException("不能向自己提问");
        }
        // =====================================================
        // 【新增】防重复提问校验
        // 逻辑：查询是否存在 (同宠物 + 同用户 + 同问题 + 待回复) 的记录
        // =====================================================
        Long count = this.lambdaQuery()
                .eq(PetConsultation::getPetId, dto.getPetId())
                .eq(PetConsultation::getAskUserId, currentUserId)
                .eq(PetConsultation::getQuestion, dto.getQuestion()) // 内容完全一致
                .eq(PetConsultation::getStatus, 0) // 且对方还没回
                .count();

        if (count > 0) {
            throw new RuntimeException("您已问过这个问题，请等待送养人回复后再试");
        }
        // =====================================================

        PetConsultation consultation = new PetConsultation();
        consultation.setPetId(dto.getPetId());
        consultation.setQuestion(dto.getQuestion());
        consultation.setAskUserId(currentUserId);
        consultation.setReplyUserId(pet.getPublisherId()); // 自动填入回答者ID
        consultation.setStatus(0); // 待回复
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            // 使用 String.join 或者 Hutool 的 CollUtil.join
            String imgStr = String.join(",", dto.getImages());
            consultation.setAskImgs(imgStr);
        }

        return this.save(consultation);
    }

    // 2. 回复提问
    public boolean reply(ConsultationReplyDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        PetConsultation consultation = this.getById(dto.getId());
        if (consultation == null) throw new RuntimeException("咨询记录不存在");

        // 权限校验：只有被提问的人（送养人）才能回复
        if (!consultation.getReplyUserId().equals(currentUserId)) {
            throw new RuntimeException("无权回复此问题");
        }

        consultation.setAnswer(dto.getAnswer());
        consultation.setStatus(1); // 已回复
        consultation.setReplyTime(LocalDateTime.now());
        // 【关键】把 List 转成 String (逗号分隔) 存入数据库
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            // 使用 String.join 或者 Hutool 的 CollUtil.join
            String imgStr = String.join(",", dto.getImages());
            consultation.setReplyImgs(imgStr);
        }
        return this.updateById(consultation);
    }

    /**
     * 我收到的提问记录
     * @return
     */
    public List<ChatSessionVO> getMyReceivedList() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<ChatSessionVO> list = baseMapper.selectMyReceivedSessions(userId);

        // (可选) 如果你想显示 "对方的昵称" 而不是 "宠物名"，可以在这里遍历处理
        // 因为 Mapper 里我是 JOIN 的 pet_info 拿的宠物头像和名字
        // 如果是“我收到的”，通常列表左边显示 提问者的头像 会更直观
        if (!list.isEmpty()) {
            Set<Long> userIds = list.stream().map(ChatSessionVO::getAskUserId).collect(Collectors.toSet());
            Map<Long, SysUser> userMap = userService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(SysUser::getId, u -> u));

            for (ChatSessionVO vo : list) {
                SysUser u = userMap.get(vo.getAskUserId());
                if (u != null) {
                    // 覆盖为提问者的信息
                    vo.setTitle(u.getNickname() + " 咨询了 " + vo.getTitle()); // 变成 "张三 咨询了 咪咪"
                    vo.setAvatar(u.getAvatar()); // 显示张三的头像
                }
                vo.setLastMessage("点击查看沟通详情..."); // 简化处理
            }
        }
        return list;
    }
    /**
     * 我发起的提问记录
     * @return
     */
    public List<ChatSessionVO> getMyAskedList() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<ChatSessionVO> list = baseMapper.selectMyAskedSessions(userId);
        // 这里显示宠物头像和名字没问题，不用改
        list.forEach(vo -> vo.setLastMessage("点击查看沟通详情..."));
        return list;
    }

    /**
     * 获取宠物和
     * @param petId
     * @param applicantId
     * @return
     */
    @Override
    public List<ConsultationVO> getPetConsultation(Long petId, Long applicantId) {
        // 1. 查询原始聊天记录
        List<PetConsultation> list = this.lambdaQuery()
                .eq(PetConsultation::getPetId, petId)
                .eq(PetConsultation::getAskUserId, applicantId)
                .orderByAsc(PetConsultation::getCreateTime)
                .list();

        if (list.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 准备关联数据 (为了性能，先查出来，不要在循环里查库)
        // 获取提问者信息
        SysUser askUser = userService.getById(applicantId);
        // 获取宠物信息
        PetInfo pet = petInfoService.getById(petId);
        // 获取回复者(发布人)信息 - 如果有需要的话，通常发布人是同一个人
         SysUser replyUser = userService.getById(pet.getPublisherId());

        // 3. 转换 Entity -> VO
        List<ConsultationVO> voList = new ArrayList<>();

        for (PetConsultation p : list) {
            ConsultationVO vo = new ConsultationVO();
            // 复制基本属性 (id, question, answer, status, createTime, replyTime)
            BeanUtils.copyProperties(p, vo);

            // --- A. 填充关联信息 ---
            if (pet != null) {
                vo.setPetId(pet.getId());
                vo.setPetName(pet.getName());
                vo.setPetCover(pet.getCoverImg());
            }
            if (askUser != null) {
                vo.setAskUserId(askUser.getId());
                vo.setAskUserNickname(askUser.getNickname());
                vo.setAskUserAvatar(askUser.getAvatar());
            }
            if (replyUser != null) {
                vo.setReplyUserNickname(replyUser.getNickname());
                vo.setReplyUserAvatar(replyUser.getAvatar());
            }

            // --- B. 处理图片 (String -> List) ---
            // 处理提问图片
            if (p.getAskImgs() != null && !p.getAskImgs().isEmpty()) {
                vo.setAskImgList(Arrays.asList(p.getAskImgs().split(",")));
            }
            // 处理回复图片
            if (p.getReplyImgs() != null && !p.getReplyImgs().isEmpty()) {
                vo.setReplyImgList(Arrays.asList(p.getReplyImgs().split(",")));
            }

            voList.add(vo);
        }

        return voList;
    }

    /**
     * 查询当前宠物的所有用户聊天记录
     * @param petId
     * @return
     */
    @Override
    public List<ConsultationSummaryVO> getPetConsultationSummary(Long petId) {
        return baseMapper.selectConsultationSummary(petId);
    }

    @Autowired
    private IPetAdoptionService petAdoptionService;

    public boolean startReturnVisit(ReturnVisitDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 1. 校验宠物信息
        PetInfo pet = petInfoService.getById(dto.getPetId());
        if (pet == null) throw new RuntimeException("宠物不存在");

        // 2. 权限校验：必须是宠物原本的“送养人”才能发起回访
        if (!pet.getPublisherId().equals(currentUserId)) {
            throw new RuntimeException("您不是该宠物的送养人，无法发起回访");
        }

        // 3. 【关键】查找该宠物的“成功领养人”作为接收消息的人
        // 逻辑：在领养申请表中，找到 status = 1 (已通过) 的记录
        PetAdoption adoption = petAdoptionService.lambdaQuery()
                .eq(PetAdoption::getPetId, dto.getPetId())
                .eq(PetAdoption::getStatus, 1) // 假设 1 代表审核通过
                .one();

        if (adoption == null) {
            throw new RuntimeException("该宠物暂无成功领养人，无法发起回访");
        }

        Long adopterUserId = adoption.getUserId(); // 获取领养人ID

        // 4. 构建咨询记录对象 (复用 PetConsultation 表)
        PetConsultation consultation = new PetConsultation();
        consultation.setPetId(dto.getPetId());
        consultation.setQuestion(dto.getQuestion()); // 回访的问题

        // 【身份互换】
        consultation.setAskUserId(currentUserId);    // 提问者 = 送养人
        consultation.setReplyUserId(adopterUserId);  // 回答者 = 领养人

        consultation.setStatus(0); // 待回复 (等待领养人回复)

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            consultation.setAskImgs(String.join(",", dto.getImages()));
        }

        return this.save(consultation);
    }
}
