// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class Unit {

    private Unit() {}

    /**
     * Unit of nanoseconds.
     */
    public static final String OPS_UNIT = "ops";

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

    public static final String BYTE_UNIT = "byte";
    public static final String MEGABYTE_UNIT = "mb";

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
