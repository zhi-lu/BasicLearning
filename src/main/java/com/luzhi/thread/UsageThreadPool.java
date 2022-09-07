package com.luzhi.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zhilu
 * @version jdk1.8
 * <p>线程池</p>
 * 线程池通过AtomicInteger ctl来控制主线程池的状态. ctl包装了两个参数 workCount 是32位的低29位.表示当前有效线程数
 * runStat 是32位的高3位,表示当前线程池的运行状态(为什么要用高3位来表示线程池运行的状态?)
 * 答案: 因为我线程池的状态有五种{
 *    1: RUNNING   当前线程池的状态是现在可以接受新的任务,并能处理在工作队列(阻塞队列)的任务.
 *    2: SHUTDOWN  当前线程池的状态是不接受新的任务,但任然处理在工作队列里的任务
 *    3: STOP      当前线程池的状态是即不接受新的任务,并且将工作队列里的任务抛出.而且中断当前线程池里的线程所执行的任务.
 *    4: TIDYING   当前的线程池状态是所有任务都已终止,workerCount(活动线程数)为零.转换到状态TIDYING的线程池将运行terminate()钩子方法
 *    5: TERMINATED当前的线程池状态是终止状态,当terminate()钩子方法完成时
 * }高三位的第一位来存储符号位(1为负,零为正).最低从高位借出两位开始.如果是借出两位则包含[-1, 1]区间的整数(-1, 0, 1).而我们的状态有五个.
 * 通过位移方式不能表示够五个状态,则往高位借出3位.则能表示的整数再[-3, 3]区间.(-3, -2, -1, 0, 1, 2, 3).通过位移能够表示五个状态.
 * 如果是借出高四位的话就会造成浪费,因为高四位能表示15个整数,高三位已经能够表示5个状态了.所以没必要.而且使用高4位会降低我们workCount(有效线程数)的大小.
 *
 * 所以我们限制 workerCount(有效线程数)为 (2^29 )-1（约 5 亿）个线程,而不是 (2^31)-1（20 亿)个其他可表示的线程
 *
 * RUNNING -> SHUTDOWN 调用shutdown()方法,可能隐藏在我们的finalize()
 * RUNNING or SHUTDOWN -> STOP 调用shutdownNow()方法
 * SHUTDOWN -> TIDYING 当线程池和工作队列都为空时
 * STOP -> TIDYING 当线程池为空时
 * TIDYING -> TERMINATED 调用terminate()钩子方法完成
 */
public class UsageThreadPool {

    /**
     * 默认线程池里的线程个数为5
     */
    private static final int DEFAULT_THREAD_POOL_NUMBER = 5;

    /**
     * 默认的任务为10个
     */
    private static final int LOOP_NUMBER = 10;

    /**
     * 执行任务的最大循环数.
     */
    private static final int LOOP_NUMBER_MIX = 50;

    public static void main(String[] args) throws InterruptedException {
        UsageSingalThreadPool.run();
        Thread.sleep(3000L);
        System.out.println("<====================================>");
        UsageSingleThreadScheduled.run();
        Thread.sleep(5000L);
        System.out.println("<====================================>");
        UsageFixedThreadPool.run();
        Thread.sleep(3000L);
        System.out.println("<====================================>");
        UsageCachedThreadPool.run();
        Thread.sleep(3000L);
        System.out.println("<====================================>");
        UsageScheduledThreadPool.run();
    }

    private static class UsageSingalThreadPool {
        public static void run() {
            // 创建单线程池来执行相关的tasks,只会用一个线程去处理所有的任务,确保任务的执行的顺序满足定义的(FIFO, LIFO, 优先级).
            ExecutorService service = Executors.newSingleThreadExecutor();
            try {
                for (int i = 0; i < LOOP_NUMBER; i++) {
                    service.execute(() -> System.out.println("执行的当前线程" + Thread.currentThread().getName() + ", 在处理作业"));
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                // 关闭已当前创建的线程池
                service.shutdown();
            }
        }
    }

    private static class UsageFixedThreadPool {
        public static void run() {
            // 创建定长线程的线程池,控制线程的最大并发数目,当前线程池没有空闲的线程资源,则需要处理的任务则在等待队列里进行等待.
            // 直到线程池里有空闲的线程,则线程在等待队列里调用等待的任务.
            ExecutorService service = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_NUMBER);
            try {
                for (int i = 0; i < LOOP_NUMBER; i++) {
                    service.execute(() -> System.out.println("执行的当前线程" + Thread.currentThread().getName() + ", 在执行任务"));
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                service.shutdown();
            }
        }
    }

    private static class UsageCachedThreadPool {
        public static void run() {
            // 缓存线程池,当任务需要的个数超过了线程长度.则灵活回收已经处理完任务的线程.进行使用.如果没有线程被回收.
            // 则该线程池会创建新的线程去处理在等待队列里的任务.
            ExecutorService service = Executors.newCachedThreadPool();
            try {
                for (int i = 0; i < LOOP_NUMBER_MIX; i++) {
                    service.execute(() -> System.out.println("执行任务的线程" + Thread.currentThread().getName() + ", 执行任务"));
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                service.shutdown();
            }
        }
    }

    private static class UsageScheduledThreadPool {
        public static void run() {
            // 创建一个定长线程池,可定时或延时执行任务.
            ScheduledExecutorService service = Executors.newScheduledThreadPool(DEFAULT_THREAD_POOL_NUMBER);
            try {
                Runnable one = () -> System.out.println("当前执行的线程" + Thread.currentThread().getName() + ", 两秒后执行任务");
                service.schedule(one, 2, TimeUnit.SECONDS);
                Runnable two = () -> System.out.println("当前执行的线程" + Thread.currentThread().getName() + ", 延迟2秒后3秒之后执行任务");
                service.scheduleAtFixedRate(two, 2, 3, TimeUnit.SECONDS);
                for (int i = 0; i < LOOP_NUMBER; i++) {
                    service.execute(() -> System.out.println("执行任务的线程" + Thread.currentThread().getName() + ", 执行普通任务"));
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private static class UsageSingleThreadScheduled {
        public static void run() {
            // 创建一个单线程执行程序, 可以安排命令在给定延迟后运行, 或定期执行。
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            try {
                for (int i = 0; i < LOOP_NUMBER; i++) {
                    service.schedule(() -> System.out.println("当前执行的线程" + Thread.currentThread().getName() + ", 开始延迟3秒执行"), 3, TimeUnit.SECONDS);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                service.shutdown();
            }
        }
    }
}
