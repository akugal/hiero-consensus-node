// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.hiero.metrics.internal.core.LabelValues;

public final class SingleValueDataPointSnapshot extends BaseDataPointSnapshot {

    private double value = Double.NaN;

    public SingleValueDataPointSnapshot(@NonNull LabelValues dynamicLabelValues) {
        super(dynamicLabelValues);
    }

    @Override
    public void setValueAt(int idx, double value) {
        checkIdx(idx);
        this.value = value;
    }

    @Nullable
    @Override
    public String valueClassifier() {
        return null;
    }

    @Override
    public int valuesSize() {
        return 1;
    }

    @Override
    public double valueAt(int idx) {
        checkIdx(idx);
        return value;
    }

    @Nullable
    @Override
    public String valueTypeAt(int idx) {
        return null;
    }

    private void checkIdx(int idx) {
        if (idx != 0) {
            throw new IndexOutOfBoundsException("Out of valuesSize() range");
        }
    }
}
