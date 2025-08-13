// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.random;

import java.util.concurrent.atomic.AtomicInteger;
import org.hiero.metrics.demo.crawler.TestUtils;

public class IdGenerator {

    private final AtomicInteger counter = new AtomicInteger(0);
    private final double repeatProbability;

    public IdGenerator(double repeatProbability) {
        if (repeatProbability < 0 || repeatProbability >= 1) {
            throw new IllegalArgumentException("Repeated probability must be between 0 (inclusive) and 1 (exclusive)");
        }
        this.repeatProbability = repeatProbability;
    }

    public long nextId() {
        if (repeatProbability > 0.0 && counter.get() != 0 && Math.random() < repeatProbability) {
            return TestUtils.randomLong(1, counter.get() + 1);
        } else {
            return counter.incrementAndGet();
        }
    }
}
