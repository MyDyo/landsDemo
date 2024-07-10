package com.demo.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author：Renjunde
 * @name：Constants
 * @Date：2024/7/10 11:22
 * @Description: 常量工具类
 */
public class Constants {

    /*授权码常量*/

    //村级管理员授权码
    public static final String VILLAGE_ADMIN_CODE = "aisjekryc";
    //普通用户授权码
    public static final String USER_CODE = "kcsyfbepx";

    /*Session常量*/
    public static final String LOGIN_SESSION = "userInfo";


    /*用户角色常量*/
    public static final String VILLAGE_ROLE = "村级管理员";
    public static final String USER_ROLE = "用户";

    /*角色与授权码的映射关系*/
    public static final Map<String,String> ROLE_CODE_MAP = new HashMap<>();
    static {
        ROLE_CODE_MAP.put(VILLAGE_ROLE,VILLAGE_ADMIN_CODE);
        ROLE_CODE_MAP.put(USER_ROLE,USER_CODE);
    }

}
