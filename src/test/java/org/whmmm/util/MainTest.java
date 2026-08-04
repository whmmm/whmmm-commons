package org.whmmm.util;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.whmmm.util.asynctask.AsyncTask;
import org.whmmm.util.asynctask.AsyncTaskExecutor;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class MainTest {
    @Test
    public void test() {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        IO.println(String.format("Hello and welcome!"));

        for (int i = 1; i <= 5; i++) {
            //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
            // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
            IO.println("i = " + i);
        }

        int cpuCores = Runtime.getRuntime().availableProcessors();
        ThreadFactory factory = ThreadUtil.newNamedThreadFactory("test--", false);

        ThreadFactory virtualFactory = Thread.ofVirtual().factory();

        //factory = virtualFactory;

        AsyncTaskExecutor executor = new AsyncTaskExecutor();
        executor.setBefore(() -> {
            log.info("before async task execution...");
        });
        executor.setAfter((x) -> {
            log.info("after async task execution...");
            log.info("return value is {}", x);
        });
        executor.setError((e) -> {
            log.error(e.getMessage(), e);
        });

        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        List<Object> list = new ArrayList<>();
        Set<Object> set = new HashSet<>();

        Map<Object, Object> map = new HashMap<>();


        Semaphore semaphore = new Semaphore(3);

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        AsyncTask<Integer> task = executor.submit(() -> {
            System.out.println("xxxxxx");
            return 1;
        });

        executor.submit(() -> {
            Integer i = 10;
            Integer b = 0;

            return i / b;
        }).get();


        Integer i = task.get();


        System.out.println(i);


        executorService.close();


        String a = "abc";
        switch (a) {
            case "abc" -> System.out.println(a);
        }


        executor.close();
        System.out.println("结束...");
    }
}
