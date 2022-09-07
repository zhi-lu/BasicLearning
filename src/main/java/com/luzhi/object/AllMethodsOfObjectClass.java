package com.luzhi.object;


/**
 * @author zhilu
 *
 * 对于本地机方法registerNatives()的作用.
 * 提出问题为什么要有本地机方法(JVM底层的C/C++方法)registerNattives{
 *     因为我们Java想要使用本地机的方法比如一些动态链接库的(win lib, mac liunx dylib)方法.
 *     需要通过两种方式[
 *          1: 通过{@link System#loadLibrary(String)} 通过链接库地址,将链接库加载到内存中进行使用.
 *          2: 通过虚拟机去定位并链接我们本地的动态链接库,从而使用我们链接库的方法
 *     ]
 * }
 * 而regisetrNatives方法是取代上述的第二步操作,让我们Java程序主动链接调用方,直接调用动态链接库的方法.不需要通过虚拟机去定位链接我们的动态链接库。
 */
public class AllMethodsOfObjectClass {

    public static void main(String[] args) {
        getClassMethod();
        hashCodeMethod();
        equalMethod();
        cloneMethod();
        toStringMethod();
        waitMethod();
        notifyMethod();
        finalizeMethod();
    }

    public static void getClassMethod() {
        Number numberZero = 0;
        Number numberOne = 1;
        Class<? extends Number> numberClass = numberZero.getClass();
        Class<? extends Number> numberOneClass = numberOne.getClass();
        // 对于java来说,对于同一个类的不同实例化对象.他们的类对象始终是同一个.并且类对象获取他的类对象只允许进行一次
        // 也就是说获取对象的类对象只能进行连续两次,后面再获取的类对象和第二次类对象是同一个对象/
        System.out.println(numberClass.hashCode() == numberOneClass.hashCode());
    }

    public static void hashCodeMethod(){
        // 'A' 的Ascii值为65, 'a' 的Ascii值为97
        String one = "Aa";
        String two = "BB";
        // "Aa" 和 "BB" 的Ascii码相同.所以对象的hashcode相同不能判断他们是同一个对象
        // 判断两个对象是不是同一个对象 {第一步:判断hashcode是否相同 -> {相同 判断值是否相同 -> 相同则两个对象为同一个对象
        // <-> 不相同则则两个对象不是同一个对象} <-> hashcode不相同则两个对象一定不相同}
        // 但是我们最好要减少hashcode码相同而对象不相同的情况.因为过多的hash冲突,会降低我们hash表的性能.
        System.out.println("Aa的hashcode值为:" + one.hashCode());
        System.out.println("BB的hashcode值为:" + two.hashCode());
    }

    @SuppressWarnings("StringOperationCanBeSimplified")
    public static void equalMethod(){
        // 对于equal()有四个性质 自反性, 对称性, 传递性, 一致性.
        // 当重写该方法时,通常都需要重写hashCode方法.以维护hashCode()的一般约定,即相等的对象必须具有相等的哈希码。
        String one = "abc";
        String two = new String("abc");
        System.out.println(one.hashCode() == two.hashCode());
        System.out.println(one.equals(two));
    }

    public static void cloneMethod(){
        People people = new People(21, "luzhi", new Object());
        System.out.format("原生对象的年龄:%d, 姓名:%s\n", people.age, people.name);
        try {
            People clone = (People) people.clone();
            System.out.format("克隆对象的年龄:%d, 姓名:%s\n", clone.age, clone.name);
            System.out.println("原生对象和克隆对象是不是同一个对象" + people.equals(clone));
            System.out.println("未改变name值,people和clone是否相同:" + people.name.equals(clone.name));
            // 重新让引用指向不同地址的值
            people.name = "zhilu";
            System.out.println("改变克隆的name值,people和clone是否相同:" + people.name.equals(clone.name));
            System.out.println("people和clone里的Object对象是不是同一个对象:" + people.object.equals(clone.object));
        }catch (CloneNotSupportedException exception){
            exception.printStackTrace();
        }

    }

    public static void toStringMethod(){
        // toString()以文本的形式返回对象的信息,直接打印对象.默认调用该方法.如果不对对象进行重新则默认打印对象的类名和16进制的对象hashcode值
        // 结果应该是一个简洁并且信息丰富地表示,易于人们阅读.建议所有子类重写此方法。
        AllMethodsOfObjectClass allMethodsOfObjectClass = new AllMethodsOfObjectClass();
        System.out.println(allMethodsOfObjectClass);
    }

    public static void waitMethod(){
    }

    public static void notifyMethod(){
    }

    public static void finalizeMethod(){
        // 很多时候除了特殊要求对finalize进行重写,一般不要对finalize进行重写.避免给对象重新加上强引用从而导致内存泄露.
    }

    @Override
    public String toString() {
        return "当前对象的类名称," + this.getClass().getName() + ",该对象十六进制hashcode的值:" + Integer.toHexString(this.hashCode());
    }
}

class People implements Cloneable {
    int age;
    String name;

    Object object;

    public People(int age, String name, Object object){
        this.age = age;
        this.name = name;
        this.object = object;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

