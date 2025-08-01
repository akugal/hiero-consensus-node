// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.exception;

public class DocumentProcessException extends JobException {

    public DocumentProcessException(String message) {
        super(message);
    }

    public DocumentProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
