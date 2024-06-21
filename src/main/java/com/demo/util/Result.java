package com.demo.util;

import com.alibaba.fastjson.JSONObject;

/**
 * 前端封装返回结果类
 * @author Rex Joush
 * @time 2022.01.03
 */
public class Result {

    /**
     * 请求成功的接口，仅返回提示信息
     * @param message 提示信息
     * @return 结果集合
     */
    public static String ok(String message){
        JSONObject object = new JSONObject();
        object.put("code", 20000);
        object.put("message", message);
        return object.toJSONString();
    }

    /**
     * 请求成功的接口，返回数据和提示信息
     * @param message 提示信息
     * @param data 携带的数据
     * @return 结果集合
     */
    public static String ok(String message, Object data){
        JSONObject object = new JSONObject();
        object.put("code", 20000);
        object.put("message", message);
        object.put("data", data);
        return object.toJSONString();
    }

    /**
     * 请求失败的接口，返回失败信息
     * @param message 失败信息
     * @return 结果集合
     */
    public static String error(String message) {
        JSONObject object = new JSONObject();
        object.put("code", 20001);
        object.put("message", message);
        return object.toJSONString();
    }

    /**
     * 请求失败的接口，返回失败信息
     * @param message 失败信息
     * @return 结果集合
     */
    public static String error(String message, Object data) {
        JSONObject object = new JSONObject();
        object.put("code", 20001);
        object.put("message", message);
        object.put("data", data);
        return object.toJSONString();
    }

    /**
     * 请求成功的接口，返回自定义代码和信息
     * @param code 自定义返回码
     * @param message 提示信息
     * @return 结果集合
     */
    public static String info(int code, String message) {
        JSONObject object = new JSONObject();
        object.put("code", code);
        object.put("message", message);
        return object.toJSONString();
    }

}
