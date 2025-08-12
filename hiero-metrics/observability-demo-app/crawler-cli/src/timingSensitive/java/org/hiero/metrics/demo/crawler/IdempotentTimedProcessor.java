// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler; // SPDX-License-Identifier: Apache-2.0

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.hiero.metrics.api.stat.StatUtils;

public class IdempotentTimedProcessor {

    private final double deviation;

    private final Map<URI, Long> idempotencyCache = new ConcurrentHashMap<>();
    private final Function<URI, Long> timeGenerator;

    public IdempotentTimedProcessor(long minMs, long maxMs, double deviation) {
        if (minMs < 0 || maxMs < 0) {
            throw new IllegalArgumentException("Min or max time must be non-negative");
        }
        if (minMs > maxMs) {
            throw new IllegalArgumentException("Min time must be less than or equal to max time");
        }
        if (deviation < StatUtils.ZERO || deviation > StatUtils.ONE) {
            throw new IllegalArgumentException("Deviation must be between 0 and 1");
        }

        this.timeGenerator = link -> TestUtils.randomLong(minMs, maxMs);
        this.deviation = deviation;
    }

    public void process(URI uri) throws InterruptedException {
        long originTime = idempotencyCache.computeIfAbsent(uri, timeGenerator);
        long currentTime = TestUtils.randomDeviation(originTime, deviation);
        TestUtils.simulateBlockingIO(Duration.ofMillis(currentTime));
    }
}
