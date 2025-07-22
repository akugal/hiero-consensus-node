// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import java.util.concurrent.atomic.AtomicInteger;

public class StatContainer {

    private final AtomicInteger counter = new AtomicInteger(0);
    private final AtomicInteger sum = new AtomicInteger(0);

    public void update(int value) {
        counter.incrementAndGet();
        sum.accumulateAndGet(value, Integer::sum);
    }

    public int getCounter() {
        return counter.get();
    }

    public int getSum() {
        return sum.get();
    }

    public double getAverage() {
        int count = counter.get();
        return count == 0 ? 0.0 : (double) getSum() / count;
    }

    public void reset() {
        counter.set(0);
        sum.set(0);
    }
}
