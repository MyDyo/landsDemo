package com.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
@MapperScan("com.demo.mapper")
//@ComponentScan(basePackages = {"com.nwu"})
public class ResearchProjectCollectionSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResearchProjectCollectionSpringBootApplication.class, args);
    }

}
