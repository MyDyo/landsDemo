package com.demo.util;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@EnableWebMvc
/**
 * <p>
 *  服务实现类
 * </p>
 *
 */
public class SwaggerConfig implements WebMvcConfigurer {
    /*
    swagger会帮助我们生成接口文档
    1：配置生成的文档信息
    2：配置生成规则
     */
    @Bean
    public Docket getDocket(){
        ApiInfoBuilder apiInfoBuilder = new ApiInfoBuilder();
        //指定封面信息
        apiInfoBuilder.title("土地规划系统后端接口说明")
                .description("此文档详细说明了液化系统后端接口规范")
                .version("v 2.0.1")
                .contact( new Contact("MyDyo","","273485717@qq.com"))
                .build();

        ApiInfo apiInfo = apiInfoBuilder.build();

        //指定生成策略
        Docket docket = new Docket(DocumentationType.SWAGGER_2)//指定文档路径
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.demo.controller"))
                .paths(PathSelectors.any())
                .build();

        return docket;
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations(
                "classpath:/static/");
        registry.addResourceHandler("swagger-ui.html").addResourceLocations(
                "classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations(
                "classpath:/META-INF/resources/webjars/");
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }

}