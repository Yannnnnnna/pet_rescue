package com.wei.pet.pet_rescue.common;

/**
 * @author wyr on 2026/2/5
 */
import com.alibaba.dashscope.utils.Constants;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "ai.qwen")
@Data
public class AiConfig {
    private String apiKey;

    @PostConstruct
    public void init() {
        // 阿里SDK通常通过全局变量设置Key
        Constants.apiKey = this.apiKey;
    }
}
