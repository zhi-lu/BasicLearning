package com.luzhi.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author zhilu
 * @version jdk1.8
 *
 * 创建线程的三种方法.
 * 对于Java线程的六种状态{
 *     1: NEW           创建好状态,但此时的线程没有被执行(没有被启动).
 *     2: RUNNABLE      运行状态,是Java虚拟机执行线程的状态(此时线程被执行).
 *     3: BLOCKED       阻塞状态,等待监视器锁的线程处于此状态(等待锁释放).
 *     4: WAITING       无限时期等待状态,等待另一个线程执行特定操作的线程处于此状态(操作).
 *     5: TIME_WAITING  等待另一个线程执行操作达指定等待时间的线程处于此状态(时间).
 *     6: TERMINATED    退出线程,退出的线程处于这样的状态.
 * }
 */
public class CreateThread {

    public static class CreateThreadByThreadClass extends Thread {
        @Override
        public void run() {
            System.out.println("1--线程名:" + super.getName());
        }
    }

    public static class CreateThreadByRunnable implements Runnable {
        @Override
        public void run() {
            System.out.println("2--线程名:" + Thread.currentThread().getName());
        }
    }

    public static class CreateThreadByCallable implements Callable<Integer> {
        @Override
        public Integer call() {
            System.out.println("3--线程名:" + Thread.currentThread().getName());
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println("输出当前的线程为:" + Thread.currentThread().getName());
        run();
        thread();
    }

    @SuppressWarnings({"AlibabaAvoidManuallyCreateThread"})
    public static void run() {
        Thread thread = new CreateThreadByThreadClass();
        thread.start();
        new Thread(new CreateThreadByRunnable(), "Thread-1").start();
        FutureTask<Integer> futureTask = new FutureTask<>(new CreateThreadByCallable());
        new Thread(futureTask, "Thread-2").start();
    }

    @SuppressWarnings("CallToThreadRun")
    public static void thread() {
        // run()就是一普通方法,是由当前调用线程去执行该方法(能重复执行)
        new StartAndRunThread().run();
        // start()是通过native start0()方法 JVM底层C/C++方法去创建一个新的线程去执行线程的run()方法.真正意义上的多线程执行(不能重复执行).
        new StartAndRunThread().start();
    }
}

class StartAndRunThread extends Thread {
    @Override
    public void run() {
        System.out.println("输出当前执行的线程是:" + getName());
    }
}



