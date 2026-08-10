package io.github.whmmm.commons.asynctask;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.*;

@SuppressWarnings({"rawtypes"})
@Slf4j
public final class AsyncTaskExecutor {
    private final ExecutorService executorService;

    @Setter
    private Decorator decorator;

    public AsyncTaskExecutor(@Nullable ExecutorService executorService) {
        ExecutorService service = executorService;
        if (service == null) {
            service = createExecutorService("async-task-executor--");
        }

        this.executorService = service;
    }

    public AsyncTaskExecutor() {
        this(null);
    }

    public static ExecutorService createExecutorService(String prefix) {
        ThreadFactory factory = new NamedThreadFactory(prefix, false);

        int cpuCores = Runtime.getRuntime().availableProcessors();
        ExecutorService threadPoolExecutor = new ThreadPoolExecutor(
                cpuCores * 8,
                cpuCores * 16,
                180,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                factory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return threadPoolExecutor;
    }


    @SuppressWarnings({"unchecked"})
    public <T> AsyncTask<T> submit(Callable<T> callable) {
        Callable<T> call = callable;
        if (this.decorator != null) {
            try {
                call = decorator.decorate(call);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return AsyncTask.task(executorService.submit(call));
    }

    @SuppressWarnings({"unchecked"})
    public <T> AsyncTask<T> submit(Callable<T> callable,
                                   @Nonnull Semaphore semaphore) {
        Callable<T> call = () -> {
            try {
                semaphore.acquire();
                return callable.call();
            } finally {
                semaphore.release();
            }
        };
        if (this.decorator != null) {
            try {
                call = decorator.decorate(call);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return AsyncTask.task(executorService.submit(call));
    }

    public void close() {
        this.executorService.close();
    }


    public interface Decorator<T> {
        Callable<T> decorate(Callable<T> callable) throws Exception;
    }
}
