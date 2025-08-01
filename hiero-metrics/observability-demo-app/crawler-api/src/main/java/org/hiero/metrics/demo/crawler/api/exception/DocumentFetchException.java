// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.exception;

public class DocumentFetchException extends JobException {

    public DocumentFetchException(Throwable error) {
        super(error);
    }

    public DocumentFetchException(String message) {
        super(message);
    }

    public DocumentFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
