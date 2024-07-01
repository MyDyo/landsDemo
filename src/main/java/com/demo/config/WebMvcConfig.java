package com.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc 配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private FileProperties fileProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源映射到classpath:/static/
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");

        // Swagger UI 相关资源映射
//        registry.addResourceHandler("/swagger-ui.html")
//                .addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("/webjars/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/");
//        registry.addResourceHandler("/doc.html")
//                .addResourceLocations("classpath:/META-INF/resources/");

        // 本地文件上传路径
        if (fileProperties.getLocal() != null && fileProperties.getLocal().isEnabled()) {
            String basePath = fileProperties.getLocal().getBasePath();
            // 将basePath转换为Spring Resource Handler可以理解的格式
            String resourcePattern = "file:" + basePath.replace("\\", "\\\\") + "/";
            registry.addResourceHandler("/profile/**")
                    .addResourceLocations(resourcePattern);
        }
    }
}
