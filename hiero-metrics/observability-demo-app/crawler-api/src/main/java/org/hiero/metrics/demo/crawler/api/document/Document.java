// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.document;

import java.net.URI;
import java.util.List;

public interface Document {

    URI getUri();

    long sizeInBytes();

    List<URI> getLinks();
}
