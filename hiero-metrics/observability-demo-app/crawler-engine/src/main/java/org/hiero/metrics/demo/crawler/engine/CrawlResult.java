// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.engine;

import java.net.URI;
import java.util.Set;

public record CrawlResult(URI url, Set<URI> references) {

    @Override
    public String toString() {
        return "CrawlResult{" +
                "url=" + url +
                ", references=" + references +
                '}';
    }
}
