// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.document.cache;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.net.URI;
import java.util.Optional;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.document.DocumentFetcher;
import org.hiero.metrics.demo.crawler.api.exception.DocumentFetchException;

public final class NoOpDocumentCache implements DocumentCache {

    public static final NoOpDocumentCache INSTANCE = new NoOpDocumentCache();

    private NoOpDocumentCache() {}

    @Override
    public String getName() {
        return "doc-no-op";
    }

    @Override
    public Optional<Document> fetchIfAbsent(URI uri, DocumentFetcher fetcher) throws DocumentFetchException {
        return fetcher.fetch(uri);
    }

    @Override
    public void bind(@NonNull MetricRegistry registry) {
        // no cache to measure
    }
}
