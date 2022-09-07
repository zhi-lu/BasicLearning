package com.luzhi.service.interfaces;

import com.luzhi.pojo.User;

/**
 * @author zhilu
 *
 * // 定义一个Service接口.动态代理
 */
public interface ControllerService {

    /**
     * 获取用户对象从数据库中(demo)
     *
     * @param user {@link User}的实例对象
     */
    void getUser(User user);

    /**
     * 更新用户对象
     */
    void updateUser();
}
