package org.whmmm.util;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.thread.ThreadUtil;
import org.whmmm.util.asynctask.AsyncTask;

import java.util.*;
import java.util.concurrent.*;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {
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

        factory = virtualFactory;

        ExecutorService threadPoolExecutor = new ThreadPoolExecutor(
                cpuCores * 2,
                cpuCores * 4,
                180,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000),
                factory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

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

        Future<Integer> future = threadPoolExecutor.submit(() -> {
            System.out.println("xxxxxx");
            return 1;
        });


        AsyncTask<Integer> task = AsyncTask.task(future);

        Integer i = task.getOrDefault();


        System.out.println(i);


        executorService.close();
        threadPoolExecutor.close();


        String a = "abc";
        switch (a) {
            case "abc" -> System.out.println(a);
        }
    }
}
