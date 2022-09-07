package com.luzhi.synchro;

import cn.hutool.core.io.FileUtil;

import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhilu
 */
public class UsageSync {

    /**
     * 给定长线程池初始化100个线程设置
     */
    private static final int THREAD_NUMBER = 100;

    /**
     * abc.txt文件的的绝对地址.
     */
    private static final String ORIGINAL_PATH_FILE = System.getProperty("user.dir") + "\\src\\abc.txt";

    public synchronized int run() {
        return readAndWrite();
    }

    public static int readAndWrite() {
        int value = 0;
        File file = new File(ORIGINAL_PATH_FILE);
        if (!FileUtil.exist(ORIGINAL_PATH_FILE)) {
            try {
                if (file.createNewFile()) {
                    System.out.println("abc.txt文件创建成功!");
                }
                try {
                    // 将原先的内容覆盖
                    FileOutputStream fileOutputStream = new FileOutputStream(ORIGINAL_PATH_FILE);
                    fileOutputStream.write("1".getBytes());
                    fileOutputStream.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                    System.exit(1);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                System.exit(1);
            }
        }
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            value = Integer.parseInt(bufferedReader.readLine());
            AtomicInteger atomicInteger = new AtomicInteger(value);
            atomicInteger.getAndIncrement();
            int newValue = atomicInteger.get();
            try {
                // 将原先的内容覆盖
                FileOutputStream fileOutputStream = new FileOutputStream(ORIGINAL_PATH_FILE);
                fileOutputStream.write(String.valueOf(newValue).getBytes());
                fileOutputStream.close();
            } catch (Exception exception) {
                exception.printStackTrace();
                System.exit(1);
            }
        }catch (Exception exception){
            exception.printStackTrace();
        }
        return value;
    }

    public static void main(String[] args) {
        ExecutorService service = new ThreadPoolExecutor(THREAD_NUMBER, THREAD_NUMBER, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), Executors.defaultThreadFactory(), (r, executor) -> System.exit(1));
        UsageSync sync = new UsageSync();
        try {
            for (int i = 0; i < THREAD_NUMBER; i++) {
                service.execute(() -> {
                    String threadMessage = "当前执行的线程是:" + Thread.currentThread().getName();
                    int currentFileValue = sync.run();
                    System.out.format("%s, abc.txt文件的值是%d\n", threadMessage, currentFileValue);
                });
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            service.shutdown();
        }
    }
}
