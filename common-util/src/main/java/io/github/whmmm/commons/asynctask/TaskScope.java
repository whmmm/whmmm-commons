package io.github.whmmm.commons.asynctask;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class TaskScope<T> {
    private final Semaphore semaphore;
    private final AsyncTaskExecutorService executorService;
    private final List<Future<T>> futures = new ArrayList<>();

    TaskScope(Semaphore semaphore, AsyncTaskExecutorService executorService) {
        this.semaphore = semaphore;
        this.executorService = executorService;
    }

    public FutureTask<T> submit(Callable<T> callable) {
        FutureTask<T> task = this.executorService.submit(callable, this.semaphore);
        this.futures.add(task);
        return task;
    }

    public List<T> get() {
        List<T> result = new ArrayList<>();
        for (Future<T> future : this.futures) {
            try {
                T f = future.get();
                if (f != null) {
                    result.add(f);
                }
            } catch (InterruptedException e) {
                log.error("TaskScope get interrupted error: {}", e.getMessage(), e);
            } catch (ExecutionException e) {
                log.error("TaskScope get Execution error: {}", e.getMessage(), e);
            }
        }
        return result;
    }

    public List<T> getAndThrows() throws ExecutionException, InterruptedException {
        List<T> result = new ArrayList<>();
        for (Future<T> future : this.futures) {
            T f = future.get();
            if (f != null) {
                result.add(f);
            }
        }
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <V> List<V> getFlatValues() {
        List result = new ArrayList<>();
        List<T> ts = this.get();
        for (T t : ts) {
            if (t instanceof Collection) {
                result.addAll((Collection) t);
            } else {
                result.add(t);
            }
        }
        return result;
    }
}
