package io.github.whmmm.springboottest;

import io.github.whmmm.commons.asynctask.AsyncTaskExecutor;
import io.github.whmmm.commons.spring3.filter.RequestLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

@Slf4j
public class Test2 {
    @Test
    public void test() {
        RequestLogUtil.setTraceId("123");
        AsyncTaskExecutor executor = new AsyncTaskExecutor(
                Executors.newVirtualThreadPerTaskExecutor()
        );
        executor.setDecorator(t -> {
            System.out.println("decorator");
            String traceId = RequestLogUtil.getTraceId();
            log.warn("父线程");
            return () -> {
                try {
                    RequestLogUtil.setTraceId(traceId);
                    log.warn("子线程内部，traceId:{}", traceId);
                    return t.call();
                } finally {
                    RequestLogUtil.removeTraceId();
                }
            };
        });
        executor.submit(() -> {
            log.warn("hello world");
            return 0;
        });
    }
}
