package com.luzhi.concurent;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhilu
 * <p>
 * 通过{@link Thread#join()}来保证线程依次执行的顺序.该方法通过{@link Thread#isAlive()}这个本地方法
 * 判断当前执行的线程是否存活.如果{@code #join()}线程存活,则主线程和主线程中的其他子线程一直处于等待状态{@link Object#wait(long)}
 * 直到我们的{@code #join()}线程终止后.则调用{@link Object#notifyAll()}将所有等待线程唤醒.
 * 退出循环恢复主线程执行,很显然这种循环检查的方式比较低效.
 *
 * 此外, 使用{@link Thread#join()}缺少灵活性,往往实际项目中很少会手动创建线程,通常使用线程池,使用{@link java.util.concurrent.Executors}
 * or {@link java.util.concurrent.ThreadPoolExecutor} 减少了{@code #join()}的使用. 大多数时候我们{@code #join()}
 * 方法仅仅停在demo上.
 */
public class JoinMethodOnlyDemo {

    public static void main(String[] args) throws InterruptedException {
        Thread threadOne = new ThreadOne();
        Thread threadTwo = new ThreadTwo();
        threadOne.start();
        threadOne.join();
        threadTwo.start();
        threadTwo.join();
    }

    @Slf4j
    static class ThreadOne extends Thread {
        public ThreadOne() {
            super("ThreadOne");
        }

        @Override
        public void run() {
            try {
                log.info("{} --线程开始在 {}毫秒, 执行任务.", this.getName(), System.currentTimeMillis());
                sleep(1000L);
                log.info("{} --线程结束在 {}毫秒, 结束任务.", this.getName(), System.currentTimeMillis());
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }

    @Slf4j
    static class ThreadTwo extends Thread {
        public ThreadTwo() {
            super("ThreadTwo");
        }

        @Override
        public void run() {
            try {
                log.info("{} --线程开始在 {}毫秒, 执行任务.", this.getName(), System.currentTimeMillis());
                sleep(1000L);
                log.info("{} --线程结束在 {}毫秒, 结束任务.", this.getName(), System.currentTimeMillis());
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }
}
