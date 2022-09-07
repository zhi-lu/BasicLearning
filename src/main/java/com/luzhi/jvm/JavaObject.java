package com.luzhi.jvm;

import org.openjdk.jol.info.ClassLayout;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author zhilu
 * @version jdk1.8
 *
 * 对Java对象的结构进行深层剖析
 * <p></p>
 * 一个Java对象有三个部分{
 *     1: Header(对象头){
 *          i:  MarkWord markWord 存储对象运行时的数据.它的值类似于HashCode,保存锁的状态, GC分代年龄. 在64操作系统下占8个字节,在32位操作系统下占4位.
 *         ii:  Pointer           对象指针指向它的类元数据指针,用于判断当前的对象是那个类的实例,有无压缩,对指针压缩则指针占4字节,无压缩则指针占8个字节.
 *        iii:  Array_Len         数组长度,这部分只有数组对象才有.当前对象为数组对象的时候占4个字节.
 *     }
 *     2: Instance Date(实例数据)  存储对象的各个类型字段的信息.
 *     3: 对齐填充(Padding)        当前对象的字节数不够8的则通过对齐填充填充为8的倍数
 * }
 *
 * 问: 为什么通过对齐填充一定要填充为8的倍数呢?
 * 答：由于cpu对内存进行访问,一次寻址的指针大小是8个字节,正好是缓存行L1的大小.如果不进行对齐填充的话。可能会出现跨缓存行的情况。
 *    存在缓存行污染的情况.会影响程序执行的效率.所以为了提高效率我们使用(空间换时间的概念)来提高执行的效率.
 * 基础数据的填充位置{
 *       1: 如果在方法体内定义的,这时候就是在栈上分配的.
 *       2: 如果是类的成员变量,这时候就是在堆上分配的.
 *       3: 如果是类的静态成员变量,在方法区上分配的.
 * }
 */
public class JavaObject {
    public static void main(String[] args) throws InterruptedException {
        ClassLayout layout = ClassLayout.parseClass(ObjectParse.class);
        System.out.println(layout.toPrintable());
        run();
    }

    public static void run() throws InterruptedException {
        Object o = new Object();
        // Java空对象占16个字节.
        ClassLayout layout = ClassLayout.parseInstance(o);
        System.out.println(layout.toPrintable());
        System.out.println("<==============================>");
        Thread.sleep(10000L);
        Field[] fields = Unsafe.class.getDeclaredFields();
        System.out.println(ClassLayout.parseInstance(fields).toPrintable());
    }
}

class ObjectParse{
}