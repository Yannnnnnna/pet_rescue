package com.wei.pet.pet_rescue.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wei.pet.pet_rescue.common.Result;
import com.wei.pet.pet_rescue.common.SmsService;
import com.wei.pet.pet_rescue.entity.PetAdoption;
import com.wei.pet.pet_rescue.entity.PetInfo;
import com.wei.pet.pet_rescue.entity.SysUser;
import com.wei.pet.pet_rescue.entity.dto.user.*;
import com.wei.pet.pet_rescue.entity.vo.AdminDashboardVO;
import com.wei.pet.pet_rescue.entity.vo.UserInfoVO;
import com.wei.pet.pet_rescue.mapper.PetAdoptionMapper;
import com.wei.pet.pet_rescue.mapper.PetInfoMapper;
import com.wei.pet.pet_rescue.mapper.SysUserMapper;
import com.wei.pet.pet_rescue.service.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author yanna
 * @since 2026-01-10
 */
@Slf4j
@Service

public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    // 注入 Mapper
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private PetInfoMapper petInfoMapper;
    @Resource
    private PetAdoptionMapper petAdoptionMapper;
    @Resource
    private SmsService smsService;

    // 定义常量
    @Value("${wechat.app-id}")
    private String wxAppId;

    @Value("${wechat.secret}")
    private String wxSecret;

    @Value("${wechat.auth-url}")
    private String wxAuthUrl;

    /**
     * 逻辑1：Web管理员登录
     */
    public String loginAdmin(AdminLoginDto dto) {
        // 1. 查用户
        SysUser user = lambdaQuery()
                .eq(SysUser::getUsername, dto.getUsername())
                .one();

        // 2. 校验账号密码
        if (user == null || !user.getPassword().equals(dto.getPassword())) {
            throw new RuntimeException("账号或密码错误");
        }

        // 3. 校验角色（防止普通用户跑来登后台）
        if (user.getRole() != 1) {
            throw new RuntimeException("您不是管理员，无权登录");
        }

        // 4. 登录
        StpUtil.login(user.getId());
        log.info("登录成功：{}", user.getUsername());
        return StpUtil.getTokenValue();
    }

    /**
     * 逻辑2：小程序用户登录 (核心)
     */
    public String loginByWechat(WechatLoginDto dto) {
        // 1. 拿 code 去微信服务器换 openid
        // 使用 Hutool 的 HttpUtil 工具发送 GET 请求
        String url = StrUtil.format(wxAuthUrl, wxAppId, wxSecret, dto.getCode());
        System.out.println("请求URL：" + url);
        String response = HttpUtil.get(url);

        System.out.println("微信返回结果" + response);

        // 解析微信返回的 JSON (建议用 JSONUtil 或 FastJson)
        JSONObject json = JSONUtil.parseObj(response);
        String openid = json.getStr("openid");

        if (StrUtil.isBlank(openid)) {
            throw new RuntimeException("微信登录失败，请稍后重试");
        }

        // 2. 查数据库：这个 openid 注册过吗？
        SysUser user = lambdaQuery().eq(SysUser::getOpenid, openid).one();

        // 3. 如果没注册，自动注册（新用户）
        if (user == null) {
            user = new SysUser();
            user.setOpenid(openid);
            user.setUsername("微信用户" + RandomUtil.randomString(6)); // 随机生成个账号
            user.setNickname("新用户");
            user.setRole(0); // 0代表普通用户
            user.setAvatar("默认头像URL");
            sysUserMapper.insert(user); // 插入数据库
        }

        StpUtil.login(user.getId());

        // 5. 每天登录奖励10积分
        if (!user.getLastLoginTime().toLocalDate().isEqual(LocalDateTime.now().toLocalDate())) {
            user.setCoin(user.getCoin() + 10);
            // 顺便更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            this.updateById(user);
        }
        log.info("小程序登录成功：{} 时间{}", user.getOpenid(), user.getLastLoginTime());
        return StpUtil.getTokenValue();
    }

    /**
     * 分页查询用户信息
     * @param query
     * @return
     */
    @Override
    public IPage<SysUser> getUserPage(UserQueryDTO query) {
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        // 1. 关键字搜索 (用户名/昵称/手机号)
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysUser::getUsername, query.getKeyword())
                    .or()
                    .like(SysUser::getNickname, query.getKeyword())
                    .or()
                    .like(SysUser::getPhone, query.getKeyword()));
        }

        // 2. 角色筛选
        wrapper.eq(query.getRole() != null, SysUser::getRole, query.getRole());

        // 4. 排序 (按创建时间倒序)
        wrapper.orderByDesc(SysUser::getCreateTime);

        // 5. 排除密码字段 (安全起见，不返回加密后的密码)
        wrapper.select(SysUser.class, info -> !info.getColumn().equals("password"));

        return this.page(page, wrapper);
    }

    /**
     * 用户和管理员修改个人信息
     * @param dto
     * @return
     */
    @Override
    public boolean updateMyInfo(UserUpdateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();

        SysUser user = new SysUser();
        user.setId(userId);
        user.setNickname(dto.getNickname());
        user.setAvatar(dto.getAvatar());
        user.setPhone(dto.getPhone());

        return this.updateById(user);
    }

    /**
     * 获取个人信息
     * @return
     */
    @Override
    public UserInfoVO getMyInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = this.getById(userId);
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        if (user.getPassword() == null){
            vo.setHasPassword(false);
        }else {
            vo.setHasPassword(true);
        }
        return vo;
    }

    /**
     * 修改密码
     * @param dto
     * @return
     */
    @Override
    public boolean updatePassword(UserPasswordDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = this.getById(userId);

        // 检查数据库中当前用户是否已设置过密码
        // 使用 Hutool 的 StrUtil.isNotBlank，或者 StringUtils.hasText
        boolean hasExistingPassword = user.getPassword() != null && !user.getPassword().isEmpty();

        if (hasExistingPassword) {
            // --- 场景A：已有密码（修改密码逻辑）---

            // 1. 必须传入旧密码
            if (dto.getOldPassword() == null || dto.getOldPassword().isEmpty()) {
                throw new RuntimeException("请输入旧密码以验证身份");
            }

            // 2. 校验旧密码是否正确
            // 如果是明文（不推荐）：
            if (!user.getPassword().equals(dto.getOldPassword())) {
                throw new RuntimeException("旧密码错误");
            }
            // 如果是加密（推荐）：
            // if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) { ... }
        } else {
            // --- 场景B：无密码（初始化密码逻辑）---
            // 这种情况下，直接跳过旧密码校验，允许用户直接设置新密码
            // 也可以在这里加一个日志，记录用户“初始化了密码”
            log.info("用户初始化密码");
        }

        // 3. 设置新密码
        user.setPassword(dto.getNewPassword());
        // user.setPassword(BCrypt.hashpw(dto.getNewPassword())); // 强烈建议加密存储

        return this.updateById(user);
    }

    /**
     * 通过手机号登录
     * @param phone
     * @param password
     * @return
     */
    @Override
    public String loginByPhone(String phone, String password) {
        // 1. 根据手机号查询用户
        // 使用 LambdaQueryWrapper，SysUser::getPhone 会自动映射为数据库的 phone 字段
        // 这里的 .one() 表示只查一条，如果数据库有逻辑删除配置，MyBatis-Plus 会自动过滤 is_deleted=1 的数据
        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone));

        // 2. 校验用户是否存在
        if (user == null) {
            throw new RuntimeException("账号不存在或已注销");
        }

        // 3. 校验密码
        // 注意：一定要判空，防止还没设置过密码的用户尝试登录
        if (user.getPassword() == null || !user.getPassword().equals(password)) {
            // 如果你是加密存储（如BCrypt），请用：
            // if (!BCrypt.checkpw(password, user.getPassword())) { ... }
            throw new RuntimeException("手机号或密码错误");
        }

        // 4. 校验通过，执行登录
        StpUtil.login(user.getId());

        // 5. 每天登录奖励10积分
        if (!user.getLastLoginTime().toLocalDate().isEqual(LocalDateTime.now().toLocalDate())) {
            user.setCoin(user.getCoin() + 10);
            // 顺便更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            this.updateById(user);
        }

        // 5. 返回 Token
        return StpUtil.getTokenValue();
    }
    /**
     * 获取管理员后台仪表盘数据
     * @return
     */
    @Override
    public AdminDashboardVO getDashboardData() {
        AdminDashboardVO vo = new AdminDashboardVO();

        // 1. 统计救助总数 (查询 pet_info 表总数)
        Long totalRescue = petInfoMapper.selectCount(null);
        vo.setTotalRescueCount(totalRescue);

        // 2. 统计已领养数 (查询 status = 2 的记录)
        Long adopted = petInfoMapper.selectCount(new LambdaQueryWrapper<PetInfo>()
                .eq(PetInfo::getStatus, 2)); // 假设 2 是已领养
        vo.setTotalAdoptionCount(adopted);

        // 3. 计算领养率 (避免除以0异常)
        if (totalRescue > 0) {
            // (adopted / total) * 100
            BigDecimal rate = new BigDecimal(adopted)
                    .divide(new BigDecimal(totalRescue), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            vo.setAdoptionRate(rate);
        } else {
            vo.setAdoptionRate(BigDecimal.ZERO);
        }

        // 4. 统计待审核申请 (pet_adoption 表 status = 0)
        Long pending = petAdoptionMapper.selectCount(new LambdaQueryWrapper<PetAdoption>()
                .eq(PetAdoption::getStatus, 0));
        vo.setPendingAuditCount(pending);

        // 5. 热门品种统计 (用于画饼图)
        // SQL 逻辑: SELECT breed as name, COUNT(*) as value FROM pet_info GROUP BY breed ORDER BY value DESC LIMIT 6
        QueryWrapper<PetInfo> breedWrapper = new QueryWrapper<>();
        breedWrapper.select("breed as name", "count(*) as value")
                .groupBy("breed")
                .orderByDesc("value")
                .last("LIMIT 6"); // 只取前6名

        List<Map<String, Object>> breedStats = petInfoMapper.selectMaps(breedWrapper);
        vo.setBreedDistribution(breedStats);

        // === 6. 生成近7天趋势图数据 (双折线) ===

        // 6.1 生成 X 轴 (共用的日期列表)
        List<String> dateList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            dateList.add(today.minusDays(i).toString());
        }
        vo.setDates(dateList);

        // 6.2 获取【救助趋势】数据 (查 pet_info 表)
        // 逻辑：统计 create_time
        QueryWrapper<PetInfo> rescueWrapper = new QueryWrapper<>();
        rescueWrapper.select("DATE_FORMAT(create_time, '%Y-%m-%d') as date", "count(*) as count")
                .ge("create_time", today.minusDays(6).atStartOfDay())
                .groupBy("date");

        // 调用通用方法
        List<Integer> rescueData = getDailyCounts(petInfoMapper, rescueWrapper, dateList);
        vo.setDailyRescueData(rescueData);


        // 6.3 获取【领养申请趋势】数据 (查 pet_adoption 表)
        // 逻辑：统计 create_time (代表新增的领养申请)
        QueryWrapper<PetAdoption> adoptionWrapper = new QueryWrapper<>();
        adoptionWrapper.select("DATE_FORMAT(create_time, '%Y-%m-%d') as date", "count(*) as count")
                .ge("create_time", today.minusDays(6).atStartOfDay())
                .groupBy("date");

        // 调用通用方法
        List<Integer> adoptionData = getDailyCounts(petAdoptionMapper, adoptionWrapper, dateList);
        vo.setDailyAdoptionData(adoptionData);

        return vo;
    }
    /**
     * 通过短信验证码登录
     * @param phone
     * @param code
     * @return
     */
    @Override
    public String loginBySMS(String phone, String code) {

        boolean isCodeValid = smsService.verifyCode(phone, code);

        // 2. 如果校验失败（由于防重放机制，如果输入错一次，Redis里的也没了，或者已经过了5分钟过期了）
        if (!isCodeValid) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 3. 校验成功！接下来就是纯纯的业务逻辑了：
        // (1) 去数据库查这个手机号存不存在？
         SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone));
        // (2) 如果不存在，说明是新用户，直接执行注册逻辑（自动 insert 一条记录到数据库）
         if (user == null) {
             user = new SysUser();
             user.setUsername(RandomUtil.randomString(6)); // 随机生成个账号
             user.setNickname("新用户");
             user.setRole(0); // 0代表普通用户
             user.setAvatar("默认头像URL");
             sysUserMapper.insert(user); // 插入数据库
         }
        //3. 生成该用户的登录状态 Token
            StpUtil.login(user.getId());
        // 4. 返回 Token 给前端
        return StpUtil.getTokenValue();
    }

    /**
     * 提取出来的私有通用方法：负责查库、转Map、补零
     * @param mapper 传入 Mapper (可能是 PetInfoMapper 或 PetAdoptionMapper)
     * @param wrapper 传入构造好的查询条件
     * @param dateList 传入完整的日期列表 (X轴)
     * @return 对齐后的 Y 轴数据列表
     */
    private <T> List<Integer> getDailyCounts(BaseMapper<T> mapper, QueryWrapper<T> wrapper, List<String> dateList) {
        // 1. 执行查询
        List<Map<String, Object>> queryResult = mapper.selectMaps(wrapper);

        // 2. 转为 Map<日期, 数量> 方便查找
        Map<String, Integer> dataMap = new HashMap<>();
        if (queryResult != null) {
            for (Map<String, Object> map : queryResult) {
                String dateKey = (String) map.get("date");
                Number countVal = (Number) map.get("count");
                dataMap.put(dateKey, countVal.intValue());
            }
        }

        // 3. 补零对齐
        List<Integer> resultList = new ArrayList<>();
        for (String date : dateList) {
            resultList.add(dataMap.getOrDefault(date, 0));
        }
        return resultList;
    }
}
