// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.http;

import java.net.URI;
import java.util.Map;
import org.hiero.metrics.demo.crawler.api.document.DocumentFetcher;
import org.hiero.metrics.demo.crawler.api.document.DocumentProcessor;
import org.hiero.metrics.demo.crawler.api.document.DocumentSizeProcessor;
import org.hiero.metrics.demo.crawler.api.document.SchemeCrawler;

public class HttpSchemeCrawler implements SchemeCrawler {

    private final DocumentFetcher fetcher = new HtmlDocumentFetcher();
    private final Map<String, DocumentProcessor> processors =
            SchemeCrawler.asMap(new HostCounterDocumentProcessor(), new DocumentSizeProcessor());

    @Override
    public String getName() {
        return "http";
    }

    @Override
    public boolean supports(URI uri) {
        String scheme = uri.getScheme();
        return scheme != null && (scheme.equals("http") || scheme.equals("https"));
    }

    @Override
    public DocumentFetcher fetcher() {
        return fetcher;
    }

    @Override
    public Map<String, DocumentProcessor> processors() {
        return processors;
    }
}
