package com.luzhi.reference;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.stream.Stream;

/**
 * @author zhilu
 * @version jdk1.8
 *
 * 对Unsafe的一个基本使用
 */
public class UsageUnsafe {

    int number = 0;
    private static final UsageUnsafe USAGE_UNSAFE = new UsageUnsafe();

    public static void main(String[] args) throws IllegalAccessException {
        // 通过函数获取单例对象Unsafe
        // Unsafe unsafe = Unsafe.getUnsafe()
        // 通过反射的方式获取Unsafe对象
        Field field = Unsafe.class.getDeclaredFields()[0];
        Stream.of(Unsafe.class.getDeclaredFields()).forEach(field1 -> System.out.println(field1.getName()));
        field.setAccessible(true);
        Unsafe unsafe = (Unsafe) field.get(null);

        Field declaredField = UsageUnsafe.class.getDeclaredFields()[0];
        declaredField.setAccessible(true);
        // 获取对象的属性在内存的偏移量,相当于在可以通过这个偏移量找到对象属性在内存的位置.
        long offset = unsafe.objectFieldOffset(declaredField);
        System.out.println(offset);
        // 底层使用JVM中C++的方法去加锁 lock cmpxchg 锁.{关中断,缓存锁 存在对象大于缓存,锁总线 拉高北桥电平信号}
        boolean success = unsafe.compareAndSwapInt(USAGE_UNSAFE, offset, 1, 1);
        System.out.println(success);
        System.out.println(USAGE_UNSAFE.number);
    }
}
