// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.exception;

public class JobTimeoutException extends JobException {

    public JobTimeoutException(String message) {
        super(message);
    }
}
