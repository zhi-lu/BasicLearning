package com.luzhi.service;


import com.luzhi.pojo.User;

/**
 * @author zhilu
 * @version jdk1.8
 *
 * jdk动态代理和cglib动态代理的区别
 * jdk动态代理:   被代理类需要实现相关的接口才能被JDK动态代理.
 * cglib动态代理: 主要是通过创建子类的方式,在父类的方法外进行操作。
 */
public class ControllerServiceImpl implements com.luzhi.service.interfaces.ControllerService {

    @Override
    public void getUser(User user) {
        System.out.format("用户对象%s,从数据库获取成功.\n", user);
    }

    @Override
    public void updateUser() {
        System.out.println("用户数据更新成功.");
    }
}
