package com.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 本地文件 配置信息
 */
@Data
@Component
@ConfigurationProperties(prefix = "local")
public class LocalFileProperties {

    /**
     * 是否开启
     */
    private boolean enable;

    /**
     * 默认磁盘根路径
     */
    private String basePath;

    /**
     * 默认文件URL前缀
     */
    private String baseUrl;

    public boolean isEnabled() {
        return this.enable;
    }
}

