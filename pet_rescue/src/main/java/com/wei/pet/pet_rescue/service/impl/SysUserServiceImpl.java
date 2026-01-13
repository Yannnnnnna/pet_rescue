package com.wei.pet.pet_rescue.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wei.pet.pet_rescue.entity.SysUser;
import com.wei.pet.pet_rescue.entity.dto.AdminLoginDto;
import com.wei.pet.pet_rescue.entity.dto.WechatLoginDto;
import com.wei.pet.pet_rescue.mapper.SysUserMapper;
import com.wei.pet.pet_rescue.service.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
}
