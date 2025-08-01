// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.exception;

public class JobException extends RuntimeException {

    public JobException(Throwable error) {
        super(error);
    }

    public JobException(String message) {
        super(message);
    }

    public JobException(String message, Throwable cause) {
        super(message, cause);
    }
}
