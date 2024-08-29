package com.demo;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@MapperScan("com.demo.mapper")
@EnableWebMvc
@EnableSwagger2
@Slf4j
@SpringBootApplication
//@ComponentScan(basePackages = {"com.nwu"})
public class ResearchProjectCollectionSpringBootApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(ResearchProjectCollectionSpringBootApplication.class, args);
        log.info("项目已启动...");
    }

}
