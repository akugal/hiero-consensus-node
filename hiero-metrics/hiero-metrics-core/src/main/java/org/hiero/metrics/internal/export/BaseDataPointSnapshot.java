// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.hiero.metrics.api.export.DataPointSnapshot;
import org.hiero.metrics.internal.core.LabelValues;

import java.util.Objects;

public abstract class BaseDataPointSnapshot implements DataPointSnapshot {

    private final LabelValues dynamicLabelValues;

    public BaseDataPointSnapshot(@NonNull LabelValues dynamicLabelValues) {
        this.dynamicLabelValues = dynamicLabelValues;
    }

    @NonNull
    @Override
    public String labelValue(int idx) {
        return dynamicLabelValues.get(idx);
    }

    public abstract void setValueAt(int idx, double value);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DataPointSnapshot{");
        sb.append("label-values=").append(dynamicLabelValues);
        sb.append(", value-classifier=").append(valueClassifier());
        sb.append(", values=");
        for (int i = 0; i < valuesSize(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(valueTypeAt(i)).append("=").append(valueAt(i));
        }
        return sb.append("}").toString();
    }
}
