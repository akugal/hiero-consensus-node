// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.file;

import java.net.URI;
import java.util.Map;
import org.hiero.metrics.demo.crawler.api.document.DocumentFetcher;
import org.hiero.metrics.demo.crawler.api.document.DocumentProcessor;
import org.hiero.metrics.demo.crawler.api.document.DocumentSizeProcessor;
import org.hiero.metrics.demo.crawler.api.document.SchemeProcessor;

public class FileSchemeProcessor implements SchemeProcessor {

    private final DocumentFetcher fetcher = new PathDocumentFetcher();
    private final Map<String, DocumentProcessor> processors =
            SchemeProcessor.asMap(new PathCounterDocumentProcessor(), new DocumentSizeProcessor());

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public boolean supports(URI uri) {
        String scheme = uri.getScheme();
        return scheme != null && scheme.equals("file");
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
