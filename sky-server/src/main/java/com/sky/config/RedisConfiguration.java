package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author yixin
 * @date 2025/6/1
 * @description redis配置类
 */
@Configuration
@Slf4j
public class RedisConfiguration {

    /**
     * 创建redis模板对象，并设置相关的属性
     * 加上Bean注解，会按照类型把redisConnectionFactory注入到方法中
     * @param redisConnectionFactory redis连接工厂对象
     * @return
     */
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        log.info("开始创建redis模板对象...");
        RedisTemplate redisTemplate = new RedisTemplate();
        //设置redis的连接工厂对象，该对象不用自己创建，因为starter依赖会创建好放到spring容器中
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置redis key的序列化器，自己创建Template对象主要目的就是为了设置键的序列化器，因为默认序列化器会造成乱码问题
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
