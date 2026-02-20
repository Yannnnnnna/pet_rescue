package com.wei.pet.pet_rescue.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 1. 定义 String 序列化器 (用于 Key)
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 2. 定义 JSON 序列化器 (用于 Value)
        // 🔥 核心修改开始：我们要自定义 ObjectMapper
        ObjectMapper mapper = new ObjectMapper();

        // (1) 注册 JavaTimeModule，解决 LocalDateTime 报错问题
        mapper.registerModule(new JavaTimeModule());

        // (2) 禁用“把日期写成时间戳”，这样存进去是 "2026-02-19 12:00:00" 而不是一串数字，方便看
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // (3) 激活默认类型信息 (非常重要！否则从 Redis 拿出来会变成 LinkedHashMap 报错)
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        // 将配置好的 mapper 塞给序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(mapper);
        // 🔥 核心修改结束

        // 3. 设置 Key 和 Value 的序列化方式
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
