// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.document;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hiero.metrics.demo.crawler.api.util.Named;

public interface SchemeCrawler extends Named {

    boolean supports(URI uri);

    DocumentFetcher fetcher();

    Map<String, DocumentProcessor> processors();

    static Map<String, DocumentProcessor> asMap(DocumentProcessor... processors) {
        return Map.copyOf(Stream.of(processors).collect(Collectors.toMap(Named::getName, proc -> proc)));
    }
}
