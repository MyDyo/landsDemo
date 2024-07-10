package com.demo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.entity.vo.SafeUser;
import com.demo.service.UserService;
import com.demo.util.Result;
import com.demo.util.resultCode.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author：Renjunde
 * @name：UserController
 * @Date：2024/7/8 11:27
 * @Description: 封装了对用户一系列操作的接口
 */
@RestController
@CrossOrigin
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<Page<SafeUser>> page(int page, int pageSize, String userStyle, String userName) {
        Page<SafeUser> pageResult = userService.getUserListPage(page,pageSize,userStyle,userName);
        return Result.success(pageResult);
    }
}
