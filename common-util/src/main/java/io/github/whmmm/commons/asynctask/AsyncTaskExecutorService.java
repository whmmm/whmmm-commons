package io.github.whmmm.commons.asynctask;

import cn.hutool.core.util.RandomUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.concurrent.*;

@Slf4j
public final class AsyncTaskExecutorService {
    private final ExecutorService executorService;

    @Setter
    private AsyncTaskDecorator taskDecorator;

    AsyncTaskExecutorService(@Nullable ExecutorService executorService) {
        ExecutorService service = executorService;
        if (service == null) {
            int cpuCores = Runtime.getRuntime().availableProcessors();
            service = createThreadExecutor(
                    "async-task-executor--",
                    cpuCores * 2,
                    cpuCores * 4
            );
        }

        this.executorService = service;
    }

    public AsyncTaskExecutorService() {
        this(null);
    }

    /* ------------ static method start -------------- */

    public static AsyncTaskExecutorService createTaskExecutor(String prefix,
                                                              int coreSize,
                                                              int maxSize,
                                                              AsyncTaskDecorator decorator) {
        ExecutorService executorService = createThreadExecutor(prefix, coreSize, maxSize);
        return createTaskExecutor(executorService, decorator);
    }


    public static AsyncTaskExecutorService createTaskExecutor(ExecutorService service,
                                                              AsyncTaskDecorator decorator) {
        AsyncTaskExecutorService executor = new AsyncTaskExecutorService(service);
        executor.setTaskDecorator(decorator);
        return executor;
    }

    public static AsyncTaskExecutorService createTaskExecutor(ExecutorService service) {
        return createTaskExecutor(service, null);
    }

    public static ExecutorService createThreadExecutor(String prefix,
                                                       int coreSize,
                                                       int maxSize) {
        ThreadFactory factory = new NamedThreadFactory(prefix, false);

        // int cpuCores = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(
                coreSize,
                maxSize,
                10,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(1000),
                factory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /* ------------- static method end ------------- */


    @SuppressWarnings({"unchecked"})
    public <T> FutureTask<T> submit(Callable<T> callable,
                                    @Nullable AsyncTaskParam param) {
        Callable<T> taskCallable;
        final AsyncTaskParam taskParam = param == null ? this.createTaskParam() : param;
        if (taskParam.getCreateAt() == null) {
            taskParam.setCreateAt(new Date());
        }
        final Semaphore semaphore = taskParam.getSemaphore();

        taskCallable = () -> {
            try {
                taskParam.start();
                T called = callable.call();
                taskParam.setTaskResult(called);
                return called;
            } catch (Exception e) {
                log.error("异步任务执行错误: {}", e.getMessage(), e);
                if (taskParam.isThrowException()) {
                    throw e;
                }
            } finally {
                if (semaphore != null) {
                    semaphore.release();
                }
                taskParam.setEndAt(new Date());
                taskParam.setDuration(
                        taskParam.getEndAt().getTime() - taskParam.getCreateAt().getTime()
                );
            }
            return null;
        };

        if (this.taskDecorator != null) {
            try {
                final AsyncTaskDecorator.AsyncTaskContext context = new AsyncTaskDecorator.AsyncTaskContext();
                context.setCallable(taskCallable);
                context.setParam(taskParam);
                taskCallable = this.taskDecorator.decorate(context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        FutureTask<T> task = new FutureTask<>(taskCallable);

        // 提交任务
        if (semaphore != null) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
        try {
            this.executorService.execute(task);
        } catch (RuntimeException e) {
            if (semaphore != null) {
                semaphore.release();
            }
            throw e;
        }
        return task;
    }

    public <T> FutureTask<T> submit(Callable<T> callable, Semaphore semaphore) {
        AsyncTaskParam param = new AsyncTaskParam();
        param.setSemaphore(semaphore);
        return this.submit(callable, param);
    }

    public <T> FutureTask<T> submit(Callable<T> callable) {
        return this.submit(callable, this.createTaskParam());
    }

    public void close() {
        this.executorService.close();
    }

    public AsyncTaskParam createTaskParam() {
        AsyncTaskParam param = new AsyncTaskParam();
        String source = param.getSource();

        if (source == null || source.isEmpty()) {
            String threadName = Thread.currentThread().getName();
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(threadName).append("]");
            String name = sb.toString();
            param.setSource(name);
        }
        param.setTraceId("task-" + RandomUtil.randomStringUpper(6));

        return param;
    }

    public <T> TaskScope<T> scope(int concurrency) {
        Semaphore semaphore = new Semaphore(concurrency);
        return new TaskScope<>(semaphore, this);
    }
}
