// SPDX-License-Identifier: Apache-2.0

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class TestItem {

    private final String uri;
    private Duration timeout = Duration.ofSeconds(30); // Default timeout
    private int depth = 1; // Default depth
    private final List<String> processors = new ArrayList<>();

    public TestItem(String uri) {
        this.uri = Objects.requireNonNull(uri, "URI must not be null");
    }

    public TestItem withTimeout(Duration timeout) {
        Objects.requireNonNull(timeout, "Timeout must not be null");
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
        this.timeout = timeout;
        return this;
    }

    public TestItem withDepth(int depth) {
        if (depth < 0) {
            throw new IllegalArgumentException("Depth must be non-negative");
        }
        this.depth = depth;
        return this;
    }

    public TestItem withProcessors(String... processors) {
        this.processors.addAll(Arrays.asList(processors));
        return this;
    }

    public String uri() {
        return uri;
    }

    public Duration timeout() {
        return timeout;
    }

    public int depth() {
        return depth;
    }

    public List<String> processors() {
        return processors;
    }
}
