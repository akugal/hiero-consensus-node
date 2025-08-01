// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.document;

import java.net.URI;

public abstract class AbstractDocument implements Document {

    private final URI uri;

    public AbstractDocument(URI uri) {
        this.uri = uri;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return uri.toString();
    }
}
