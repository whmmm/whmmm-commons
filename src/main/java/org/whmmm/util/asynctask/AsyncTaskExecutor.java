package org.whmmm.util.asynctask;


import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.whmmm.util.function.Action;
import org.whmmm.util.function.ConsumerEx;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.*;

@SuppressWarnings({"rawtypes"})
@Slf4j
public final class AsyncTaskExecutor {
    private final ExecutorService executorService;

    @Setter
    private Action before;
    @Setter
    private ConsumerEx after;
    @Setter
    private ConsumerEx<Exception> error;

    public AsyncTaskExecutor(@Nullable ExecutorService executorService) {
        ExecutorService service = executorService;
        if (service == null) {
            service = this.createDefaultExecutorService("async-task-executor--");
        }

        this.executorService = service;
    }

    public AsyncTaskExecutor() {
        this(null);
    }

    private ExecutorService createDefaultExecutorService(String prefix) {
        ThreadFactory factory = new NamedThreadFactory(prefix, false);

        int cpuCores = Runtime.getRuntime().availableProcessors();
        ExecutorService threadPoolExecutor = new ThreadPoolExecutor(
                cpuCores * 8,
                cpuCores * 16,
                180,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000),
                factory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return threadPoolExecutor;
    }


    @SuppressWarnings({"unchecked"})
    public <T> AsyncTask<T> submit(Callable<T> callable) {
        return AsyncTask.task(executorService.submit(() -> {
            try {
                if (before != null) {
                    before.perform();
                }
                T t = callable.call();

                if (after != null) {
                    after.accept(t);
                }

                return t;
            } catch (Exception e) {
                if (error != null) {
                    error.accept(e);
                    return null;
                } else {
                    throw e;
                }
            } finally {

            }
        }));
    }

    @SuppressWarnings({"unchecked"})
    public <T> AsyncTask<T> submit(Callable<T> callable,
                                   @Nonnull Semaphore semaphore) {
        return AsyncTask.task(executorService.submit(() -> {
            try {
                semaphore.acquire();
                if (before != null) {
                    before.perform();
                }
                T t = callable.call();
                if (after != null) {
                    after.accept(t);
                }
                return t;
            } catch (Exception e) {
                if (error != null) {
                    error.accept(e);
                    return null;
                } else {
                    throw e;
                }
            } finally {
                semaphore.release();
            }
        }));
    }

    public void close() {
        this.executorService.close();
    }
}
