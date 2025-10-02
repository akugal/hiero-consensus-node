// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Arrays;
import org.hiero.metrics.internal.core.LabelValues;

public final class FixedMultiValueDataPointSnapshot extends BaseDataPointSnapshot {

    private final String valueClassifier;
    private final String[] valueTypes;
    private final double[] values;

    public FixedMultiValueDataPointSnapshot(
            @NonNull LabelValues dynamicLabelValues, @NonNull String valueClassifier, @NonNull String[] valueTypes) {
        super(dynamicLabelValues);
        this.valueClassifier = valueClassifier;
        this.valueTypes = valueTypes;
        values = new double[valueTypes.length];
        Arrays.fill(values, Double.NaN);
    }

    @NonNull
    @Override
    public String valueClassifier() {
        return valueClassifier;
    }

    @Override
    public int valuesSize() {
        return values.length;
    }

    @Override
    public double valueAt(int idx) {
        return values[idx];
    }

    @Nullable
    @Override
    public String valueTypeAt(int idx) {
        return valueTypes[idx];
    }

    @Override
    public void setValueAt(int idx, double value) {
        values[idx] = value;
    }
}
