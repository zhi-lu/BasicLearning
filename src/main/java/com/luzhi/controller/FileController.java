package com.luzhi.controller;

import cn.hutool.core.io.FileUtil;
import com.luzhi.service.TimeService;
import com.luzhi.synchro.UsageSync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

/**
 * @author zhilu
 */
public class FileController {

    private static final long TIMEOUT = 10L;

    private static final int INIT_SPINNING_LOCK_NUMBER = 5;

    private static final String ORIGINAL_PATH = System.getProperty("user.dir");

    public static void main(String[] args) {
        UsageSync.readAndWrite();
    }

    public static void run() {
        String filePath = ORIGINAL_PATH + "\\src\\abc.txt";
        long start = System.currentTimeMillis();
        File file = new File(filePath);
        if (file.exists()) {
            for (; ; ) {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    String message = bufferedReader.readLine();
                    if ("OK".toLowerCase(Locale.ROOT).equals(message)) {
                        System.out.println("运行成功");
                        return;
                    }
                    long time = TimeService.getInstance().getTime() - start;
                    if (time > TIMEOUT * 1000) {
                        System.err.println("超时运行失败");
                        return;
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        } else {
            System.out.println("文件不存在");
        }
    }

    @SuppressWarnings("BusyWait")
    public static void runSpinningLock() {
        String filePath = ORIGINAL_PATH + "\\src\\abc.txt";
        if (!FileUtil.exist(filePath)) {
            System.out.println("文件不存在");
            return;
        }
        CountDownLatch count = new CountDownLatch(INIT_SPINNING_LOCK_NUMBER);
        for (; ; ) {
            try {
                File file = new File(filePath);
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String message = bufferedReader.readLine();
                if ("1".toLowerCase(Locale.ROOT).equals(message)) {
                    System.out.println("运行成功");
                    return;
                }
                count.countDown();
                System.out.println("输出:" + count.getCount());
                bufferedReader.close();
                if (count.getCount() == 0) {
                    Thread.sleep(8000L);
                    count = new CountDownLatch(5);
                    System.out.println("又可以再进行自旋");
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
