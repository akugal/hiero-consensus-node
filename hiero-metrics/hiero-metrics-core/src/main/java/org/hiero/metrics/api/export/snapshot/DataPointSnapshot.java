// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.snapshot;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Snapshot of a single data point of a {@link org.hiero.metrics.api.core.Metric} at some point in time.
 * Implementations could be mutable for performance reasons, allowing to update the data point snapshot
 * in place with centralized snapshotting manager.
 * <p>
 *
 * Exporters must cast to specific implementation classes to access additional data
 * beyond the {@link DataPointSnapshot} interface. Existing extensions are:
 * <ul>
 *     <li> {@link SingleValueDataPointSnapshot}
 *     <li> {@link GenericMultiValueDataPointSnapshot}
 *     <li> {@link StateSetDataPointSnapshot}
 * </ul>
 */
public interface DataPointSnapshot {

    /**
     * Returns the value of the dynamic label at the given index.
     * The index is guaranteed to be in range [0, {@link org.hiero.metrics.api.core.Metric#dynamicLabelNames()}.size()).
     *
     * @param idx the index of the dynamic label
     * @return the value of the dynamic label at the given index
     */
    @NonNull
    String labelValue(int idx);
}
