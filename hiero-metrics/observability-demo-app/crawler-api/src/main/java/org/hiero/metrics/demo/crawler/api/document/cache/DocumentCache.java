// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.document.cache;

import java.net.URI;
import java.util.Optional;
import org.hiero.metrics.api.core.MetricRegistryAware;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.document.DocumentFetcher;
import org.hiero.metrics.demo.crawler.api.exception.DocumentFetchException;

public interface DocumentCache extends MetricRegistryAware {

    Optional<Document> fetchIfAbsent(URI uri, DocumentFetcher fetcher) throws DocumentFetchException;
}
