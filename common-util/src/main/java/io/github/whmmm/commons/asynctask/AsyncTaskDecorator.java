package io.github.whmmm.commons.asynctask;

import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.Callable;

@SuppressWarnings({"rawtypes"})
public interface AsyncTaskDecorator {


    Callable decorate(AsyncTaskContext context) throws Exception;


    @Data
    class AsyncTaskContext implements Serializable {
        private Callable callable;
        private AsyncTaskParam param;
    }
}