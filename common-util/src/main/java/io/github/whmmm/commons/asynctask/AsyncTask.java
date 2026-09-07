package io.github.whmmm.commons.asynctask;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Data
@Slf4j
public class AsyncTask<T> implements Serializable {

    private final transient Future<T> future;

    AsyncTask(Future<T> future) {
        this.future = future;
    }

    public static <T> AsyncTask<T> task(Future<T> future) {
        return new AsyncTask<>(future);
    }


    public T getAndThrows() throws ExecutionException, InterruptedException {
        return this.future.get();
    }

    public T get() {
        T t = null;
        try {
            t = this.getAndThrows();
        } catch (ExecutionException e) {
            log.error("async task error: {}", e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("async task interrupted: {}", e.getMessage(), e);
        }
        return t;
    }

}
