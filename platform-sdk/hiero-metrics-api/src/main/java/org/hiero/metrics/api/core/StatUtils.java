// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import java.util.function.DoubleBinaryOperator;
import java.util.function.LongBinaryOperator;

public final class StatUtils {

    /**
     * changes average very slowly
     */
    public static final double WEIGHT_SMOOTH = 0.01;

    /**
     * changes average quite rapidly
     */
    public static final double WEIGHT_VOLATILE = 0.1;

    public static final double WEIGHT_DEFAULT = 0.5;

    public static final LongBinaryOperator LONG_SUM = Long::sum;
    public static final LongBinaryOperator LONG_MIN = Long::min;
    public static final LongBinaryOperator LONG_MAX = Long::max;

    public static final DoubleBinaryOperator DOUBLE_SUM = Double::sum;
    public static final DoubleBinaryOperator DOUBLE_MIN = Double::min;
    public static final DoubleBinaryOperator DOUBLE_MAX = Double::max;

    public static final DoubleBinaryOperator DOUBLE_AVG_DEFAULT =
            (prev, cur) -> prev * (1 - WEIGHT_DEFAULT) + cur * WEIGHT_DEFAULT;
    public static final DoubleBinaryOperator DOUBLE_AVG_VOLATILE =
            (prev, cur) -> prev * (1 - WEIGHT_VOLATILE) + cur * WEIGHT_VOLATILE;
    public static final DoubleBinaryOperator DOUBLE_AVG_SMOOTH =
            (prev, cur) -> prev * (1 - WEIGHT_SMOOTH) + cur * WEIGHT_SMOOTH;

    private StatUtils() {}
}
