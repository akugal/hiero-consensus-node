// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class Unit {

    private Unit() {}

    /**
     * Unit of nanoseconds.
     */
    public static final String NANOSECOND_UNIT = "ns";

    /**
     * Unit of microseconds.
     */
    public static final String MICROSECOND_UNIT = "µs";

    /**
     * Unit of milliseconds.
     */
    public static final String MILLISECOND_UNIT = "ms";

    public static final String SECOND_UNIT = "s";

    @NonNull
    public static String getUnit(final ChronoUnit timeUnit) {
        Objects.requireNonNull(timeUnit, "timeUnit must not be null");
        return switch (timeUnit) {
            case NANOS -> NANOSECOND_UNIT;
            case MICROS -> MICROSECOND_UNIT;
            case MILLIS -> MILLISECOND_UNIT;
            case SECONDS -> SECOND_UNIT;
            default -> "";
        };
    }
}
