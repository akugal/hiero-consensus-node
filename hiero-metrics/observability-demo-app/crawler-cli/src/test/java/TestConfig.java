// SPDX-License-Identifier: Apache-2.0

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TestConfig {

    private final List<TestItem> items = new ArrayList<>();
    private Duration timeout = Duration.ofSeconds(60); // Default timeout
    private int concurrentUsers = 4; // Default concurrent users
    private int throughputPerSecond = 0; // Default throughput, 0 means no limit

    public TestConfig() {}

    public TestConfig(List<TestItem> items, Duration timeout, int concurrentUsers, int throughputPerSecond) {
        this.items.addAll(items);
        this.timeout = timeout;
        this.concurrentUsers = concurrentUsers;
        this.throughputPerSecond = throughputPerSecond;
    }

    public TestConfig withItem(TestItem item) {
        items.add(Objects.requireNonNull(item, "Test item must not be null"));
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

    public TestConfig withConcurrentUsers(int concurrentUsers) {
        if (concurrentUsers <= 0) {
            throw new IllegalArgumentException("Concurrent users must be greater than zero");
        }
        this.concurrentUsers = concurrentUsers;
        return this;
    }

    public TestConfig withThroughputPerSecond(int throughputPerSecond) {
        if (throughputPerSecond < 0) {
            throw new IllegalArgumentException("Throughput per second must be non-negative");
        }
        this.throughputPerSecond = throughputPerSecond;
        return this;
    }

    public List<TestItem> items() {
        return items;
    }

    public Duration timeout() {
        return timeout;
    }

    public int concurrentUsers() {
        return concurrentUsers;
    }

    public int throughputPerSecond() {
        return throughputPerSecond;
    }
}
