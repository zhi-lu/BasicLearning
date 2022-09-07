package com.luzhi.proxy.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author zhilu
 * <p>
 * jdk动态代理实现{@link InvocationHandler}接口.用于激发动态代理类的方法.
 * 再通过{@link java.lang.reflect.Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)}创建动态代理的类.
 * 由于这个过程是在程序执行过程中动态生成与处理,所以叫做动态代理.
 * invoke方法具体调用处理程序的方法逻辑.
 * 如果代理类实现了接口并且代理类是jdk中的Proxy类创建的,则使用的是jdk代理的.
 */
public class ControllerJdkProxy implements InvocationHandler {

    /**
     * 被代理的对象.
     */
    private final Object object;

    public ControllerJdkProxy(Object object) {
        this.object = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 当与其关联的代理类对象在调用相关的事务/实例(其他)方法,将在调用的事务/实例(其他)方法上使用该调用程序的invoke方法.
        System.out.println("记录数据库更新操作开始启动");
        // 通过Method反射调用事务方法,并且如果方法正常完成,则返回值返回给invoke的调用者;如果该值具有原始类型,则首先将其适当地包装在一个对象中.
        Object res = method.invoke(object, args);
        System.out.println("记录数据库更新操作成功结束.");
        return res;
    }
}
