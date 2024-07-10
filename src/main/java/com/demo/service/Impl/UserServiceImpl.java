package com.demo.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.entity.User;
import com.demo.entity.dto.UserRegisterDTO;
import com.demo.entity.vo.SafeUser;
import com.demo.mapper.UserMapper;
import com.demo.service.UserService;
import com.demo.util.Constants;
import com.demo.util.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.ParameterResolutionDelegate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;


    /**
     * 用户登录相关方法
     *
     * @param user 封装了用户的登录信息
     * @return 用户脱敏信息实体类
     */
    @Override
    public SafeUser checkLoginInfo(User user) {
        if (StringUtils.isAnyBlank(user.getUsername(), user.getPassword(), user.getUserstyle())) {
            return null;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername())
                .eq(User::getPassword, user.getPassword())
                .eq(User::getUserstyle, user.getUserstyle());

        return Optional.ofNullable(userMapper.selectOne(wrapper))
                .map(currentUser -> {
                    SafeUser safeUser = new SafeUser();
                    BeanUtils.copyProperties(currentUser, safeUser);
                    return safeUser;
                }).orElse(null);
    }

    @Override
    public Page<SafeUser> getUserListPage(int page, int pageSize, String userStyle, String userName) {
        Page<User> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(userStyle), User::getUserstyle, userStyle)
                .like(StringUtils.isNotBlank(userName), User::getUsername, userName);
        this.page(pageInfo, wrapper);
        List<SafeUser> safeUserList = pageInfo.getRecords().stream().map(user -> {
            SafeUser safeUser = new SafeUser();
            BeanUtils.copyProperties(user, safeUser);
            return safeUser;
        }).collect(Collectors.toList());
        Page<SafeUser> safeUserPage = new Page<>(pageInfo.getCurrent(), pageInfo.getSize(), pageInfo.getTotal());
        safeUserPage.setRecords(safeUserList);
        return safeUserPage;
    }

    @Override
    public Boolean registerByCode(UserRegisterDTO user) {
        if (user == null || user.getUser() == null || StringUtils.isBlank(user.getUser().getUserstyle()) || StringUtils.isBlank(user.getCode())) {
            return false;
        }
        //校验授权码与用户角色是否匹配
        String expectedCode = Constants.ROLE_CODE_MAP.get(user.getUser().getUserstyle());
        if (expectedCode == null || !expectedCode.equals(user.getCode())) {
            return false; // 授权码不匹配或角色未定义
        }
        //确保用户名唯一性
        User oldUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUser().getUsername()));
        if(oldUser != null) {
            return false;
        }
        // 执行注册流程
        User registerUser = new User();
        registerUser.setUsername(user.getUser().getUsername());
        registerUser.setPassword(user.getUser().getPassword());
        registerUser.setUserstyle(user.getUser().getUserstyle());
        int insert = userMapper.insert(registerUser);
        return insert > 0;
    }
}
