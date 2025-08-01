// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.file;

import java.net.URI;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.document.DocumentFetcher;
import org.hiero.metrics.demo.crawler.api.exception.DocumentFetchException;

public class PathDocumentFetcher implements DocumentFetcher {

    private static final Logger logger = LogManager.getLogger(PathDocumentFetcher.class);

    @Override
    public Optional<Document> fetch(URI uri) throws DocumentFetchException {
        logger.info("Fetching path document, uri={}", uri);
        return Optional.of(new PathDocument(uri));
    }
}
