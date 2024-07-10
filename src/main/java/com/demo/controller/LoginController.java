package com.demo.controller;

import com.demo.entity.User;
import com.demo.entity.dto.UserRegisterDTO;
import com.demo.entity.vo.SafeUser;
import com.demo.service.UserService;
import com.demo.util.Constants;
import com.demo.util.Result;
import com.demo.util.resultCode.R;
import com.fasterxml.jackson.databind.util.BeanUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author：Renjunde
 * @Project：landsDemo
 * @name：LoginController
 * @Date：2024/7/5 10:34
 * @Filename：LoginController
 * @Description: 系统登录注册接口实现
 */
@RestController
@CrossOrigin
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UserService userService;


    @PostMapping("/login")
    public R login(@RequestBody User user, HttpServletRequest request) {
        //返回的是脱敏后的用户信息
        SafeUser userInfo = userService.checkLoginInfo(user);
        if (userInfo != null) {
            //存session
            request.getSession().setAttribute(Constants.LOGIN_SESSION,userInfo);
            return R.ok().data(Constants.LOGIN_SESSION,userInfo);
        }
        return R.error().message("请检查个人信息是否正确");
    }


    @PostMapping("/register")
    public Result<Boolean> register(@RequestBody UserRegisterDTO user) {
        Boolean isSuccess =  userService.registerByCode(user);
        return isSuccess?Result.success(isSuccess):Result.error("注册失败");
    }
}
