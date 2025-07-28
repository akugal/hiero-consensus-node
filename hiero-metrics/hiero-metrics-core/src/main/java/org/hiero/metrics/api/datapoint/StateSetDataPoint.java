// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Set;

public interface StateSetDataPoint<T> extends DataPoint {

    void setFalse(T value);

    void setTrue(T value);

    boolean getState(T value);

    @NonNull
    Set<T> getStates();

    @Override
    default void reset() {
        // there is no use case to reset the entire set, just reset the states
    }
}
