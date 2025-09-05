package org.hiero.metrics.api.export;

/**
 * Exception to indicate problems during metrics export.
 * If other than this exception is thrown during export, exporter should be disabled
 */
public class MetricsExportException extends Exception {

    public MetricsExportException(String message) {
        super(message);
    }

    public MetricsExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
