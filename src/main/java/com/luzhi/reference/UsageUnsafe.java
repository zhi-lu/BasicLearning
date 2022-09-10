package com.luzhi.reference;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.stream.Stream;

/**
 * @author zhilu
 * @version jdk1.8
 *
 * 对Unsafe的一个基本使用: 用于执行底层, 不安全操作的方法的集合.
 * 尽管类和所有方法都是公共的,但此类的使用受到限制,因为只有受信任的代码才能获取它的实例.
 *
 * 我们一般通过{@link Unsafe#getUnsafe()}获取Unsafe实例,对于该方法的使用注意以下为:{
 *     为调用者提供执行不安全操作的能力。
 *     返回的Unsafe对象应由调用者小心保护, 因为它可用于在任意内存地址读取和写入数据, 它绝不能传递给不受信任的代码.
 *     此类中的大多数方法都是非常底层的, 并且对应于少量硬件指令(在典型机器上). 鼓励编译器相应地优化这些方法.
 * }
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
