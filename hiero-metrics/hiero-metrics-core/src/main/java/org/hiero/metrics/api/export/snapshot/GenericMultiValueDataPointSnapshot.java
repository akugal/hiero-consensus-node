// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.snapshot;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A {@link DataPointSnapshot} that contains multiple double values, each identified by a type string.
 * Value types are classified by a common classifier string accessible via {@link #valueClassifier()}, e.g. "stat".
 */
public interface GenericMultiValueDataPointSnapshot extends DataPointSnapshot {

    /**
     * Classifier of the value types, e.g. "stat".
     *
     * @return value classifier
     */
    @NonNull
    String valueClassifier();

    /**
     * Value type at the given index.
     *
     * @param idx index in range [0, {@link #valuesCount()}).
     * @return value type at the given index e.g. "min", "max", "avg", "p95", etc.
     */
    @NonNull
    String valueTypeAt(int idx);

    /**
     * @return number of values of the data point.
     */
    int valuesCount();

    /**
     * Value at the given index.
     *
     * @param idx index in range [0, {@link #valuesCount()}).
     * @return value at the given index
     */
    double valueAt(int idx);
}
