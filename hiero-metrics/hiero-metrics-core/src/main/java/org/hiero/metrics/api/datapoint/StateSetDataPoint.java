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
}
