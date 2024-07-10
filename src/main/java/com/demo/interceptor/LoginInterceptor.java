package com.demo.interceptor;

import com.demo.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @Author：Renjunde
 * @Project：landsDemo
 * @Date：2024/7/5 17:57
 * @Filename：LoginInterceptor
 * @Description: 主要完成对未登录用户进行接口请求的拦截功能
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    /**
     * 目标方法执行之前
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        log.info("拦截的请求路径是{}", requestURI);
        //登录检查逻辑
        HttpSession session = request.getSession();
        //判断session中是否存在loginUser，登录成功的用户会在session中加入loginUser对象
        Object loginUser = session.getAttribute(Constants.LOGIN_SESSION);
        if (loginUser != null) {
            return true;
        }
        response.setStatus(HttpStatus.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        return false;
    }

    /**
     * 目标方法执行之后
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("postHandle执行{}", modelAndView);
    }

    /**
     * 目标方法页面渲染之后
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("afterCompletion执行异常{}", ex);
    }
}
