package com.demo.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author：Renjunde
 * @Project：landsDemo
 * @name：SafeUser
 * @Date：2024/7/5 10:52
 * @Description: 用户脱敏信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SafeUser {
    private int userId;
    private String username;
    private String userstyle;
}
