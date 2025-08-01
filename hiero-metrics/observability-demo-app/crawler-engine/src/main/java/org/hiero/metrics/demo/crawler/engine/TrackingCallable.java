package org.hiero.metrics.demo.crawler.engine;

import java.util.concurrent.Callable;

public class TrackingCallable<T> implements Callable<T> {

    private final Callable<T> callable;
    private boolean executed = false;

    public TrackingCallable(Callable<T> callable) {
        this.callable = callable;
    }

    @Override
    public T call() throws Exception {
        executed = true;
        return callable.call();
    }

    public boolean isExecuted() {
        return executed;
    }
}
