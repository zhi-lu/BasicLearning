package com.luzhi.controller;

import com.luzhi.annotation.AutoWired;
import com.luzhi.service.UserService;

/**
 * @author zhilu
 * @version jdk1.8
 *
 * // 用户对象控制器
 */
public class UserController {

    @AutoWired
    UserService userService;

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
