package io.github.whmmm.springboottest;

import io.github.whmmm.commons.asynctask.AsyncTaskExecutorService;
import io.github.whmmm.commons.asynctask.TaskScope;
import io.github.whmmm.commons.spring3.filter.RequestLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Executors;

@Slf4j
public class Test2 {
    @Test
    public void test() {
        RequestLogUtil.setTraceId("123");
        AsyncTaskExecutorService executor = AsyncTaskExecutorService.createTaskExecutor(
                Executors.newVirtualThreadPerTaskExecutor()
        );
        executor.setTaskDecorator(t -> {
            System.out.println("decorator");
            String traceId = RequestLogUtil.getTraceId();
            log.warn("父线程");
            return () -> {
                try {
                    RequestLogUtil.setTraceId(traceId);
                    log.warn("子线程内部，traceId:{}", traceId);
                    return t.getCallable().call();
                } finally {
                    RequestLogUtil.removeTraceId();
                }
            };
        });
        executor.submit(() -> {
            log.warn("hello world");
            return 0;
        });

        TaskScope<Object> scope = executor.scope(1);
        for (int i = 0; i < 20; i++) {
            final Integer v = i;
            scope.submit(() -> {
                System.out.println("thread " + Thread.currentThread().getName() + "~~~");
                return v;
            });
        }
        List<Object> list = scope.get();

        System.out.println(list);
    }
}
