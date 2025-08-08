// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.stat;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.IntBinaryOperator;
import java.util.function.IntSupplier;
import java.util.function.LongBinaryOperator;
import java.util.function.LongSupplier;
import java.util.function.ToDoubleBiFunction;

public final class StatUtils {

    public static final String DEFAULT_STAT_LABEL = "stat";

    public static final Object NO_DEFAULT_INITIALIZER = new Object();

    public static final double ZERO = 0.0;
    public static final double ONE = 1.0;

    /**
     * changes average very slowly
     */
    public static final double WEIGHT_SMOOTH = 0.01;

    /**
     * changes average quite rapidly
     */
    public static final double WEIGHT_VOLATILE = 0.1;

    public static final double WEIGHT_DEFAULT = 0.5;

    public static final BooleanSupplier BOOL_INIT_FALSE = () -> false;
    public static final BooleanSupplier BOOL_INIT_TRUE = () -> true;

    public static final IntSupplier INT_INIT = () -> 0;
    public static final IntBinaryOperator INT_SUM = Integer::sum;
    public static final IntBinaryOperator INT_MIN = Integer::min;
    public static final IntBinaryOperator INT_MAX = Integer::max;
    public static final IntBinaryOperator INT_LAST = (current, supplied) -> supplied;
    public static final IntBinaryOperator INT_NO_OP = (current, supplied) -> current;
    public static final ToDoubleBiFunction<Integer, Integer> INT_AVERAGE =
            (sum, count) -> count == 0 ? 0 : (double) sum / count;

    public static final LongSupplier LONG_INIT = () -> 0L;
    public static final LongBinaryOperator LONG_SUM = Long::sum;
    public static final LongBinaryOperator LONG_MIN = Long::min;
    public static final LongBinaryOperator LONG_MAX = Long::max;
    public static final LongBinaryOperator LONG_LAST = (current, supplied) -> supplied;
    public static final LongBinaryOperator LONG_NO_OP = (current, supplied) -> current;

    public static final DoubleSupplier DOUBLE_INIT = () -> ZERO;
    public static final DoubleBinaryOperator DOUBLE_SUM = Double::sum;
    public static final DoubleBinaryOperator DOUBLE_MIN = Double::min;
    public static final DoubleBinaryOperator DOUBLE_MAX = Double::max;
    public static final DoubleBinaryOperator DOUBLE_LAST = (current, supplied) -> supplied;
    public static final DoubleBinaryOperator DOUBLE_NO_OP = (current, supplied) -> current;

    public static final DoubleBinaryOperator DOUBLE_AVG_DEFAULT =
            (prev, cur) -> prev * (1 - WEIGHT_DEFAULT) + cur * WEIGHT_DEFAULT;
    public static final DoubleBinaryOperator DOUBLE_AVG_VOLATILE =
            (prev, cur) -> prev * (1 - WEIGHT_VOLATILE) + cur * WEIGHT_VOLATILE;
    public static final DoubleBinaryOperator DOUBLE_AVG_SMOOTH =
            (prev, cur) -> prev * (1 - WEIGHT_SMOOTH) + cur * WEIGHT_SMOOTH;

    private StatUtils() {}

    public static BooleanSupplier asInitializer(boolean value) {
        return value ? BOOL_INIT_TRUE : BOOL_INIT_FALSE;
    }

    public static IntSupplier asInitializer(int value) {
        return value == 0 ? INT_INIT : () -> value;
    }

    public static LongSupplier asInitializer(long value) {
        return value == 0L ? LONG_INIT : () -> value;
    }

    public static DoubleSupplier asInitializer(double value) {
        return value == ZERO ? DOUBLE_INIT : () -> value;
    }
}
