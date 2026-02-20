package com.wei.pet.pet_rescue.common;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.teaopenapi.models.Config;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SmsService {

    @Value("${aliyun.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name}")
    private String signName;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String SMS_LIMIT_PREFIX = "sms:limit:";

    /**
     * 发送验证码 (支持多场景)
     * @param phone 手机号
     * @param type  业务类型: 1-登录/注册, 2-修改绑定手机, 3-重置密码, 4-绑定新手机
     */
    public void sendCode(String phone, Integer type) throws Exception {
        // 1. 【Redis 防刷限流】检查 60 秒内是否已经发过
        if (Boolean.TRUE.equals(redisTemplate.hasKey(SMS_LIMIT_PREFIX + phone))) {
            throw new RuntimeException("操作太频繁，请60秒后再试");
        }

        // 2. 根据业务类型选择阿里云赠送的模板 CODE
        String templateCode;
        switch (type) {
            case 1: templateCode = "100001"; break; // 登录/注册
            case 2: templateCode = "100002"; break; // 修改绑定手机号
            case 3: templateCode = "100003"; break; // 重置密码
            case 4: templateCode = "100004"; break; // 绑定新手机号
            default: throw new RuntimeException("未知的短信业务类型");
        }

        // 3. 依然自己生成 6 位随机验证码，保留架构亮点
        String code = String.format("%06d", new Random().nextInt(999999));

        // 4. 调用阿里云最新 API
        boolean isSuccess = sendDypnsSms(phone, code, templateCode);

        if (isSuccess) {
            // 5. 存入 Redis：验证码有效期 5 分钟，防刷限制 60 秒
            redisTemplate.opsForValue().set(SMS_CODE_PREFIX + phone, code, 5, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(SMS_LIMIT_PREFIX + phone, "1", 60, TimeUnit.SECONDS);
        } else {
            throw new RuntimeException("短信发送失败，请检查阿里云配置");
        }
    }

    /**
     * 校验验证码 (供后续业务接口调用)
     */
    public boolean verifyCode(String phone, String inputCode) {
        if (!StringUtils.hasText(inputCode)) return false;
        String realCode = (String) redisTemplate.opsForValue().get(SMS_CODE_PREFIX + phone);
        if (inputCode.equals(realCode)) {
            redisTemplate.delete(SMS_CODE_PREFIX + phone); // 校验成功即销毁
            return true;
        }
        return false;
    }

    /**
     * 封装阿里云号码认证服务 (Dypnsapi) 的底层请求
     */
    private boolean sendDypnsSms(String phone, String code, String templateCode) {
        try {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret);
            // 注意：这里的 Endpoint 和之前不同了
            config.endpoint = "dypnsapi.aliyuncs.com";
            Client client = new Client(config);

            // 根据你查阅的文档，组装 SendSmsVerifyCode 请求
            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setSignName(signName)
                    .setTemplateCode(templateCode);

            // 【关键点】按照文档要求，传入我们自己生成的 code 和有效期 min
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("code", code);
            paramMap.put("min", "5");
            request.setTemplateParam(new Gson().toJson(paramMap));

            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCode(request);

            // "OK" 代表阿里云成功推给运营商
            if ("OK".equals(response.getBody().getCode())) {
                log.info("短信发送成功！手机号：{}，场景模板：{}，验证码：{}", phone, templateCode, code);
                return true;
            } else {
                log.error("阿里云返回错误：{}", response.getBody().getMessage());
                return false;
            }
        } catch (Exception e) {
            log.error("短信接口调用异常：", e);
            return false;
        }
    }
}
