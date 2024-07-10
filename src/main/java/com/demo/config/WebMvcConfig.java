package com.demo.config;

import com.demo.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
/**
 * WebMvc 配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private FileProperties fileProperties;
    static final String ORIGINS[] = new String[]{"GET", "POST", "PUT", "DELETE","OPTIONS"};
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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")// /** 表示拦截所有请求,静态资源也会被拦截
                .excludePathPatterns("/api/login")//登录请求
                .excludePathPatterns("/api/register");//注册请求
    }

    private CorsConfiguration corsConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOriginPattern("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);
        return corsConfiguration;
    }
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig());
        return new CorsFilter(source);
    }


}
