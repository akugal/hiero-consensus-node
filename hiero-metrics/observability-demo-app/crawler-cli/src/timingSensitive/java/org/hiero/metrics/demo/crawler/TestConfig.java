// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler; // SPDX-License-Identifier: Apache-2.0

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TestConfig {

    private final List<TestJobSpec> jobSpecs;
    private Duration timeout = Duration.ofSeconds(60); // Default timeout
    private double throughputPerSecond = 0.0; // Default throughput, 0 means no limit

    public TestConfig() {
        jobSpecs = new ArrayList<>();
    }

    public TestConfig(List<TestJobSpec> jobSpecs) {
        this.jobSpecs = jobSpecs;
    }

    public TestConfig(List<TestJobSpec> jobSpecs, Duration timeout, int throughputPerSecond) {
        this(jobSpecs);
        this.timeout = timeout;
        this.throughputPerSecond = throughputPerSecond;
    }

    public TestConfig withJobSpec(TestJobSpec jobSpec) {
        jobSpecs.add(Objects.requireNonNull(jobSpec, "Job spec must not be null"));
        return this;
    }

    public TestConfig withTimeout(Duration timeout) {
        Objects.requireNonNull(timeout, "Timeout must not be null");
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
        this.timeout = timeout;
        return this;
    }

    public TestConfig withThroughputPerSecond(double throughputPerSecond) {
        if (throughputPerSecond < 0) {
            throw new IllegalArgumentException("Throughput per second must be non-negative");
        }
        this.throughputPerSecond = throughputPerSecond;
        return this;
    }

    public List<TestJobSpec> items() {
        return jobSpecs;
    }

    public Duration timeout() {
        return timeout;
    }

    public double throughputPerSecond() {
        return throughputPerSecond;
    }
}
