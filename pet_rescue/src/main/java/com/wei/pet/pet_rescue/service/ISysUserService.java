package com.wei.pet.pet_rescue.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wei.pet.pet_rescue.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wei.pet.pet_rescue.entity.dto.user.*;
import com.wei.pet.pet_rescue.entity.vo.AdminDashboardVO;
import com.wei.pet.pet_rescue.entity.vo.UserInfoVO;

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

    IPage<SysUser> getUserPage(UserQueryDTO query);

    boolean updateMyInfo(UserUpdateDTO dto);

    UserInfoVO getMyInfo();

    boolean updatePassword(UserPasswordDTO dto);

    String loginByPhone(String phone, String password);

    AdminDashboardVO getDashboardData();

    String loginBySMS(String phone, String code);
}
