// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

public enum PrimitiveDataType {
    LONG(true),
    DOUBLE(true),
    STRING(false),
    BOOLEAN(false);

    private final boolean isNumber;

    PrimitiveDataType(boolean isNumber) {
        this.isNumber = isNumber;
    }

    public boolean isNumber() {
        return isNumber;
    }

    public static PrimitiveDataType mapDataType(final Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        if (Double.class.equals(type) || Float.class.equals(type)) {
            return PrimitiveDataType.DOUBLE;
        }
        if (Number.class.isAssignableFrom(type)) {
            return PrimitiveDataType.LONG;
        }
        if (Boolean.class.equals(type)) {
            return PrimitiveDataType.BOOLEAN;
        }
        if (String.class.equals(type)) {
            return PrimitiveDataType.STRING;
        }
        throw new IllegalArgumentException("Unsupported type: " + type.getName());
    }
}
