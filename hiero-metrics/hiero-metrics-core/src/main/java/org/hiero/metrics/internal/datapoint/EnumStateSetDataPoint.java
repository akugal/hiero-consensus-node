// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.datapoint;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Map;
import java.util.Set;
import org.hiero.metrics.api.datapoint.StateSetDataPoint;

public class EnumStateSetDataPoint<E extends Enum<E>> implements StateSetDataPoint<E> {

    private static final VarHandle ARR_HANDLER = MethodHandles.arrayElementVarHandle(boolean[].class);

    private final Map<E, Boolean> initValues;
    private final Set<E> statesSet;
    private final boolean[] states;

    public EnumStateSetDataPoint(Class<E> enumClass) {
        this(Map.of(), enumClass);
    }

    public EnumStateSetDataPoint(Map<E, Boolean> initValues, Class<E> enumClass) {
        this.initValues = initValues == null ? Map.of() : initValues;
        statesSet = Set.of(enumClass.getEnumConstants());
        states = new boolean[statesSet.size()];
    }

    @Override
    public void setFalse(E value) {
        ARR_HANDLER.setVolatile(states, value.ordinal(), false);
    }

    @Override
    public void setTrue(E value) {
        ARR_HANDLER.setVolatile(states, value.ordinal(), true);
    }

    @Override
    public boolean getState(E value) {
        return (boolean) ARR_HANDLER.getVolatile(states, value.ordinal());
    }

    @NonNull
    @Override
    public Set<E> getStates() {
        return statesSet;
    }

    @Override
    public void reset() {
        for (E state : statesSet) {
            ARR_HANDLER.setVolatile(states, state.ordinal(), initValues.getOrDefault(state, false));
        }
    }
}
