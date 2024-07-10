package com.demo.entity.dto;

import com.demo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author：Renjunde
 * @name：UserRegisterDTO
 * @Date：2024/7/10 11:18
 * @Description: 用户注册信息实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterDTO {
    private User user;
    //授权码
    private String code;
}
