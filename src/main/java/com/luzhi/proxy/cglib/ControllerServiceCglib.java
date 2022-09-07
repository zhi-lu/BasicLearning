package com.luzhi.proxy.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * @author zhilu
 *
 * cglib一个基于ASM的字节码生成库,它允许我们在运行时对字节码进行修改和动态生成.
 * cglib通过继承方式实现代理,在子类中采用方法拦截的技术拦截所有父类方法的调用并顺势织入横切逻辑。
 *
 * 代理类方法会被拦截器拦截,方法拦截器中会对目标类中所有的方法建立索引.
 * 其实大概就是将每个方法的引用保存在数组中,我们就可以根据数组的下标直接调用方法,而不是用反射.
 */
public class ControllerServiceCglib implements MethodInterceptor {

    /**
     * 需要被代理的对象
     */
    private final Object target;

    public ControllerServiceCglib(Object target) {
        this.target = target;
    }

    public Object getProxyInstance() {
        // 通过Enhancer去创建我们的动态代理对象.
        Enhancer enhancer = new Enhancer();
        // 设置一个父类
        enhancer.setSuperclass(target.getClass());
        // 设置回调函数,调用当前对象重写的intercept中断方法.
        enhancer.setCallback(this);
        // 生成代理对象
        return enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, @NotNull MethodProxy methodProxy) throws Throwable {
        // 调用我们地被代理类中的实例方法的时候,会进入我们拦截器的代理方法类似于jdk动态代理的具体的动态代理的调用处理程序.
        System.out.println("增强开始");
        // 直接方法调用而不是像jdk动态代理通过反射的方式调方法.
        Object res = methodProxy.invokeSuper(o, objects);
        System.out.println("增强结束");
        return res;
    }
}
