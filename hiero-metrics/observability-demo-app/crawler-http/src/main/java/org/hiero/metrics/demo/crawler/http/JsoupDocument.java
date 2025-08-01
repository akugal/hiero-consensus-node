// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.http;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.demo.crawler.api.document.AbstractDocument;
import org.jsoup.nodes.Document;

public class JsoupDocument extends AbstractDocument {

    private static final Logger logger = LogManager.getLogger(JsoupDocument.class);

    private final Document document;
    private final List<URI> links;

    public JsoupDocument(URI url, Document document) {
        super(url);
        this.document = document;
        links = List.copyOf(getLinksSet());
    }

    @Override
    public long sizeInBytes() {
        return document.html().length() * 2L; // each char is 2 bytes
    }

    @Override
    public List<URI> getLinks() {
        return links;
    }

    private Set<URI> getLinksSet() {
        return document.select("a[href]").stream()
                .map(element -> element.attr("abs:href"))
                .filter(link -> !link.isEmpty())
                .filter(link -> link.startsWith("http"))
                .map(this::toURI)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Nullable
    private URI toURI(String url) {
        try {
            return URI.create(url);
        } catch (Exception e) {
            logger.warn("Invalid URL found in document - ignoring: {}", url);
            return null;
        }
    }
}
