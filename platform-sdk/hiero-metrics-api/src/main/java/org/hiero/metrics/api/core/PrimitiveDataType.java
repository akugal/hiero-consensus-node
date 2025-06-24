package org.hiero.metrics.api.core;

public enum PrimitiveDataType {

    LONG,
    DOUBLE,
    STRING,
    BOOLEAN;

    public static PrimitiveDataType mapDataType(final Class<?> type) {
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