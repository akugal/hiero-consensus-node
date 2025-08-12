// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.random; // SPDX-License-Identifier: Apache-2.0

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.hiero.metrics.demo.crawler.IdempotentTimedProcessor;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.document.DocumentFetcher;
import org.hiero.metrics.demo.crawler.api.exception.DocumentFetchException;

public class TestRandomDocumentFetcher implements DocumentFetcher {

    private final Map<URI, Document> docCache = new ConcurrentHashMap<>();
    private final IdempotentTimedProcessor fetcher;
    private final Function<URI, Document> docGenerator;

    public TestRandomDocumentFetcher(IdempotentTimedProcessor fetcher, double repeatedLinksProbability,
                                     int linksMin, int linksMax) {
        this.fetcher = fetcher;
        IdGenerator linkIdGenerator = new IdGenerator(repeatedLinksProbability);
        docGenerator = uri -> new TestRandomDocument(uri, linkIdGenerator, linksMin, linksMax);
    }

    @Override
    public Optional<Document> fetch(URI uri) throws DocumentFetchException {
        // take doc from cache to be consistent and have the same next links for the same URI, but vary only fetch time
        Document document = docCache.computeIfAbsent(uri, docGenerator);
        try {
            // simulate fetching the document
            fetcher.process(uri);
        } catch (InterruptedException e) {
            throw new DocumentFetchException("Interrupted while fetching document: " + uri, e);
        }
        return Optional.of(document);
    }
}
