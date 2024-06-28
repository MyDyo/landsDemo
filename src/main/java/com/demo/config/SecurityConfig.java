//package com.demo.config;
//
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.builders.WebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.web.firewall.HttpFirewall;
//
//@EnableWebSecurity
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//    private final HttpFirewall customFirewall;
//
//    public SecurityConfig(HttpFirewall customFirewall) {
//        this.customFirewall = customFirewall;
//    }
//
//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.httpFirewall(customFirewall);
//    }
//}
