// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.http;

import org.hiero.metrics.demo.crawler.api.document.DocumentFetcher;
import org.hiero.metrics.demo.crawler.api.document.DocumentProcessor;

public final class CrawlerHttpFacade {

    public static final DocumentProcessor HOST_COUNTER_PROCESSOR = new HostCounterDocumentProcessor();
    public static final DocumentFetcher HTML_FETCHER = new HtmlDocumentFetcher();
}
