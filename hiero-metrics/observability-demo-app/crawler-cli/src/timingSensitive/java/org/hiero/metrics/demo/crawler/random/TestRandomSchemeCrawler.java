// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.random; // SPDX-License-Identifier: Apache-2.0

import java.net.URI;
import java.util.Map;
import org.hiero.metrics.demo.crawler.IdempotentTimedProcessor;
import org.hiero.metrics.demo.crawler.api.document.DocumentFetcher;
import org.hiero.metrics.demo.crawler.api.document.DocumentProcessor;
import org.hiero.metrics.demo.crawler.api.document.SchemeCrawler;

public class TestRandomSchemeCrawler implements SchemeCrawler {

    private final String scheme = "random";
    private final TestRandomDocumentFetcher fetcher;
    private final Map<String, DocumentProcessor> processors;

    public TestRandomSchemeCrawler(
            IdempotentTimedProcessor fetcher, double repeatedLinksProbability, int linksMin, int linksMax,
            IdempotentTimedProcessor processor) {
        this.fetcher = new TestRandomDocumentFetcher(fetcher, repeatedLinksProbability, linksMin, linksMax);
        this.processors = SchemeCrawler.asMap(new TestRandomDocumentProcessor(processor));
    }

    @Override
    public boolean supports(URI uri) {
        return uri.getScheme().equals(scheme);
    }

    @Override
    public DocumentFetcher fetcher() {
        return fetcher;
    }

    @Override
    public Map<String, DocumentProcessor> processors() {
        return processors;
    }

    @Override
    public String getName() {
        return scheme;
    }
}
