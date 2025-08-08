// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.datapoint;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.hiero.metrics.api.datapoint.StateSetDataPoint;

public class GenerictStateSetDataPoint<T> implements StateSetDataPoint<T> {

    private final Map<T, Boolean> initState;
    private final Map<T, Boolean> states = new ConcurrentHashMap<>();

    public GenerictStateSetDataPoint() {
        this(Map.of());
    }

    public GenerictStateSetDataPoint(Map<T, Boolean> initState) {
        this.initState = initState == null ? Map.of() : initState;
        states.putAll(this.initState);
    }

    @Override
    public void setFalse(T value) {
        states.put(value, false);
    }

    @Override
    public void setTrue(T value) {
        states.put(value, true);
    }

    @NonNull
    @Override
    public Set<T> getStates() {
        return Collections.unmodifiableSet(states.keySet());
    }

    @Override
    public boolean getState(T value) {
        return states.getOrDefault(value, false);
    }

    @Override
    public void reset() {
        states.clear();
        if (!initState.isEmpty()) {
            states.putAll(initState);
        }
    }
}
