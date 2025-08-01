// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.document;

import java.net.URI;
import java.util.Optional;
import org.hiero.metrics.demo.crawler.api.exception.DocumentFetchException;

@FunctionalInterface
public interface DocumentFetcher {

    Optional<Document> fetch(URI uri) throws DocumentFetchException;
}
