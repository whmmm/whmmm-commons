package org.whmmm.util.asynctask;

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

    /**
     *
     */
    @Nullable
    @Setter(AccessLevel.PRIVATE)
    private T result;

    @Setter(AccessLevel.PRIVATE)
    private boolean cancelled;

    @Setter(AccessLevel.PRIVATE)
    private boolean success;

    @Setter(AccessLevel.PRIVATE)
    private Exception exception;

    private AsyncTask(Future<T> future) {
        this.future = future;
    }


    /**
     * 创建一个 {@link AsyncTask} 对象
     *
     * @param future
     * @param <T>
     * @return
     */
    public static <T> AsyncTask<T> task(Future<T> future) {
        return new AsyncTask<>(future);
    }

    /**
     * 调用 {@link Future#get()}, <br/>
     * 如果有异常，则内部记录异常，并且返回 null
     *
     * @return {@link T}
     */
    @Nullable
    public T get() {
        if (this.isSuccess()) {
            return this.getResult();
        }

        if (this.getException() != null) {
            return null;
        }

        try {
            T t = this.future.get();
            this.setSuccess(true);
            this.setResult(t);
            return t;
        } catch (InterruptedException e) {
            this.setCancelled(true);
            this.setSuccess(false);
            this.setException(e);
            log.error(e.getMessage(), e);
        } catch (ExecutionException e) {
            this.setSuccess(false);
            this.setException(e);
            log.error(e.getMessage(), e);
        }

        return null;
    }

    @Nonnull
    public T getNonnull() {
        T t = this.get();
        if (t == null) {
            throw new NullPointerException("method `getNonnull` return value cannot be null!");
        }
        return t;
    }


    /**
     * 获取结果，抛出异常
     *
     * @return {@link T }
     * @throws InterruptedException see {@link Future#get()}
     * @throws ExecutionException   see {@link Future#get()}
     * @throws RuntimeException     缓存 {@link Future#get()} 抛出的异常，重新以 RuntimeException 抛出
     */
    public T getOrThrows() throws
            InterruptedException, ExecutionException, RuntimeException {

        if (this.isSuccess()) {
            return this.getResult();
        }
        if (this.getException() != null) {
            throw new RuntimeException(this.getException());
        }

        T t = null;
        try {
            t = this.getFuture().get();
            this.setSuccess(true);
            this.setResult(t);

        } catch (InterruptedException e) {
            this.setSuccess(false);
            this.setCancelled(true);
            this.setException(e);
            throw e;
        } catch (ExecutionException e) {
            this.setSuccess(false);
            this.setException(e);
            throw e;
        }

        return t;
    }

}
