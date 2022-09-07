package com.luzhi.object;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhilu
 */
public class CopyObject {
    public static void main(String[] args) {
        referenceCopy();
        deepCopy();
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static void referenceCopy() {
        // 当通过引用复制,多个引用指向同一个对象.一个引用对对象进行操作,其他引用获取的是该对象被操作后的对象
        AtomicInteger one = new AtomicInteger(1);
        AtomicInteger two = one;
        one.getAndIncrement();
        System.out.println(one.hashCode());
        System.out.println(two.hashCode());
        System.out.println("输出:" + two.get());
    }

    @SuppressWarnings("StringEquality")
    public static void deepCopy() {
        Father father = new Father("费雷德.特朗普");
        Son son = new Son(76, "唐纳德.特朗普", father);
        try {
            Son clone = son.clone();
            System.out.println(son.father.equals(clone.father));
            System.out.println(son.name == clone.name);
            System.out.println(son.father.name == clone.father.name);
        } catch (CloneNotSupportedException exception) {
            exception.printStackTrace();
        }
        System.out.println("<=======================================================>");
        try {
            Son sonDeepCloneBySerializable = son.deepCloneUseSerializable();
            System.out.println(son.father.equals(sonDeepCloneBySerializable.father));
            System.out.println(son.name == sonDeepCloneBySerializable.name);
            System.out.println(son.father.name == sonDeepCloneBySerializable.father.name );
        }catch (ClassNotFoundException | IOException exception){
            exception.printStackTrace();
        }
    }

}

class Son implements Cloneable, Serializable {

    int age;

    String name;

    Father father;

    public Son(int age, String name, Father father) {
        this.age = age;
        this.name = name;
        this.father = father;
    }

    @SuppressWarnings("StringOperationCanBeSimplified")
    @Override
    protected Son clone() throws CloneNotSupportedException {
        Son clone = (Son) super.clone();
        clone.name = new String(name);
        clone.father = father.clone();
        return clone;
    }

    protected Son deepCloneUseSerializable() throws IOException, ClassNotFoundException {
        Son son;
        // 创建一个字节数组输出流缓冲区,将所有发送到输出流的数据保存到该字节数组输出流中.初始值未32个字节.会随着当前的需求进行变化.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // 将对象进行序列化输出
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(this);

        // 使用字节缓冲区数组,使用上一步将对象序列化后保存在缓冲区的缓冲对象son.
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        // 将对象进行反序列化输入
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        son = (Son) objectInputStream.readObject();
        return son;
    }
}

class Father implements Cloneable, Serializable {

    String name;

    public Father(String name) {
        this.name = name;
    }

    @Override
    protected Father clone() throws CloneNotSupportedException {
        return (Father) super.clone();
    }
}