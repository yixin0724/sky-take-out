package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yixin
 * @date 2025/5/30
 * @description 配置类，用于创建AliOssUtil工具类的对象
 */
@Configuration
@Slf4j
public class OssConfiguration {
    //读取从配置文件中的阿里云OSS相关配置信息

    /**
     * 创建阿里云文件上传工具类对象
     * 通过参数注入的方式直接把AliOssProperties对象注入到该方法中
     * 因为AliOssProperties类加过@Component注解，属于Spring容器中的bean，所以可以直接注入到该方法中
     * 最后别忘了加上@Bean注解，这样项目启动时，这个方法就会执行，返回一个AliOssUtil对象，并交给Spring容器管理
     * 因为是公共工具类，只需要一个所以加上@ConditionalOnMissingBean注解，用于判断当前容器中是否已经存在AliOssUtil对象，如果存在则不创建，不存在则创建
     * @param aliOssProperties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties) {
        log.info("开始创建阿里云文件上传工具类对象：{}", aliOssProperties);
        return new AliOssUtil(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName()
        );
    }
}
