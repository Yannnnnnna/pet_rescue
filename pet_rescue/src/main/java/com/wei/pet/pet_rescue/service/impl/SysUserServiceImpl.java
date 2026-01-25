package com.wei.pet.pet_rescue.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wei.pet.pet_rescue.entity.SysUser;
import com.wei.pet.pet_rescue.entity.dto.*;
import com.wei.pet.pet_rescue.mapper.SysUserMapper;
import com.wei.pet.pet_rescue.service.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

        // 4. 直接登录
        StpUtil.login(user.getId());
        log.info("小程序登录成功：{}", user.getOpenid());
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
    public SysUser getMyInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = this.getById(userId);
        user.setPassword(null); // 抹除密码，防止泄露
        return user;
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

        // 1. 校验旧密码 (注意：实际项目中密码通常是 BCrypt 加密的，这里简单演示明文或简单加密)
        // 如果你的密码是加密存储的，请用 passwordEncoder.matches(dto.getOldPassword(), user.getPassword())
        if (!user.getPassword().equals(dto.getOldPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 2. 修改为新密码
        user.setPassword(dto.getNewPassword());
        // user.setPassword(passwordEncoder.encode(dto.getNewPassword())); // 建议加密

        return this.updateById(user);
    }
}
