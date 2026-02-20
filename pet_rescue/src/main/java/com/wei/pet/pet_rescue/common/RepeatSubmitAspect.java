package com.wei.pet.pet_rescue.common;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest; // SpringBoot 2.x 请换成 javax.servlet...
import java.util.concurrent.TimeUnit;

/**
 * 防重复提交切面处理 (强制登录版)
 * @author wyr
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RepeatSubmitAspect {

    private final StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(noRepeatSubmit)")
    public Object around(ProceedingJoinPoint joinPoint, NoRepeatSubmit noRepeatSubmit) throws Throwable {

        // 1. 获取请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();
        String uri = request.getRequestURI();


        // 如果未登录，这里会直接抛出 NotLoginException，流程瞬间终止！不会往下走了！
        StpUtil.checkLogin();

        // 既然代码能走到这里，说明百分之百已经登录了，放心大胆地获取用户 ID
        Long userId = StpUtil.getLoginIdAsLong();

        // 2. 组装只属于该用户的精准 Redis 锁 Key
        String lockKey = "lock:submit:" + uri + ":" + userId;

        // 3. 抢锁防抖逻辑
        long lockTime = noRepeatSubmit.lockTime();
        Boolean isSuccess = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", lockTime, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(isSuccess)) {
            log.warn("拦截到用户[{}]重复提交请求: {}", userId, uri);
            // 抛出异常交给全局异常处理器返回给前端
            throw new RuntimeException("手速太快啦，请勿重复点击提交！");
        }

        // 4. 放行真实业务
        return joinPoint.proceed();
    }
}