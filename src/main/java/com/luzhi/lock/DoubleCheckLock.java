package com.luzhi.lock;

import java.util.concurrent.*;

/**
 * @author zhilu
 */
public class DoubleCheckLock {

    private static final int LOOP_NUMBER = 1000;

    private static volatile DoubleCheckLock INSTANCE = null;

    private DoubleCheckLock() {
    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    public static DoubleCheckLock getInstance() {
        if (INSTANCE == null) {
            synchronized (DoubleCheckLock.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DoubleCheckLock();
                }
            }
        }
        return INSTANCE;
    }

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        ExecutorService service = new ThreadPoolExecutor(4, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
                Executors.defaultThreadFactory(), (r, executor) -> System.out.println("呵呵"));
        try {
            for (int i = 0; i < LOOP_NUMBER; i++) {
                service.execute(() -> System.out.println("对象的HashCode" + getInstance().hashCode()));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }finally {
            service.shutdown();
        }
    }
}

class ObjectLazy{
    private static final ObjectLazy INSTANCE = new ObjectLazy();
    private ObjectLazy() {}

    public static ObjectLazy getInstance() {
        return INSTANCE;
    }
}

