package com.wei.pet.pet_rescue.service;

import com.wei.pet.pet_rescue.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.pet.pet_rescue.entity.dto.AdminLoginDto;
import com.wei.pet.pet_rescue.entity.dto.WechatLoginDto;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author yanna
 * @since 2026-01-10
 */
public interface ISysUserService extends IService<SysUser> {
    String loginAdmin(AdminLoginDto dto);
    String loginByWechat(WechatLoginDto dto);
}
