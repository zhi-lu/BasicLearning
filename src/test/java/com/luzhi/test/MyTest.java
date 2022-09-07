package com.luzhi.test;

import com.luzhi.controller.UserController;
import com.luzhi.pojo.User;
import com.luzhi.proxy.cglib.ControllerServiceCglib;
import com.luzhi.proxy.jdk.ControllerJdkProxy;
import com.luzhi.service.ControllerServiceImpl;
import com.luzhi.service.UserService;
import com.luzhi.service.interfaces.ControllerService;
import org.junit.Assert;
import org.junit.Test;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;
import java.util.WeakHashMap;


public class MyTest {

    @Test
    public void test() throws Exception {
        UserController userController = new UserController();
        // 通过实例对象的getClass()方法获取当前对象的类对象。
        Class<? extends UserController> clazz = userController.getClass();
        // 创建对象
        UserService userService = new UserService();
        System.out.println("输出对象: " + userService);
        // 获取所有声明的属性值
        Field declaredField = clazz.getDeclaredField("userService");
        // 将布尔设置为true则是再反射对象使用时禁止java语言进行访问检查(不检查语句的访问权限)
        declaredField.setAccessible(true);
        // 通过方法获取具体的属性值
        String name = declaredField.getName();
        name = name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
        // 拼接方法名
        String setMethodName = "set" + name;
        // 通过方法注入对象
        Method method = clazz.getMethod(setMethodName, UserService.class);
        // 反射
        method.invoke(userController, userService);
        System.out.println("输出对象: " + userController.getUserService());
    }

    @Test
    public void jdkProxy() {
        User user = new User();
        ControllerServiceImpl controllerServiceImpl = new ControllerServiceImpl();
        ControllerJdkProxy controllerJdkProxy = new ControllerJdkProxy(controllerServiceImpl);
        ControllerService controllerService = (ControllerService) Proxy.newProxyInstance(controllerServiceImpl.getClass().getClassLoader(),
                controllerServiceImpl.getClass().getInterfaces(), controllerJdkProxy);

        controllerService.getUser(user);
        controllerService.updateUser();
    }

    @Test
    public void cglibProxy() {
        ControllerServiceCglib controllerServiceCglib = new ControllerServiceCglib(new ControllerServiceImpl());
        ControllerService controllerService = (ControllerService) controllerServiceCglib.getProxyInstance();
        controllerService.getUser(new User());
        controllerService.updateUser();
    }

    @Test
    public void strongReference() {
        Object reference = new Object();
        Assert.assertNotNull(reference);
        // 强引用,如果一个对象具有强引用。则JVM让对象再虚拟机当中尽可能的保存(对象不被其他所引用时).
        // 哪怕内存不足时抛出OutOfMemory也不会随便回收对象
        Object strongReference = reference;
        Assert.assertSame(reference, strongReference);
        reference = null;
        System.gc();
        // gc 后strongReference指向的对象不会被回收
        Assert.assertNotNull(strongReference);
    }

    @Test
    public void softReference() {
        Object reference = new Object();
        SoftReference<Object> softReference = new SoftReference<>(reference);
        Assert.assertSame(reference, softReference.get());
        reference = null;
        System.gc();
        // 只有触发Jvm OutOfMemory内存不足时,才会被回收.适合用于缓存.
        Assert.assertNotNull(softReference.get());
    }

    @Test
    public void weakReference() {
        Object reference = new Object();
        // 只具有弱引用的对象拥有更短暂的生命周期。
        // 在垃圾回收器线程扫描它所管辖的内存区域的过程中，一旦发现了只具有弱引用的对象，
        // 不管当前内存空间足够与否，都会回收它的内存。不过，由于垃圾回收器是一个优先级很低的线程，
        // 因此不一定会很快发现那些只具有弱引用的对象。
        WeakReference<Object> weakReference = new WeakReference<>(reference);
        Assert.assertSame(reference, weakReference.get());
        reference = null;
        System.gc();
        // 没有被强引用的reference,weak reference引用的对象在gc后回收.
        Assert.assertNull(weakReference.get());
    }

    @Test
    public void weakHashMap() throws InterruptedException {
        WeakHashMap<Object, Object> weakHashMap = new WeakHashMap<>();
        Object key = new Object();
        Object value = new Object();
        weakHashMap.put(key, value);
        // 目前在map存在实体
        Assert.assertTrue(weakHashMap.containsValue(value));

        key = null;
        System.gc();

        Thread.sleep(1000);
        // 一旦没有指向 key 的强引用, WeakHashMap 在 GC 后将自动删除相关的 entry
        Assert.assertFalse(weakHashMap.containsValue(value));
    }

    @Test
    public void phantomReference() {
        // 虚引用用来跟踪对象被垃圾回收的活动。
        Object reference = new Object();
        PhantomReference<Object> phantomReference = new PhantomReference<>(reference, new ReferenceQueue<>());
        // phantom reference 的 get 方法永远返回 null
        Assert.assertNull(phantomReference.get());
    }
}
