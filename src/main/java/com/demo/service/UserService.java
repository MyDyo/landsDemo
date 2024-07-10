package com.demo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.entity.User;
import com.demo.entity.dto.UserRegisterDTO;
import com.demo.entity.vo.SafeUser;
import com.demo.util.Result;

import java.util.List;

public interface UserService extends IService<User> {

    /**
     * 用户登录相关方法
     * @param user 封装了用户的登录信息
     * @return 用户脱敏信息实体类
     */
    SafeUser checkLoginInfo(User user);


    /**
     * 返回用户列表分页对象
     * @param page 当前页数
     * @param pageSize 页面大小
     * @param userStyle 用户角色(可能的查询参数)
     * @param userName  用户姓名(可能的查询参数)
     * @return page对象 封装了分页信息和用户信息
     */
    Page<SafeUser> getUserListPage(int page, int pageSize, String userStyle, String userName);

    /**
     * 实现用户主动通过授权码进行注册
     * @param user 注册用户的信息
     * @return true or false
     */
   Boolean registerByCode(UserRegisterDTO user);
}
