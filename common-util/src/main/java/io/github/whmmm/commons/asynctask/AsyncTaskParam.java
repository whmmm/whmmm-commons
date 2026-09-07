package io.github.whmmm.commons.asynctask;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

@Data
public class AsyncTaskParam implements Serializable {
    private String source;

    private String traceId;
    private String thread;

    private boolean throwException;

    private transient Semaphore semaphore;

    private Date createAt;
    private Date startAt;
    private Date endAt;
    private long duration;


    private boolean started;

    private Object taskResult;

    private boolean record;
    private String taskType;
    private String taskName;

    private Object extraParam;
    private Map<String, Object> map = new LinkedHashMap<>();

    public void start() {
        if (this.isStarted()) {
            return;
        }
        this.setStarted(true);
        this.setThread(Thread.currentThread().getName());
        this.setStartAt(new Date());
    }
}
