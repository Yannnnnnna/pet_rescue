package com.wei.pet.pet_rescue.common;

import cn.dev33.satoken.stp.StpInterface;
import com.wei.pet.pet_rescue.entity.SysUser;
import com.wei.pet.pet_rescue.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wyr on 2026/2/9
 */
@Component // 必须加这个注解，Spring 才能扫描到
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private ISysUserService sysUserService; // 注入你的 Service

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> list = new ArrayList<>();

        // 1. 查数据库
        long userId = Long.parseLong(loginId.toString());
        SysUser user = sysUserService.getById(userId);

        // 2. 翻译角色 (0 -> user, 1 -> admin)
        if (user != null) {
            if (user.getRole() == 1) {
                list.add("admin"); // 对应 @SaCheckRole("admin")
            } else {
                list.add("user");
            }
        }
        return list;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>(); // 没用到权限码就返回空
    }
}
